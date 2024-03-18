package com.ixnah.app.las.gateway;

import com.intellij.ide.BytecodeTransformer;
import com.ixnah.app.las.util.LogUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.org.objectweb.asm.ClassReader;
import org.jetbrains.org.objectweb.asm.ClassWriter;
import org.jetbrains.org.objectweb.asm.Opcodes;
import org.jetbrains.org.objectweb.asm.tree.*;

import java.security.ProtectionDomain;
import java.util.Optional;

public class FixArchTransformer implements BytecodeTransformer {
    static final String targetClassName = "com.jetbrains.gateway.ssh.deploy.ShellFacade$Companion";

    @Override
    public boolean isApplicable(String className, ClassLoader loader, @Nullable ProtectionDomain protectionDomain) {
        return targetClassName.equals(className);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, @Nullable ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (targetClassName.equals(className)) {
            ClassReader reader = new ClassReader(classfileBuffer);
            ClassNode node = new ClassNode();
            reader.accept(node, 0);
            Optional<MethodNode> getDeployTargetOs = node.methods.stream()
                    .filter(methodNode -> "getDeployTargetOs".equals(methodNode.name)).findFirst();
            if (getDeployTargetOs.isPresent()) {
                MethodNode methodNode = getDeployTargetOs.get();
                InsnList insnList = methodNode.instructions;
                for (AbstractInsnNode insn = insnList.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn instanceof FieldInsnNode fieldInsnNode
                            && fieldInsnNode.getOpcode() == Opcodes.GETSTATIC
                            && "com/jetbrains/gateway/ssh/DeployTargetOS$OSArch".equals(fieldInsnNode.owner)
                            && "UNKNOWN".equals(fieldInsnNode.name)) {
                        fieldInsnNode.name = "X86_64";
                        LogUtil.i("FixArchTransformer inject!");
                    }
                }
                ClassWriter writer = new ClassWriter(0);
                node.accept(writer);
                return writer.toByteArray();
            } else {
                LogUtil.e("Find class but not found getDeployTargetOs Method!!");
            }
        }
        return classfileBuffer;
    }
}
