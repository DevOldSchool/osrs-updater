package org.de.deobfuscation;

import org.de.Deobfuscator;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class MultiplierRemover extends Deobfuscator {
    @Override
    public void onCompletion() {
        label = "multipliers";
        super.onCompletion();
    }

    @Override
    public void execute(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            for (MethodNode methodNode : classNode.methods) {
                for (AbstractInsnNode insn : methodNode.instructions.toArray()) {
                    if (insn instanceof LdcInsnNode ldcInsnNode) {
                        if (ldcInsnNode.cst instanceof Integer intCst) {
                            if (intCst > 100000 || intCst < -100000) {
                                methodNode.instructions.remove(ldcInsnNode);
                                removed++;
                            }
                        }

                        if (ldcInsnNode.cst instanceof Long longCst) {
                            if (longCst > 100000 || longCst < -100000) {
                                methodNode.instructions.remove(ldcInsnNode);
                                removed++;
                            }
                        }
                    }
                }
            }
        }
    }
}
