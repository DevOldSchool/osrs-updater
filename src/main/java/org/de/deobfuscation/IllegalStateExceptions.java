package org.de.deobfuscation;

import org.de.Deobfuscator;
import org.de.utilities.InsnMatcher;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

public class IllegalStateExceptions extends Deobfuscator implements Opcodes {
    private static final String EXCEPTION_PATTERN = """
                (ILOAD)
                ([ICONST_0-LDC])
                ([IF_ICMPEQ-IF_ACMPNE])
                (NEW)
                (DUP)
                (INVOKESPECIAL)
                (ATHROW)
            """;
    private static final String RETURN_PATTERN = """
                (ILOAD)
                ([ICONST_0-LDC])
                ([IF_ICMPEQ-IF_ACMPNE])
                ([IRETURN-RETURN])
            """;

    @Override
    public void onCompletion() {
        System.out.printf("Deobfuscation: Removed %d IllegalStateException opaque checks in %d ms\n", removed, (System.currentTimeMillis() - begin));
    }

    @Override
    public void execute(List<ClassNode> classes) {
        InsnMatcher exceptionMatcher = InsnMatcher.compile(EXCEPTION_PATTERN);
        InsnMatcher returnMatcher = InsnMatcher.compile(RETURN_PATTERN);

        for (ClassNode classNode : classes) {
            for (MethodNode methodNode : classNode.methods) {
                for (List<AbstractInsnNode> matches : exceptionMatcher.match(methodNode)) {
                    if (checkExceptionPattern(methodNode, matches)) {
                        removeMatchedInsns(methodNode, matches);
                    }
                }

                for (List<AbstractInsnNode> matches : returnMatcher.match(methodNode)) {
                    if (checkReturnPattern(methodNode, matches)) {
                        removeMatchedInsns(methodNode, matches);
                    }
                }
            }
        }
    }

    private void removeMatchedInsns(MethodNode method, List<AbstractInsnNode> insns) {
        JumpInsnNode jump = (JumpInsnNode) insns.get(2);
        JumpInsnNode gotoInsn = new JumpInsnNode(Opcodes.GOTO, jump.label);
        method.instructions.insertBefore(insns.getLast(), gotoInsn);

        for (AbstractInsnNode insn : insns) {
            method.instructions.remove(insn);
        }

        removed++;
    }

    private boolean checkExceptionPattern(MethodNode method, List<AbstractInsnNode> insns) {
        VarInsnNode load = (VarInsnNode) insns.get(0);
        AbstractInsnNode cst = insns.get(1);
        TypeInsnNode newInsn = (TypeInsnNode) insns.get(3);

        if (load.var != lastArgIndex(method)) {
            return false;
        }

        return !(cst instanceof LdcInsnNode) || newInsn.desc.equals("java/lang/IllegalStateException");
    }

    private boolean checkFakeExceptionPattern(MethodNode method, List<AbstractInsnNode> insns) {
        TypeInsnNode newInsn = (TypeInsnNode) insns.get(0);

        return newInsn.desc.equals("java/lang/IllegalStateException");
    }

    private boolean checkReturnPattern(MethodNode method, List<AbstractInsnNode> insns) {
        VarInsnNode load = (VarInsnNode) insns.get(0);
        AbstractInsnNode cst = insns.get(1);

        if (load.var != lastArgIndex(method)) {
            return false;
        }

        return cst instanceof LdcInsnNode;
    }

    private int lastArgIndex(MethodNode method) {
        int offset = Modifier.isStatic(method.access) ? 1 : 0;
        return (Type.getArgumentsAndReturnSizes(method.desc) >> 2) - offset - 1;
    }
}
