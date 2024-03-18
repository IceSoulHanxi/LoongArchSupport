package com.ixnah.app.las.fsnotifier;

import com.intellij.ide.BytecodeTransformer;
import org.jetbrains.annotations.Nullable;

import java.security.ProtectionDomain;

public class FileWatcherTransformer implements BytecodeTransformer {
    static final String targetClassName = "com.intellij.openapi.vfs.impl.local.NativeFileWatcherImpl";

    @Override
    public boolean isApplicable(String className, ClassLoader loader, @Nullable ProtectionDomain protectionDomain) {
        return targetClassName.equals(className);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, @Nullable ProtectionDomain protectionDomain, byte[] classBytes) {
        // TODO: 删除未支持平台消息输出
        return classBytes;
    }
}
