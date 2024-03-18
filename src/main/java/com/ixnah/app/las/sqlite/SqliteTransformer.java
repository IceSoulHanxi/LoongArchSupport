package com.ixnah.app.las.sqlite;

import com.intellij.ide.BytecodeTransformer;
import org.jetbrains.annotations.Nullable;

import java.security.ProtectionDomain;

public class SqliteTransformer implements BytecodeTransformer {

    @Override
    public boolean isApplicable(String className, ClassLoader loader, @Nullable ProtectionDomain protectionDomain) {
        return false;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, @Nullable ProtectionDomain protectionDomain, byte[] classBytes) {
        return classBytes;
    }
}
