package com.ixnah.app.las.transform.debug;

import com.intellij.ide.BytecodeTransformer;
import com.ixnah.app.las.util.LogUtil;
import org.jetbrains.annotations.Nullable;

import java.security.ProtectionDomain;

public class PrintClassNameTransformer implements BytecodeTransformer {
    @Override
    public boolean isApplicable(String className, ClassLoader loader, @Nullable ProtectionDomain protectionDomain) {
        LogUtil.d(className);
        return false;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, @Nullable ProtectionDomain protectionDomain, byte[] classBytes) {
        return classBytes;
    }
}
