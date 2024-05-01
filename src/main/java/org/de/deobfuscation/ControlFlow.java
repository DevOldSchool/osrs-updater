package org.de.deobfuscation;

import org.de.Deobfuscator;
import org.objectweb.asm.tree.*;

import java.util.List;

public class ControlFlow extends Deobfuscator {
    private int removedJumps = 0;
    private int removedLabels = 0;

    @Override
    public void onCompletion() {
        System.out.printf("Deobfuscation: Removed %d jumps and %d labels in %s ms\n",
                removedJumps,
                removedLabels,
                (System.currentTimeMillis() - begin));
    }

    @Override
    public void execute(List<ClassNode> classes) {
        // Remove unused methods on shipped classes (Ignores vendor files)
        for (ClassNode classNode : classes) {
            for (MethodNode methodNode : classNode.methods) {
                deobfuscateMethodControlFlow(methodNode);
            }
        }
    }

    public void deobfuscateMethodControlFlow(MethodNode methodNode) {
        // List to track new instructions after deobfuscation
        InsnList newInstructions = new InsnList();

        // Iterate through the instructions of the method
        AbstractInsnNode currentNode = methodNode.instructions.getFirst();
        while (currentNode != null) {
            AbstractInsnNode nextNode = currentNode.getNext();

            // Handle jump instructions
            if (currentNode instanceof JumpInsnNode jumpInsnNode) {
                LabelNode targetLabel = jumpInsnNode.label;

                // If the jump is redundant or unnecessary, remove the jump and the target label
                if (isRedundantJump(jumpInsnNode, targetLabel)) {
                    // Remove the jump instruction
                    methodNode.instructions.remove(currentNode);
                    removedJumps++;

                    // Remove the target label if it is unreferenced
                    if (!isLabelReferenced(targetLabel, methodNode)) {
                        methodNode.instructions.remove(targetLabel);
                        removedLabels++;
                    }

                    // Move to the next instruction
                    currentNode = nextNode;
                    continue;
                }
            }

            // Add the current instruction to newInstructions
            newInstructions.add(currentNode);

            // Move to the next instruction
            currentNode = nextNode;
        }

        // Replace old instructions with the new instructions
        methodNode.instructions = newInstructions;
    }

    /**
     * Checks if the given label is referenced in the method.
     *
     * @param labelNode  The label node to check.
     * @param methodNode The method to check within.
     * @return True if the label is referenced, false otherwise.
     */
    private boolean isLabelReferenced(LabelNode labelNode, MethodNode methodNode) {
        // Check if the label is referenced in jump instructions
        for (AbstractInsnNode node : methodNode.instructions) {
            if (node instanceof JumpInsnNode jumpInsn) {
                if (jumpInsn.label == labelNode) {
                    return true;
                }
            } else if (node instanceof TableSwitchInsnNode tableSwitchInsn) {
                if (tableSwitchInsn.labels.contains(labelNode) || tableSwitchInsn.dflt == labelNode) {
                    return true;
                }
            } else if (node instanceof LookupSwitchInsnNode lookupSwitchInsn) {
                if (lookupSwitchInsn.labels.contains(labelNode) || lookupSwitchInsn.dflt == labelNode) {
                    return true;
                }
            } else if (node instanceof LineNumberNode lineNumberNode) {
                // Check if the label node matches the label in the line number node
                if (lineNumberNode.start == labelNode) {
                    return true;
                }
            } else if (node instanceof FrameNode) {
                // Check if the label node matches the label in the frame node
                // Since FrameNode doesn't directly reference labels,
                // but is associated with labels, we need to check the previous node
                // for the associated label.
                AbstractInsnNode previousNode = node.getPrevious();
                if (previousNode instanceof LabelNode previousLabelNode && previousLabelNode == labelNode) {
                    return true;
                }
            }
        }

        // Check if the label is referenced in exception handler blocks (try-catch blocks)
        for (TryCatchBlockNode tryCatchBlock : methodNode.tryCatchBlocks) {
            if (tryCatchBlock.start == labelNode || tryCatchBlock.end == labelNode || tryCatchBlock.handler == labelNode) {
                return true;
            }
        }

        // If the label is not referenced in any context, return false
        return false;
    }

    /**
     * Check if a jump instruction is redundant.
     *
     * @param jumpInsnNode The jump instruction.
     * @param targetLabel  The target label of the jump.
     * @return True if the jump is redundant, false otherwise.
     */
    private boolean isRedundantJump(AbstractInsnNode jumpInsnNode, LabelNode targetLabel) {
        // Check if the current jump target is redundant
        if (jumpInsnNode == null) {
            return false;
        }

        // Check if the jump instruction is a jump to the immediate next instruction
        AbstractInsnNode nextInsn = jumpInsnNode.getNext();

        // If the jump target is the immediate next instruction, it's redundant
        if (nextInsn == targetLabel) {
            return true;
        }

        // Check if the jump is part of a nested control flow structure
        // where it serves a purpose in looping or branching.
        // This check can be more complex depending on your specific use case.
        // For instance, you could iterate through instructions to identify
        // whether there are other jumps leading to the same target.

        // This is a simplified approach: in practice, you might want to handle more specific cases.
        AbstractInsnNode currentInsn = jumpInsnNode;
        while (currentInsn != null && currentInsn != targetLabel) {
            if (currentInsn instanceof JumpInsnNode nextJump) {
                // If there's a jump leading to the same target, it might be redundant
                if (nextJump.label == targetLabel) {
                    return true;
                }
            }
            currentInsn = currentInsn.getNext();
        }

        // If none of the conditions for redundancy were met, the jump is not redundant
        return false;
    }
}