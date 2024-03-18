package com.ixnah.app.las.jna.proxy.handler;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ixnah.app.las.jna.proxy.Wrapper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectProxyHandler implements InvocationHandler {

    private static final Cache<Method, Method> METHOD_MAP = CacheBuilder.newBuilder().weakKeys().weakValues().build();
    private static final Map<String, String> CLASS_MAP = new ConcurrentHashMap<>();

    static {
        CLASS_MAP.put("com.ixnah.app.las.jna.proxy.NativeLibrary", "com.sun.jna.NativeLibrary");
        CLASS_MAP.put("com.ixnah.app.las.jna.proxy.Function", "com.sun.jna.Function");
        CLASS_MAP.put("com.ixnah.app.las.jna.proxy.Pointer", "com.sun.jna.Pointer");
        CLASS_MAP.put("com.ixnah.app.las.jna.proxy.Memory", "com.sun.jna.Memory");
    }

    private final Object object;

    public ObjectProxyHandler(Object object) {
        this.object = object;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if ("unwrap".equals(methodName) && Wrapper.class.isAssignableFrom(method.getDeclaringClass())) {
            return object;
        } else if ("toString".equals(methodName) && method.getParameterTypes().length == 0) {
            return object.toString();
        } else if ("equals".equals(methodName) && method.getParameterTypes().length == 1) {
            return object.equals(args[0]);
        } else if ("hashCode".equals(methodName) && method.getParameterTypes().length == 0) {
            return object.hashCode();
        } else {
            Method realMethod = METHOD_MAP.get(method, () -> {
                Class<?>[] parameterTypes = method.getParameterTypes();
                Class<?> objectClass = object.getClass();
                for (int i = 0, size = parameterTypes.length; i < size; i++) {
                    String name = parameterTypes[i].getName();
                    String realName = CLASS_MAP.get(name);
                    if (realName != null) {
                        parameterTypes[i] = objectClass.getClassLoader().loadClass(realName);
                    }
                }
                return objectClass.getMethod(methodName, parameterTypes);
            });
            Class<?> returnType = "invoke".equals(methodName) && args[0] instanceof Class<?> c ? c : method.getReturnType();
            Object result = realMethod.invoke(object, unwrap(args));
            return result == null || returnType.isPrimitive() || returnType.isInstance(result) ? result : create(result, returnType);
        }
    }

    private Object[] unwrap(Object[] args) throws ClassNotFoundException {
        for (int i = 0, size = args.length; i < size; i++) {
            if (args[i] instanceof Wrapper wrapper) {
                args[i] = wrapper.unwrap();
            } else if (args[i] instanceof Class<?> c) {
                String realName = CLASS_MAP.get(c.getName());
                if (realName != null) {
                    args[i] = object.getClass().getClassLoader().loadClass(realName);
                }
            } else if (args[i] != null && args[i].getClass().isArray()) {
                unwrap((Object[]) args[i]);
            }
        }
        return args;
    }

    @SuppressWarnings("unchecked")
    public static <T> T create(Object object, Class<T> returnType) {
        return (T) Proxy.newProxyInstance(returnType.getClassLoader(), new Class[]{returnType}, new ObjectProxyHandler(object));
    }
}
