package com.ixnah.app.las.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeUtil {

    private UnsafeUtil() {
        throw new UnsupportedOperationException();
    }

    static Unsafe unsafe;

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (Unsafe) theUnsafe.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static long staticFieldOffset(Field f) {
        return unsafe.staticFieldOffset(f);
    }

    public static long objectFieldOffset(Field f) {
        return unsafe.objectFieldOffset(f);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getObject(Object o, long offset) {
        return (T) unsafe.getObject(o, offset);
    }

    public static long getLong(Object o, long offset) {
        return unsafe.getLong(o, offset);
    }

    public static int getInt(Object o, long offset) {
        return unsafe.getInt(o, offset);
    }

    public static byte getByte(long address) {
        return unsafe.getByte(address);
    }

    public static void putObject(Object o, long offset, Object x) {
        unsafe.putObject(o, offset, x);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getObjectVolatile(Object o, long offset) {
        return (T) unsafe.getObjectVolatile(o, offset);
    }

    public static int addressSize() {
        return unsafe.addressSize();
    }

    @SuppressWarnings("unchecked")
    public static <T> T allocateInstance(Class<? extends T> cls) throws InstantiationException {
        return (T) unsafe.allocateInstance(cls);
    }
}
