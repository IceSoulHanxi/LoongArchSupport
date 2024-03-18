package com.ixnah.app.las.clangd;

import com.intellij.ide.BytecodeTransformer;
import com.ixnah.app.las.util.LogUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.org.objectweb.asm.ClassReader;
import org.jetbrains.org.objectweb.asm.ClassWriter;
import org.jetbrains.org.objectweb.asm.Opcodes;
import org.jetbrains.org.objectweb.asm.Type;
import org.jetbrains.org.objectweb.asm.tree.*;

import java.security.ProtectionDomain;
import java.util.Map;
import java.util.function.Consumer;

public class ClangdTransformer implements BytecodeTransformer {
    static final Map<String, Consumer<ClassNode>> targetHandler = Map.of(
            "com.jetbrains.cidr.lang.daemon.clang.clangd.lsp.ClangdLanguageServiceHolder",
            node -> node.methods.stream().filter(m -> "startImpl".equals(m.name)).findFirst().ifPresentOrElse(methodNode -> {
                InsnList insnList = methodNode.instructions;
                for (AbstractInsnNode insn = insnList.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn instanceof MethodInsnNode methodInsnNode
                            && Opcodes.INVOKESPECIAL == methodInsnNode.getOpcode()
                            && "com/jetbrains/cidr/lang/daemon/clang/clangd/lsp/ClangDaemonContextImpl$Builder".equals(methodInsnNode.owner)
                            && "<init>".equals(methodInsnNode.name)) {
                        insnList.insert(methodInsnNode, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, methodInsnNode.owner, "setClangdPath", "(Ljava/io/File;)Lcom/jetbrains/cidr/lang/daemon/clang/clangd/lsp/ClangDaemonContextImpl$Builder;", false));
                        insnList.insert(methodInsnNode, new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V"));
                        insnList.insert(methodInsnNode, new LdcInsnNode("/usr/bin/clangd")); // TODO: custom clangd path
                        insnList.insert(methodInsnNode, new InsnNode(Opcodes.DUP));
                        insnList.insert(methodInsnNode, new TypeInsnNode(Opcodes.NEW, "java/io/File"));
                        LogUtil.d("ClangDaemonContextImpl$Builder injected!");
                        break;
                    }
                }
            }, () -> LogUtil.e("Method ClangdLanguageServiceHolder.Builder.startImpl not found")),

            "com.jetbrains.cidr.lang.daemon.clang.clangd.connector.ProcessServerConnectionProvider",
            node -> node.methods.stream().filter(m -> "create".equals(m.name)).findFirst().ifPresentOrElse(methodNode -> {
                InsnList insnList = methodNode.instructions;
                for (AbstractInsnNode insn = insnList.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn instanceof MethodInsnNode methodInsnNode
                            && Opcodes.INVOKEVIRTUAL == methodInsnNode.getOpcode()
                            && "com/jetbrains/cidr/lang/daemon/clang/clangd/connector/ProcessServerConnectionProvider".equals(methodInsnNode.owner)
                            && "buildCommandLine".equals(methodInsnNode.name)) {
                        AbstractInsnNode aLoad0 = methodInsnNode.getPrevious().getPrevious();
                        insnList.insertBefore(aLoad0, new LdcInsnNode("com.ixnah.app.LoongArchSupport"));
                        insnList.insertBefore(aLoad0, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/intellij/openapi/extensions/PluginId", "getId", "(Ljava/lang/String;)Lcom/intellij/openapi/extensions/PluginId;", false));
                        insnList.insertBefore(aLoad0, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/intellij/ide/plugins/PluginManagerCore", "getPlugin", "(Lcom/intellij/openapi/extensions/PluginId;)Lcom/intellij/ide/plugins/IdeaPluginDescriptor;", false));
                        insnList.insertBefore(aLoad0, new MethodInsnNode(Opcodes.INVOKEINTERFACE, "com/intellij/ide/plugins/IdeaPluginDescriptor", "getClassLoader", "()Ljava/lang/ClassLoader;", true));
                        insnList.insertBefore(aLoad0, new LdcInsnNode("com.ixnah.app.las.clangd.ClangdSupport"));
                        insnList.insertBefore(aLoad0, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/ClassLoader", "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;", false));
                        insnList.insertBefore(aLoad0, new LdcInsnNode("processCmdArgs"));
                        insnList.insertBefore(aLoad0, new InsnNode(Opcodes.ICONST_2));
                        insnList.insertBefore(aLoad0, new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Class"));
                        insnList.insertBefore(aLoad0, new InsnNode(Opcodes.DUP));
                        insnList.insertBefore(aLoad0, new InsnNode(Opcodes.ICONST_0));
                        insnList.insertBefore(aLoad0, new LdcInsnNode(Type.getType("Ljava/lang/Object;")));
                        insnList.insertBefore(aLoad0, new InsnNode(Opcodes.AASTORE));
                        insnList.insertBefore(aLoad0, new InsnNode(Opcodes.DUP));
                        insnList.insertBefore(aLoad0, new InsnNode(Opcodes.ICONST_1));
                        insnList.insertBefore(aLoad0, new LdcInsnNode(Type.getType("Lcom/intellij/execution/configurations/GeneralCommandLine;")));
                        insnList.insertBefore(aLoad0, new InsnNode(Opcodes.AASTORE));
                        insnList.insertBefore(aLoad0, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false));
                        insnList.insertBefore(aLoad0, new InsnNode(Opcodes.ACONST_NULL));
                        insnList.insertBefore(aLoad0, new InsnNode(Opcodes.ICONST_2));
                        insnList.insertBefore(aLoad0, new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"));
                        insnList.insertBefore(aLoad0, new InsnNode(Opcodes.DUP));
                        insnList.insertBefore(aLoad0, new InsnNode(Opcodes.ICONST_0));
                        insnList.insertBefore(aLoad0, new VarInsnNode(Opcodes.ALOAD, 1));
                        insnList.insertBefore(aLoad0, new InsnNode(Opcodes.AASTORE));
                        insnList.insertBefore(aLoad0, new InsnNode(Opcodes.DUP));
                        insnList.insertBefore(aLoad0, new InsnNode(Opcodes.ICONST_1));
                        // aload 0
                        // aload 1
                        // INVOKEVIRTUAL com/jetbrains/cidr/lang/daemon/clang/clangd/connector/ProcessServerConnectionProvider.buildCommandLine (Lcom/jetbrains/cidr/lang/daemon/clang/clangd/lsp/ClangDaemonContext;)Lcom/intellij/execution/configurations/GeneralCommandLine;
                        insnList.insert(methodInsnNode, new TypeInsnNode(Opcodes.CHECKCAST, "com/intellij/execution/configurations/GeneralCommandLine"));
                        insnList.insert(methodInsnNode, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Method", "invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;", false));
                        insnList.insert(methodInsnNode, new InsnNode(Opcodes.AASTORE));
                        break;
                    }
                }
            }, () -> LogUtil.e("Method ProcessServerConnectionProvider.create not found")),

            "com.jetbrains.cidr.lang.daemon.clang.clangd.lsp.server.ClangServerAccessorImpl$Session",
            node -> node.methods.stream().filter(m -> "onInitializeResult".equals(m.name)).findFirst().ifPresentOrElse(methodNode -> {
                InsnList insnList = methodNode.instructions;
                for (AbstractInsnNode insn = insnList.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn instanceof MethodInsnNode methodInsnNode
                            && Opcodes.INVOKESTATIC == methodInsnNode.getOpcode()
                            && "com/jetbrains/cidr/lang/daemon/clang/clangd/lsp/server/ClangServerAccessorImpl$Session".equals(methodInsnNode.owner)
                            && "checkDaemonVersion".equals(methodInsnNode.name)) {
                        insnList.insert(insn, new InsnNode(Opcodes.ACONST_NULL));
                        insnList.remove(insn.getPrevious().getPrevious());
                        insnList.remove(insn.getPrevious());
                        insnList.remove(insn);
                        break;
                    }
                }
            }, () -> LogUtil.e("ClangServerAccessorImpl$Session.onInitializeResult not found!")),

//            "com.jetbrains.cidr.lang.daemon.clang.clangd.lsp.server.ClangServerAdapter",
//            node -> node.methods.stream().filter(m -> "<init>".equals(m.name)).findFirst().ifPresentOrElse(methodNode -> {
//                InsnList insnList = methodNode.instructions;
//                for (AbstractInsnNode insn = insnList.getFirst(); insn != null; insn = insn.getNext()) {
//                    if (insn instanceof InsnNode insnNode
//                            && Opcodes.RETURN == insnNode.getOpcode()) {
//                        insnList.insertBefore(insnNode, new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
//                        insnList.insertBefore(insnNode, new VarInsnNode(Opcodes.ALOAD, 1));
//                        insnList.insertBefore(insnNode, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false));
//                        break;
//                    }
//                }
//            }, () -> LogUtil.e("ClangServerAdapter.init not found!")),

            "com.jetbrains.cidr.lang.daemon.clang.clangd.lsp.server.ClangClientServerProviderImpl$ClangServerProtector",
            node -> node.methods.stream().filter(m -> "invoke".equals(m.name)).findFirst().ifPresentOrElse(methodNode -> {
                InsnList insnList = methodNode.instructions;
                for (AbstractInsnNode insn = insnList.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn instanceof MethodInsnNode methodInsnNode
                            && Opcodes.INVOKEVIRTUAL == methodInsnNode.getOpcode()
                            && "java/lang/reflect/Method".equals(methodInsnNode.owner)
                            && "invoke".equals(methodInsnNode.name)) {
                        AbstractInsnNode injectPoint = insn.getPrevious().getPrevious().getPrevious();
                        insnList.insertBefore(injectPoint, new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
                        insnList.insertBefore(injectPoint, new VarInsnNode(Opcodes.ALOAD, 4));
                        insnList.insertBefore(injectPoint, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false));
                        break;
                    }
                }
            }, () -> LogUtil.e("ClangClientServerProviderImpl$ClangServerProtector.invoke not found!"))
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
