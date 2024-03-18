package com.ixnah.app.las.jna.proxy;

import com.ixnah.app.las.jna.proxy.handler.ObjectProxyHandler;
import com.ixnah.app.las.util.ClassLoaderUtil;
import com.ixnah.app.las.util.UnsafeUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public interface Function extends Pointer, Wrapper {
    static Function getFunction(Pointer p) {
        try {
            Class<?> Class_Function = ClassLoaderUtil.contextLoadClass("com.sun.jna.Function");
            Object unwrap = p.unwrap();
            Object result = Class_Function.getMethod("getFunction", unwrap.getClass()).invoke(null, unwrap);
            return ObjectProxyHandler.create(result, Function.class);
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    static void setOptions(Function function, Map<String, ?> options) {
        try {
            Object unwrap = function.unwrap();
            Class<?> Class_Function = unwrap.getClass();
            Field Function_options = Class_Function.getDeclaredField("options");
            long Function_options_offset = UnsafeUtil.objectFieldOffset(Function_options);
            UnsafeUtil.putObject(unwrap, Function_options_offset, options);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    Object invoke(Class<?> returnType, Object... inArgs);

    Object invokeObject(Object... args);

    Pointer invokePointer(Object... args);

    String invokeString(Object[] args, boolean wide);

    int invokeInt(Object... args);

    long invokeLong(Object... args);

    float invokeFloat(Object... args);

    double invokeDouble(Object... args);

    void invokeVoid(Object... args);
}
