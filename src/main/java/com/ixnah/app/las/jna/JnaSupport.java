package com.ixnah.app.las.jna;

import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.ide.plugins.cl.PluginClassLoader;
import com.intellij.util.lang.UrlClassLoader;
import com.ixnah.app.las.classloader.CustomClassLoader;
import com.ixnah.app.las.classloader.ResolveScopeHandler;
import com.ixnah.app.las.transform.TransformSupport;
import com.ixnah.app.las.util.ClassLoaderUtil;
import com.ixnah.app.las.util.LogUtil;
import com.ixnah.app.las.util.ResourceUtil;
import com.ixnah.app.las.util.UnsafeUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.nio.file.Path;

public class JnaSupport {

    private JnaSupport() {
        throw new UnsupportedOperationException();
    }

    public static void load(String osArch) throws Throwable {
        String jnaPathKey = "jna.boot.library.path";
        String jnaBootLibraryPathStr = System.getProperty(jnaPathKey);
        Path nativeDir = Path.of(jnaBootLibraryPathStr).getParent().resolve(osArch);
        ResourceUtil.extractNative(osArch, nativeDir, "libjnidispatch.so", ""/*TODO: md5*/);
        System.setProperty(jnaPathKey, nativeDir.toString());
        try {
            Class.forName("com.sun.jna.Native");
        } catch (Throwable ignore) {
            TransformSupport.getTransformPipeHandler().add(new JnaTransformer());
            // 建立一个使用全局ClassPath且隔离的ClassLoader用于加载已当指令集的JniDispatch
            ClassLoader appClassLoader = ClassLoaderUtil.getAppClassLoader();
            CustomClassLoader loader = new CustomClassLoader(UrlClassLoader.build());
            ClassLoaderUtil.copyUrlClassPath(loader, appClassLoader);
            ClassLoaderUtil.injectTransformPipe(loader);
            injectJar(loader);
        }
        LogUtil.d("loadJna end");
    }

    private static void injectJar(UrlClassLoader loader) throws NoSuchFieldException, InstantiationException {
        ClassLoaderUtil.injectTransformPipe(loader);
        Class<?> classLoaderClass = PluginClassLoader.class;
        Field _resolveScopeManagerField;
        try {
            _resolveScopeManagerField = classLoaderClass.getDeclaredField("resolveScopeManager");
        } catch (NoSuchFieldException e) {
            _resolveScopeManagerField = classLoaderClass.getDeclaredField("_resolveScopeManager");
        }
        long resolveScopeManagerOffset = UnsafeUtil.objectFieldOffset(_resolveScopeManagerField);
        Field parentsField = classLoaderClass.getDeclaredField("parents");
        long parentsOffset = UnsafeUtil.objectFieldOffset(parentsField);
        Field resolveScopeManagerField = _resolveScopeManagerField;
        IdeaPluginDescriptorImpl injectDescriptor = UnsafeUtil.allocateInstance(IdeaPluginDescriptorImpl.class);
        injectDescriptor.setPluginClassLoader(loader);
        PluginManagerCore.getLoadedPlugins().forEach(pd -> {
            if (!(pd.getPluginClassLoader() instanceof PluginClassLoader pluginClassLoader)) return;
            if (JnaSupport.class.getClassLoader().equals(pluginClassLoader)) return;

            // 通过resolveScopeManager来判断是否从当前loader加载 如果从当前loader加载则会直接查找父loader 注入绕过当前loader
            Object oldManager = UnsafeUtil.getObject(pluginClassLoader, resolveScopeManagerOffset);
            ResolveScopeHandler managerHandler = new ResolveScopeHandler(oldManager);
            Object newManager = Proxy.newProxyInstance(classLoaderClass.getClassLoader(),
                    new Class[]{resolveScopeManagerField.getType()}, managerHandler);
            UnsafeUtil.putObject(pluginClassLoader, resolveScopeManagerOffset, newManager);

            // 将隔离loader注入到插件依赖中
            IdeaPluginDescriptorImpl[] parents = UnsafeUtil.getObject(pluginClassLoader, parentsOffset);
            IdeaPluginDescriptorImpl[] newParents = new IdeaPluginDescriptorImpl[parents.length + 1];
            newParents[0] = injectDescriptor;
            if (parents.length > 0) {
                System.arraycopy(parents, 0, newParents, 1, parents.length);
            }
            UnsafeUtil.putObject(pluginClassLoader, parentsOffset, newParents);

            pluginClassLoader.clearParentListCache();
            LogUtil.d("inject jar to plugin " + pd.getPluginId());
        });
    }


}
