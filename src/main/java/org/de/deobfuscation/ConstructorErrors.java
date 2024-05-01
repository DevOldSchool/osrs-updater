package org.de.deobfuscation;

import org.de.Deobfuscator;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

public class ConstructorErrors extends Deobfuscator implements Opcodes {
    @Override
    public void onCompletion() {
        label = "constructor errors";
        super.onCompletion();
    }

    @Override
    public void execute(List<ClassNode> classes) {
        for (ClassNode cls : classes) {
            List<MethodNode> toRemove = new ArrayList<>();
            for (MethodNode methodNode : cls.methods) {
                if (methodNode.name.equals("<init>")) {
                    InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, NEW, DUP, INVOKESPECIAL, ATHROW);
                    if (instructionSearcher.match()) {
                        for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                            if (checkPattern(abstractInsnNodes)) {
                                toRemove.add(methodNode);
                                removed++;
                                break;
                            }
                        }
                    }
                }
            }

            cls.methods.removeAll(toRemove);
        }
    }

    private boolean checkPattern(AbstractInsnNode[] insns) {
        if (insns.length < 3) {
            return false;
        }

        AbstractInsnNode invoke = insns[2];
        if (!(invoke instanceof MethodInsnNode methodInsn)) {
            return false;
        }

        return methodInsn.owner.equals("java/lang/Error") && methodInsn.name.equals("<init>") && methodInsn.desc.equals("()V");
    }
}
