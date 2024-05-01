package org.de.deobfuscation;

import org.de.Deobfuscator;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.List;

public class GetPaths extends Deobfuscator {
    @Override
    public void onCompletion() {
        System.out.printf("Deobfuscation: Fixed %d invalid getPath() method calls in %d ms\n", removed, (System.currentTimeMillis() - begin));
    }

    @Override
    public void execute(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            for (MethodNode method : classNode.methods) {
                InsnList insns = method.instructions;
                int seen = 0;

                for (AbstractInsnNode insn : method.instructions) {
                    if (!(insn instanceof MethodInsnNode methodInsn)) {
                        continue;
                    }
                    if (!methodInsn.name.equals("getPath")) {
                        continue;
                    }

                    if (++seen == 2) {
                        InsnList newInstructions = new InsnList();
                        newInstructions.add(new InsnNode(Opcodes.POP));
                        newInstructions.add(new LdcInsnNode(""));
                        insns.insertBefore(insn, newInstructions);
                        insns.remove(insn);
                        removed++;
                        break;
                    }
                }
            }
        }
    }
}
