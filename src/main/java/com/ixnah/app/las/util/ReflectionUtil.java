package com.ixnah.app.las.util;

import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Map;

public class ReflectionUtil {

    private ReflectionUtil() {
        throw new UnsupportedOperationException();
    }

    private static final class StackWalkerHolder {
        private static final StackWalker stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    }

    public static StackWalker getStackWalker() {
        return StackWalkerHolder.stackWalker;
    }

    public static Class<?> getCallerClass() {
        return getStackWalker().getCallerClass();
    }

    public static Class<?> getCallerClass(int skip) {
        return skip == 0
                ? getStackWalker().getCallerClass()
                : getStackWalker().walk(s -> s.skip(1 + skip).limit(1).map(StackWalker.StackFrame::getDeclaringClass).findFirst()).orElse(null);
    }

    public static String getCallerClassName() {
        return getCallerClassName(1);
    }

    public static String getCallerClassName(int skip) {
        return StackWalker.getInstance().walk(s -> s.skip(1 + skip).limit(1).map(StackWalker.StackFrame::getClassName).findFirst()).orElse(null);
    }

    public static String getCallerMethodName() {
        return getCallerMethodName(1);
    }

    public static String getCallerMethodName(int skip) {
        return StackWalker.getInstance().walk(s -> s.skip(1 + skip).limit(1).map(StackWalker.StackFrame::getMethodName).findFirst()).orElse(null);
    }

    public static Map.Entry<String, MethodType> getCallerMethod() {
        return getCallerMethod(1);
    }

    public static Map.Entry<String, MethodType> getCallerMethod(int skip) {
        return getStackWalker().walk(s -> s.skip(1 + skip).limit(1).map(frame -> Map.entry(frame.getMethodName(), frame.getMethodType())).findFirst()).orElse(null);
    }

    public static CallerInfo getCallerInfo() {
        return getCallerInfo(1);
    }

    public static CallerInfo getCallerInfo(int skip) {
        return getStackWalker().walk(s -> s.skip(1 + skip).limit(1).map(frame -> new CallerInfo(frame.getDeclaringClass(), frame.getMethodName(), frame.getMethodType())).findFirst()).orElse(null);
    }

    public record CallerInfo(Class<?> class_, String methodName, MethodType methodType) {
    }

    public static Method getMethod(Class<?> findClass, Map.Entry<String, MethodType> methodInfo) throws NoSuchMethodException {
        return findClass.getMethod(methodInfo.getKey(), methodInfo.getValue().parameterArray());
    }

    public static Method getDeclaredMethod(Class<?> findClass, Map.Entry<String, MethodType> methodInfo) throws NoSuchMethodException {
        return findClass.getDeclaredMethod(methodInfo.getKey(), methodInfo.getValue().parameterArray());
    }
}
