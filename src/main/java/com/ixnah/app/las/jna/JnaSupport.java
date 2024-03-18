package com.ixnah.app.las.jna;

import com.intellij.util.lang.UrlClassLoader;
import com.ixnah.app.las.jna.proxy.InternalJvm;
import com.ixnah.app.las.jna.proxy.NativeLibrary;
import com.ixnah.app.las.jna.proxy.Pointer;
import com.ixnah.app.las.jna.proxy.handler.LocalSymbolHandler;
import com.ixnah.app.las.transform.TransformSupport;
import com.ixnah.app.las.util.ClassLoaderUtil;
import com.ixnah.app.las.util.LogUtil;
import com.ixnah.app.las.util.ResourceUtil;
import com.ixnah.app.las.util.ThrowUtil;

import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

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
            ClassLoader isolated = ClassLoaderUtil.copyUrlClassPath(UrlClassLoader.build().get(), appClassLoader);
            TransformSupport.injectUrlClassLoader(isolated);
            ClassLoaderUtil.byContext(isolated, JnaSupport::removeLoadedNative);
        }
        LogUtil.d("loadJna end");
    }

    private static void removeLoadedNative() {
        // TODO: 从jvm中移除已加载的类 com.sun.jna.Native
        NativeLibrary library = NativeLibrary.getInstance("jvm");
        InternalJvm jvm = LocalSymbolHandler.create(library, InternalJvm.class);
        jvm.cleanInitErrorTable();
        Class<?> classNative = ThrowUtil.runThrowing(() -> Class.forName("com.sun.jna.Native", false, JnaSupport.class.getClassLoader()));
        ClassLoader classLoader = classNative.getClassLoader();
//        LogUtil.d(Arrays.toString(classLoader.getClass().getDeclaredFields()));
//        LogUtil.d(Arrays.toString(ClassLoader.class.getDeclaredFields()));
//            Field classesField = ThrowUtil.runThrowing(() -> ClassLoader.class.getDeclaredField("classes"));
//            long classesOffset = UnsafeUtil.objectFieldOffset(classesField);
//            List<Class<?>> classes = UnsafeUtil.getObject(classLoader, classesOffset);
//            synchronized (classes) {
//                classes.remove(classNative);
//                System.gc();
//            }
        Pointer classLoaderOop = jvm.resolveExternalGuard(classLoader);
        LogUtil.d("classLoaderOop: " + classLoaderOop);
        Pointer nonReflectClassLoaderOop = jvm.nonReflectionClassLoader(classLoaderOop);
        LogUtil.d("nonReflectClassLoaderOop: " + nonReflectClassLoaderOop);
        Pointer loaderData = jvm.loaderDataAcquire(nonReflectClassLoaderOop);
        LogUtil.d("loaderData: " + loaderData);
        Map<String, Long> offsetMap = VmStructs.getEntryStream(library).filter(entry -> {
            String name = entry.getName();
            String declaringType = entry.getDeclaringType();
            return ("_dictionary".equals(name) && "ClassLoaderData".equals(declaringType))
                    || ("_name".equals(name) && "Klass".equals(declaringType));
        }).limit(2).collect(Collectors.toMap(VmStructs.VmStructEntry::getName, VmStructs.VmStructEntry::getOffset));
        LogUtil.d("offsetMap: " + offsetMap);
        Pointer dictionary = loaderData.getPointer(offsetMap.get("_dictionary"));
        LogUtil.d("dictionary: " + dictionary);
        Pointer classNativeOop = jvm.resolveExternalGuard(classNative);
        LogUtil.d("classNativeOop: " + classNativeOop);
        Pointer klassNative = jvm.loadKlassRaw(classNativeOop);
        LogUtil.d("klassNative: " + klassNative);
        Pointer symbolNative = klassNative.getPointer(offsetMap.get("_name"));
        LogUtil.d("symbolNative: " + symbolNative);
        int index = jvm.classHashtableIndexFor(dictionary, symbolNative);
        LogUtil.d("index: " + index);
        int hash = jvm.classHashtableComputeHash(dictionary, symbolNative);
        LogUtil.d("hash: " + hash);
        Pointer entry = jvm.getDictionaryEntry(dictionary, index, hash, symbolNative);
        LogUtil.d("entry: " + entry);
        jvm.freeDictionaryEntry(dictionary, entry);
//        LogUtil.d("entry: " + jvm.getDictionaryEntry(dictionary, index, hash, symbolNative));
//        jvm.unloadClass(klassNative);
        try {
            Class.forName("com.sun.jna.Native");
        } catch (ClassNotFoundException e) {
            LogUtil.e(e);
        }
    }
}
