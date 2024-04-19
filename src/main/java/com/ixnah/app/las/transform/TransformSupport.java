package com.ixnah.app.las.transform;

import com.intellij.ide.BytecodeTransformer;
import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.util.lang.UrlClassLoader;
import com.ixnah.app.las.util.ClassLoaderUtil;
import com.ixnah.app.las.util.LogUtil;
import com.ixnah.app.las.util.UnsafeUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

public class TransformSupport {
    private static final TransformPipeHandler PIPELINE_HANDLER = new TransformPipeHandler();

    public static TransformPipeHandler getTransformPipeHandler() {
        return PIPELINE_HANDLER;
    }

    public static void load() throws Throwable {
        injectAppClassLoader();
        injectPluginClassLoader();
    }

    private static void injectAppClassLoader() throws NoSuchFieldException {
        ClassLoader appClassLoader = ClassLoaderUtil.getAppClassLoader();
        Field transformerField = appClassLoader.getClass().getDeclaredField("transformer");
        long transformerOffset = UnsafeUtil.objectFieldOffset(transformerField);
        Object transformer = UnsafeUtil.getObject(appClassLoader, transformerOffset);
        if (transformer != null) {
            Class<?> transformerClass = transformer.getClass();
            if (transformerClass.getName().endsWith("BytecodeTransformerAdapter")) {
                long fieldOffset = UnsafeUtil.objectFieldOffset(transformerClass.getDeclaredField("impl"));
                Object impl = UnsafeUtil.getObject(transformer, fieldOffset);
                PIPELINE_HANDLER.add((BytecodeTransformer) impl);
            } else if (transformer instanceof BytecodeTransformer bytecodeTransformer) {
                PIPELINE_HANDLER.add(bytecodeTransformer);
            } else {
                LogUtil.e("The transformer has been set, but the class is " + transformerClass);
            }
        }
        Class<?>[] interfaces = {transformerField.getType()};
        Object proxyAppTransformer = Proxy.newProxyInstance(appClassLoader, interfaces, PIPELINE_HANDLER);
        UnsafeUtil.putObject(appClassLoader, transformerOffset, proxyAppTransformer);
        LogUtil.i("inject transformer to app!");
    }

    private static void injectPluginClassLoader() throws NoSuchFieldException {
        ClassLoader appClassLoader = ClassLoaderUtil.getAppClassLoader();
        ClassLoader currentClassLoader = TransformSupport.class.getClassLoader();
        ClassLoader platformClassLoader = UrlClassLoader.getPlatformClassLoader();
        ClassLoader systemClassLoader = UrlClassLoader.getSystemClassLoader();
        Field consumerField = UrlClassLoader.class.getDeclaredField("classDataConsumer");
        Class<?>[] transformInterfaces = {BytecodeTransformer.class};
        Object pluginTransformer = Proxy.newProxyInstance(appClassLoader, transformInterfaces, PIPELINE_HANDLER);
        long consumerFieldOffset = UnsafeUtil.objectFieldOffset(consumerField);
        Class<?> dataConsumerClass = consumerField.getType();
        Class<?>[] consumerInterfaces = {dataConsumerClass};
        ClassLoader dataConsumerClassLoader = dataConsumerClass.getClassLoader();
        PluginManagerCore.getLoadedPlugins().forEach(pd -> {
            if (!(pd instanceof IdeaPluginDescriptorImpl descriptor)) return;
            ClassLoader loader = descriptor.getClassLoader();
            if (!(loader instanceof UrlClassLoader)
                    || appClassLoader.equals(loader) || currentClassLoader.equals(loader)
                    || platformClassLoader.equals(loader) || systemClassLoader.equals(loader)) {
                return;
            }
            Object consumer = UnsafeUtil.getObject(loader, consumerFieldOffset);
            if (Proxy.isProxyClass(consumer.getClass())) return;
            BytecodeTransformer bytecodeTransformer = (BytecodeTransformer) pluginTransformer;
            TransformClassDataHandler handler = new TransformClassDataHandler(consumer, bytecodeTransformer);
            Object proxyConsumer = Proxy.newProxyInstance(dataConsumerClassLoader, consumerInterfaces, handler);
            UnsafeUtil.putObject(loader, consumerFieldOffset, proxyConsumer);
            LogUtil.d("inject transformer to plugin " + descriptor.getPluginId());
        });
    }
}
