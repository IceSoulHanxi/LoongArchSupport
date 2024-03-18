package com.ixnah.app.las.util;

import java.util.function.Supplier;

public class ThrowUtil {

    private ThrowUtil() {
        throw new UnsupportedOperationException();
    }

    public static void runPrinting(VoidThrowingSupplier supplier) {
        try {
            supplier.get();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void runThrowing(VoidThrowingSupplier supplier, Supplier<? extends RuntimeException> eSupplier) {
        try {
            supplier.get();
        } catch (Throwable e) {
            RuntimeException runtimeException = eSupplier.get();
            runtimeException.addSuppressed(e);
            throw runtimeException;
        }
    }

    public static <T> T runThrowing(GenericThrowingSupplier<T> supplier) {
        return runThrowing(supplier, RuntimeException::new);
    }

    public static <T> T runThrowing(GenericThrowingSupplier<T> supplier, Supplier<? extends RuntimeException> eSupplier) {
        try {
            return supplier.get();
        } catch (Throwable e) {
            RuntimeException runtimeException = eSupplier.get();
            runtimeException.addSuppressed(e);
            throw runtimeException;
        }
    }

    public static <T> T runOrNull(GenericThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable e) {
            return null;
        }
    }

    public static <T> T runPrintingOrNull(GenericThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T runOrDefault(GenericThrowingSupplier<T> supplier, Supplier<T> defaultSupplier) {
        try {
            return supplier.get();
        } catch (Throwable e) {
            return defaultSupplier.get();
        }
    }

    public static boolean runIfSucceed(VoidThrowingSupplier supplier) {
        try {
            supplier.get();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    public static <T> T requireNonNull(T obj, Supplier<? extends RuntimeException> supplier) {
        if (obj == null) {
            throw supplier.get();
        }
        return obj;
    }

    public static <T> T requireNonNull(GenericThrowingSupplier<T> objSupplier, Supplier<? extends RuntimeException> supplier) {
        if (objSupplier == null) {
            throw supplier.get();
        }
        T obj;
        try {
            obj = objSupplier.get();
        } catch (Throwable e) {
            RuntimeException runtimeException = supplier.get();
            runtimeException.addSuppressed(e);
            throw runtimeException;
        }
        return requireNonNull(obj, supplier);
    }

    @FunctionalInterface
    public interface VoidThrowingSupplier {
        void get() throws Throwable;
    }

    @FunctionalInterface
    public interface GenericThrowingSupplier<T> {
        T get() throws Throwable;
    }
}
