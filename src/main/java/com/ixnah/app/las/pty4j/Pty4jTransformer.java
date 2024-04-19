package com.ixnah.app.las.pty4j;

import com.intellij.ide.BytecodeTransformer;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.org.objectweb.asm.ClassReader;
import org.jetbrains.org.objectweb.asm.ClassWriter;
import org.jetbrains.org.objectweb.asm.Opcodes;
import org.jetbrains.org.objectweb.asm.tree.*;

import java.security.ProtectionDomain;
import java.util.Map;
import java.util.function.Consumer;

public class Pty4jTransformer implements BytecodeTransformer {

    static final Map<String, Consumer<ClassNode>> targetHandler = Map.of(
            "com.pty4j.unix.linux.OSFacadeImpl",
            node -> node.methods.forEach(methodNode -> {
                switch (methodNode.name) {
                    case "<clinit>": {
                        // 删除libutil初始化代码
                        InsnList insnList = methodNode.instructions;
                        boolean start = false;
                        for (AbstractInsnNode insn = insnList.getFirst(); insn != null; insn = insn.getNext()) {
                            if (!start && insn instanceof LdcInsnNode ldcInsnNode && "util".equals(ldcInsnNode.cst)) {
                                start = true;
                                continue;
                            }
                            if (start) insnList.remove(insn.getPrevious());
                            if (insn instanceof InsnNode insnNode && Opcodes.RETURN == insnNode.getOpcode()) break;
                        }
                        break;
                    }
                    case "login_tty": {
                        // 将调用libutil改为调用libc
                        InsnList insnList = methodNode.instructions;
                        for (AbstractInsnNode insn = insnList.getFirst(); insn != null; insn = insn.getNext()) {
                            if (insn instanceof FieldInsnNode fieldInsnNode && "m_Utillib".equals(fieldInsnNode.name)) {
                                fieldInsnNode.name = "m_Clib";
                                fieldInsnNode.desc = "Lcom/pty4j/unix/linux/OSFacadeImpl$C_lib;";
                            }
                            if (insn instanceof MethodInsnNode methodInsnNode && "login_tty".equals(methodInsnNode.name)) {
                                methodInsnNode.owner = "com/pty4j/unix/linux/OSFacadeImpl$C_lib";
                            }
                        }
                        break;
                    }
                }
            }),

            "com.pty4j.unix.linux.OSFacadeImpl$C_lib", // 在libc接口中添加login_tty
            node -> node.methods.add(new MethodNode(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT, "login_tty", "(I)I", null, null))
    );

    @Override
    public boolean isApplicable(String className, ClassLoader loader, @Nullable ProtectionDomain protectionDomain) {
        return targetHandler.containsKey(className);
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, @Nullable ProtectionDomain protectionDomain, byte[] classBytes) {
        if (targetHandler.containsKey(className)) {
            ClassReader reader = new ClassReader(classBytes);
            ClassNode node = new ClassNode();
            reader.accept(node, 0);
            targetHandler.get(className).accept(node);
            ClassWriter writer = new ClassWriter(0);
            node.accept(writer);
            return writer.toByteArray();
        }
        return classBytes;
    }
}
