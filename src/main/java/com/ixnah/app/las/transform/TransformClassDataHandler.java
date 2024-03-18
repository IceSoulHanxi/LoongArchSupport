package com.ixnah.app.las.transform;

import com.intellij.ide.BytecodeTransformer;
import com.ixnah.app.las.util.UnsafeUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class TransformClassDataHandler implements InvocationHandler {

    private final Object originConsumer;
    private final BytecodeTransformer transformer;

    public TransformClassDataHandler(Object originConsumer, BytecodeTransformer transformer) {
        this.originConsumer = originConsumer;
        this.transformer = transformer;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        return switch (methodName) {
            case "isByteBufferSupported" -> {
                ClassLoader classLoader = originConsumer instanceof ClassLoader loader ? loader : unwrapConsumer(originConsumer);
                String string = args[0].toString();
                if (string.startsWith("com.sun.jna")) System.out.println(string);
                yield !transformer.isApplicable(args[0].toString(), classLoader, null);
            }
            case "consumeClassData" -> {
                if (ByteBuffer.class.isAssignableFrom(method.getParameterTypes()[1])) {
                    if (!method.canAccess(originConsumer)) method.setAccessible(true);
                    yield method.invoke(originConsumer, args);
                } else {
                    String name = args[0].toString();
                    byte[] classBytes = (byte[]) args[1];
                    ClassLoader classLoader = originConsumer instanceof ClassLoader loader ? loader : unwrapConsumer(originConsumer);
                    if (transformer.isApplicable(name, classLoader, null)) {
                        classBytes = transformer.transform(classLoader, name, null, classBytes);
                    }
                    if (!method.canAccess(originConsumer)) method.setAccessible(true);
                    yield args.length == 2
                            ? method.invoke(originConsumer, name, classBytes)
                            : method.invoke(originConsumer, Stream.concat(Stream.of(name, classBytes), Stream.of(args).skip(2)).toArray());
                }
            }
            case "toString" -> this.toString();
            case "equals" -> this.equals(args[0]);
            case "hashCode" -> this.hashCode();
            default -> throw new UnsupportedOperationException();
        };
    }

    private static final AtomicLong CLASS_DATA_CONSUMER_OFFSET = new AtomicLong();

    private static ClassLoader unwrapConsumer(Object wrappedConsumer) {
        try {
            if (CLASS_DATA_CONSUMER_OFFSET.get() == 0L) {
                Field consumerField = wrappedConsumer.getClass().getDeclaredField("classDataConsumer");
                long fieldOffset = UnsafeUtil.objectFieldOffset(consumerField);
                CLASS_DATA_CONSUMER_OFFSET.compareAndSet(0, fieldOffset);
            }
            return UnsafeUtil.getObject(wrappedConsumer, CLASS_DATA_CONSUMER_OFFSET.get());
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
