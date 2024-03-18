package com.ixnah.app.las.jna.proxy.handler;

import com.ixnah.app.las.jna.proxy.*;
import com.ixnah.app.las.jna.proxy.annonation.Symbol;
import com.ixnah.app.las.util.LogUtil;
import com.ixnah.app.las.util.UnsafeUtil;
import net.fornwall.jelf.ElfFile;
import net.fornwall.jelf.ElfSymbol;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalSymbolHandler implements InvocationHandler {

    private final Map<String, Function> symbolCache = new ConcurrentHashMap<>();
    private final NativeLibrary handle;
    private final Pointer baseSymbolPtr;
    private final long baseSymbolOffset;
    private final ElfFile libElf;

    public LocalSymbolHandler(NativeLibrary handle) {
        this.handle = handle;
        try {
            String symbolName = "gHotSpotVMStructs";
            baseSymbolPtr = handle.getGlobalVariableAddress(symbolName);
            String symbolFilePath = getSymbolFilePath(baseSymbolPtr);
            libElf = ElfFile.from(Files.newInputStream(Path.of(symbolFilePath)));
            baseSymbolOffset = libElf.getELFSymbol(symbolName).st_value;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if ("unwrap".equals(methodName) && Wrapper.class.isAssignableFrom(method.getDeclaringClass())) {
            return handle;
        } else if ("toString".equals(methodName) && method.getParameterTypes().length == 0) {
            return handle.toString();
        } else if ("equals".equals(methodName) && method.getParameterTypes().length == 1) {
            return handle.equals(args[0]);
        } else if ("hashCode".equals(methodName) && method.getParameterTypes().length == 0) {
            return handle.hashCode();
        } else {
            Symbol symbolInfo = method.getAnnotation(Symbol.class);
            if (symbolInfo == null) return null;
            Function function = symbolCache.computeIfAbsent(symbolInfo.value(), s -> {
                ElfSymbol symbol = libElf.getELFSymbol(symbolInfo.value());
                Function result = symbol == null ? null
                        : Function.getFunction(baseSymbolPtr.share(symbol.st_value - baseSymbolOffset));
                if (result != null && symbolInfo.allowObject()) {
                    Function.setOptions(result, Collections.singletonMap("allow-objects", true));
                }
                return result;
            });
            return function != null ? function.invoke(method.getReturnType(), args) : null;
        }
    }

    public static String getSymbolFilePath(Pointer symbolPointer) {
        NativeLibrary process = NativeLibrary.getInstance(null);
        Function dladdr = process.getFunction("dladdr");
        Memory dlInfo = Memory.create(UnsafeUtil.addressSize() * 4L);
        int dlAddrCode = dladdr.invokeInt(symbolPointer, dlInfo);
        LogUtil.d("dlAddrCode: " + dlAddrCode);
        Pointer dli_fname = dlInfo.getPointer(0);
        return dli_fname.getString(0);
    }

    @SuppressWarnings("unchecked")
    public static <T> T create(NativeLibrary handle, Class<T> tClass) {
        return (T) Proxy.newProxyInstance(tClass.getClassLoader(), new Class[]{tClass}, new LocalSymbolHandler(handle));
    }
}
