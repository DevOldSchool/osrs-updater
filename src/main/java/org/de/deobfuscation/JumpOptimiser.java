package org.de.deobfuscation;

import org.de.Deobfuscator;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class JumpOptimiser extends Deobfuscator {
    @Override
    public void onCompletion() {
        System.out.printf("Deobfuscation: Removed %d redundant GOTO jumps in %d ms\n", removed, (System.currentTimeMillis() - begin));
    }

    @Override
    public void execute(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            for (MethodNode methodNode : classNode.methods) {
                for (AbstractInsnNode insnNode : methodNode.instructions) {
                    if (insnNode.getOpcode() == Opcodes.GOTO) {
                        JumpInsnNode jumpInsn = (JumpInsnNode) insnNode;

                        if (getNextReal(insnNode) == getNextReal(jumpInsn.label)) {
                            methodNode.instructions.remove(jumpInsn);
                            removed++;
                        }
                    }
                }
            }
        }
    }

    private AbstractInsnNode getNextReal(AbstractInsnNode insn) {
        AbstractInsnNode next = insn.getNext();
        while (next != null && next.getOpcode() == -1) {
            next = next.getNext();
        }

        return next;
    }
}
