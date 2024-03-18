package com.ixnah.app.las.jna;

import com.intellij.ide.BytecodeTransformer;
import com.ixnah.app.las.util.LogUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.org.objectweb.asm.ClassReader;
import org.jetbrains.org.objectweb.asm.ClassWriter;
import org.jetbrains.org.objectweb.asm.Opcodes;
import org.jetbrains.org.objectweb.asm.tree.*;

import java.security.ProtectionDomain;
import java.util.Optional;

public class JnaTransformer implements BytecodeTransformer {

    public static final String targetClassName = "com.sun.jna.Native";

    @Override
    public boolean isApplicable(String className, ClassLoader loader, @Nullable ProtectionDomain protectionDomain) {
        return targetClassName.equals(className);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, @Nullable ProtectionDomain protectionDomain, byte[] classBytes) {
        if (targetClassName.equals(className)) {
            ClassReader reader = new ClassReader(classBytes);
            ClassNode node = new ClassNode();
            reader.accept(node, 0);
            Optional<MethodNode> staticInit = node.methods.stream()
                    .filter(methodNode -> "<clinit>".equals(methodNode.name)).findFirst();
            if (staticInit.isPresent()) {
                MethodNode methodNode = staticInit.get();
                InsnList insnList = methodNode.instructions;
                for (AbstractInsnNode insn = insnList.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn instanceof MethodInsnNode methodInsnNode
                            && Opcodes.INVOKESTATIC == methodInsnNode.getOpcode()
                            && "isCompatibleVersion".equals(methodInsnNode.name)
                            && "com/sun/jna/Native".equals(methodInsnNode.owner)) {
                        MethodInsnNode base = (MethodInsnNode) methodInsnNode.getPrevious();
                        insnList.remove(base.getPrevious());
                        insnList.insert(base, base.clone(null));
                        break;
                    }
                }
                ClassWriter writer = new ClassWriter(0);
                node.accept(writer);
                return writer.toByteArray();
            } else {
                LogUtil.e("Find class but not found staticInit Method!!");
            }
        }
        return classBytes;
    }
}
