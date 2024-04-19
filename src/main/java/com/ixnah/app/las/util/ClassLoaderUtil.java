package com.ixnah.app.las.util;

import com.intellij.ide.BytecodeTransformer;
import com.intellij.util.lang.UrlClassLoader;
import com.ixnah.app.las.transform.TransformClassDataHandler;
import com.ixnah.app.las.transform.TransformSupport;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public class ClassLoaderUtil {

    private ClassLoaderUtil() {
        throw new UnsupportedOperationException();
    }

    private static ClassLoader appClassLoader;

    public synchronized static ClassLoader getAppClassLoader() {
        if (appClassLoader == null) {
            try {
                appClassLoader = Class.forName("com.intellij.ide.BytecodeTransformer").getClassLoader();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return appClassLoader;
    }

    public static void byContext(ClassLoader classLoader, Runnable runnable) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            runnable.run();
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    public static Class<?> contextLoadClass(String className) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(className);
    }

    public static ClassLoader createIsolated(ClassLoader appClassLoader) throws NoSuchFieldException {
        UrlClassLoader isolateLoader = UrlClassLoader.build().get();
        Field UrlClassLoader_classPath = UrlClassLoader.class.getDeclaredField("classPath");
        long UrlClassLoader_classPath_offset = UnsafeUtil.objectFieldOffset(UrlClassLoader_classPath);
        UnsafeUtil.putObject(isolateLoader, UrlClassLoader_classPath_offset, ((UrlClassLoader) appClassLoader).getClassPath());
        return isolateLoader;
    }

    public static ClassLoader copyUrlClassPath(ClassLoader classLoader, ClassLoader classPathFrom) throws NoSuchFieldException {
        if (classLoader instanceof UrlClassLoader) {
            Field UrlClassLoader_classPath = UrlClassLoader.class.getDeclaredField("classPath");
            long UrlClassLoader_classPath_offset = UnsafeUtil.objectFieldOffset(UrlClassLoader_classPath);
            UnsafeUtil.putObject(classLoader, UrlClassLoader_classPath_offset, ((UrlClassLoader) classPathFrom).getClassPath());
        }
        return classLoader;
    }

    public static void injectTransformPipe(ClassLoader classLoader) throws NoSuchFieldException {
        ClassLoader appClassLoader = getAppClassLoader();
        Field consumerField = UrlClassLoader.class.getDeclaredField("classDataConsumer");
        long consumerFieldOffset = UnsafeUtil.objectFieldOffset(consumerField);
        Class<?> dataConsumerClass = consumerField.getType();
        Object originConsumer = UnsafeUtil.getObject(classLoader, consumerFieldOffset);
        if (Proxy.isProxyClass(originConsumer.getClass())) return;
        InvocationHandler pipeHandler = TransformSupport.getTransformPipeHandler();
        Object transformer = Proxy.newProxyInstance(appClassLoader, new Class[]{BytecodeTransformer.class}, pipeHandler);
        TransformClassDataHandler handler = new TransformClassDataHandler(originConsumer, (BytecodeTransformer) transformer);
        ClassLoader dataConsumerClassLoader = dataConsumerClass.getClassLoader();
        Object proxyConsumer = Proxy.newProxyInstance(dataConsumerClassLoader, new Class[]{dataConsumerClass}, handler);
        UnsafeUtil.putObject(classLoader, consumerFieldOffset, proxyConsumer);
    }
}
