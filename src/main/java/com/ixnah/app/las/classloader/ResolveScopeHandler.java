package com.ixnah.app.las.classloader;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class ResolveScopeHandler implements InvocationHandler {

    private static final List<String> HANDLE_PACKAGE_NAMES = Arrays.asList("com.sun.jna.", "com.pty4j.", "jtermios.", "com.intellij.terminal.pty.");
    final Object handle;

    public ResolveScopeHandler(Object handle) {
        this.handle = handle;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("isDefinitelyAlienClass".equals(method.getName()) && args.length == 3
                && args[0] instanceof String name && args[1] instanceof String
                && HANDLE_PACKAGE_NAMES.stream().anyMatch(name::startsWith)) {
            return "";
        }
        return method.invoke(handle, args);
    }
}
