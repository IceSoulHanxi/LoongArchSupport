package com.ixnah.app.las.jna.proxy;

import com.ixnah.app.las.jna.proxy.handler.ObjectProxyHandler;
import com.ixnah.app.las.util.ClassLoaderUtil;

import java.lang.reflect.InvocationTargetException;

public interface Memory extends Pointer, Wrapper {

    static Memory create(long address) {
        try {
            Class<?> Class_Memory = ClassLoaderUtil.contextLoadClass("com.sun.jna.Memory");
            Object instance = Class_Memory.getConstructor(long.class).newInstance(address);
            return ObjectProxyHandler.create(instance, Memory.class);
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    Pointer getPointer(long offset);
}
