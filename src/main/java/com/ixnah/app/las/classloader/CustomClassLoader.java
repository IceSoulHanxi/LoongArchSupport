package com.ixnah.app.las.classloader;

import com.intellij.util.lang.UrlClassLoader;
import com.ixnah.app.las.util.ClassLoaderUtil;
import com.ixnah.app.las.util.LogUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class CustomClassLoader extends UrlClassLoader {

    private static final List<String> HANDLE_PACKAGE_NAMES = Arrays.asList("com.sun.jna.", "com.pty4j.", "jtermios.", "com.intellij.terminal.pty.");

    public CustomClassLoader(@NotNull Builder builder) {
        super(builder, registerAsParallelCapable());
    }

    @Override
    protected Class<?> findClass(@NotNull String name) throws ClassNotFoundException {
        if (HANDLE_PACKAGE_NAMES.stream().anyMatch(name::startsWith)) {
            LogUtil.d("findClass: " + name);
            return super.findClass(name);
        }
        return ClassLoaderUtil.getAppClassLoader().loadClass(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (HANDLE_PACKAGE_NAMES.stream().anyMatch(name::startsWith)) {
            LogUtil.d("loadClass: " + name);
            return super.loadClass(name, resolve);
        }
        return ClassLoaderUtil.getAppClassLoader().loadClass(name);
    }

    @Override
    public @Nullable Class<?> loadClassInsideSelf(String name, String fileName, long packageNameHash, boolean forceLoadFromSubPluginClassloader) throws IOException {
        if (HANDLE_PACKAGE_NAMES.stream().anyMatch(name::startsWith)) {
            LogUtil.d("loadClassInsideSelf: " + name);
            return super.loadClassInsideSelf(name, fileName, packageNameHash, forceLoadFromSubPluginClassloader);
        }
        return ((UrlClassLoader) ClassLoaderUtil.getAppClassLoader()).loadClassInsideSelf(name, fileName, packageNameHash, forceLoadFromSubPluginClassloader);
    }
}
