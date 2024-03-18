package com.ixnah.app.las.jna.proxy;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;

public interface Pointer extends Wrapper {
    Pointer share(long offset);

    short getShort(long offset);

    int getInt(long offset);

    long getLong(long offset);

    ByteBuffer getByteBuffer(long offset, long length);

    String getString(long offset);

    byte[] getByteArray(long offset, int arraySize);

    Pointer getPointer(long offset);

    static long nativeValue(Pointer p) {
        Class<?> pClass = p.unwrap().getClass();
        try {
            return (long) pClass.getMethod("nativeValue", pClass).invoke(null, p.unwrap());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
