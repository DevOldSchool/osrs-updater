package org.de.deobfuscation;

import org.de.Deobfuscator;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import java.util.List;

public class DecomplierTrap extends Deobfuscator {
    @Override
    public void onCompletion() {
        label = "decomplier traps";
        super.onCompletion();
    }

    @Override
    public void execute(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            for (MethodNode method : classNode.methods) {
                if (hasIncompleteTryCatchBlocks(method)) {
                    // Add NOP instruction at the end of the method
                    method.instructions.add(new InsnNode(Opcodes.NOP));
                    removed++;
                }
            }
        }
    }

    private boolean hasIncompleteTryCatchBlocks(MethodNode method) {
        for (TryCatchBlockNode tryCatchBlock : method.tryCatchBlocks) {
            if (tryCatchBlock.end == null || tryCatchBlock.end.getNext() == null) {
                // The end of try-catch block is null or points to the end of method
                return true;
            }
        }
        return false;
    }
}
