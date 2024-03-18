package com.ixnah.app.las.transform;

import com.intellij.ide.BytecodeTransformer;
import com.ixnah.app.las.util.LogUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.ProtectionDomain;
import java.util.AbstractList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.LongAdder;

public class TransformPipeHandler extends AbstractList<BytecodeTransformer> implements List<BytecodeTransformer>, InvocationHandler {

    private static final ThreadLocal<LongAdder> STACK_DEPTH = ThreadLocal.withInitial(LongAdder::new);
    private final List<BytecodeTransformer> transformers = new CopyOnWriteArrayList<>();

    @Override
    public void add(int index, BytecodeTransformer element) {
        Class<? extends BytecodeTransformer> elementClass = element.getClass();
        if (Proxy.isProxyClass(elementClass) && this == Proxy.getInvocationHandler(element)) {
            LogUtil.e("Bytecode transformer can't be proxy class: " + elementClass.getName());
        } else {
            transformers.add(index, element);
        }
    }

    @Override
    public BytecodeTransformer remove(int index) {
        return transformers.remove(index);
    }

    @Override
    public BytecodeTransformer get(int index) {
        return transformers.get(index);
    }

    @Override
    public int size() {
        return transformers.size();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        String methodName = method.getName();
        return switch (methodName) {
            case "isApplicable" -> {
                LongAdder stackDepth = STACK_DEPTH.get();
                try {
                    String className = (String) args[0];
                    stackDepth.increment();
                    if (stackDepth.longValue() > 3) {
                        LogUtil.e("Cyclic loading detected! " + className);
                        stackDepth.reset();
                        yield false;
                    }
                    yield transformers.stream().anyMatch(t -> t.isApplicable(className, (ClassLoader) args[1], null));
                } finally {
                    stackDepth.decrement();
                }
            }
            case "transform" -> {
                int bytesIndex = ProtectionDomain.class.isAssignableFrom(method.getParameterTypes()[2])
                        ? 3     // com.intellij.ide.BytecodeTransformer
                        : 2;    // com.intellij.util.lang.PathClassLoader$BytecodeTransformer
                yield transformers.stream().filter(t -> t.isApplicable((String) args[1], (ClassLoader) args[0], null))
                        .reduce((byte[]) args[bytesIndex], (b, t) -> t.transform((ClassLoader) args[0], (String) args[1], null, b), (b1, b2) -> b2);
            }
            case "toString" -> this.toString();
            case "equals" -> this.equals(args[0]);
            case "hashCode" -> this.hashCode();
            default -> throw new UnsupportedOperationException();
        };
    }
}
