package com.ixnah.app.las.util;

import com.intellij.ide.BytecodeTransformer;
import com.intellij.util.lang.UrlClassLoader;
import com.ixnah.app.las.transform.TransformClassDataHandler;
import com.ixnah.app.las.transform.TransformSupport;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ClassLoaderUtil {

    private ClassLoaderUtil() {
        throw new UnsupportedOperationException();
    }

    private static volatile ClassLoader appClassLoader;

    public static ClassLoader getAppClassLoader() {
        if (appClassLoader == null) {
            synchronized (ClassLoaderUtil.class) {
                if (appClassLoader == null) {
                    try {
                        appClassLoader = Class.forName("com.intellij.ide.BytecodeTransformer").getClassLoader();
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return appClassLoader;
    }

    private static final MethodHandle loadClassHandle = ThrowUtil.runPrintingOrNull(() -> {
        Method method = ClassLoader.class.getDeclaredMethod("loadClass", String.class, boolean.class);
        return MethodHandles.privateLookupIn(ClassLoader.class, MethodHandles.lookup()).unreflect(method);
    });

    public static Class<?> loadClass(ClassLoader classLoader, String name, boolean resolve) throws ClassNotFoundException {
        try {
            return (Class<?>) loadClassHandle.invokeExact(classLoader, name, resolve);
        } catch (Throwable e) {
            throw e instanceof ClassNotFoundException e1 ? e1 : new ClassNotFoundException(name, e);
        }
    }

    private static final MethodHandle findClassHandle = ThrowUtil.runPrintingOrNull(() -> {
        Method method = UrlClassLoader.class.getDeclaredMethod("findClass", String.class);
        return MethodHandles.privateLookupIn(UrlClassLoader.class, MethodHandles.lookup()).unreflect(method);
    });

    public static Class<?> findClass(ClassLoader classLoader, String name) throws ClassNotFoundException {
        try {
            return (Class<?>) findClassHandle.invokeExact(classLoader, name);
        } catch (Throwable e) {
            throw e instanceof ClassNotFoundException e1 ? e1 : new ClassNotFoundException(name, e);
        }
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
