package com.ixnah.app.las.jna.proxy;

import com.ixnah.app.las.jna.proxy.handler.ObjectProxyHandler;
import com.ixnah.app.las.util.ClassLoaderUtil;

import java.lang.reflect.InvocationTargetException;

public interface NativeLibrary extends Wrapper {

    static NativeLibrary getInstance(String libraryName) {
        try {
            Class<?> Class_NativeLibrary = ClassLoaderUtil.contextLoadClass("com.sun.jna.NativeLibrary");
            Object instance = Class_NativeLibrary.getMethod("getInstance", String.class).invoke(null, libraryName);
            return ObjectProxyHandler.create(instance, NativeLibrary.class);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    Function getFunction(String functionName);

    Pointer getGlobalVariableAddress(String symbolName);
}
