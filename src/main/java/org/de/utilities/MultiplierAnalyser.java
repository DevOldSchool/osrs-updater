package org.de.utilities;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class MultiplierAnalyser implements Opcodes {
    private static final String[] PATTERNS = {
            "(ldc|getstatic|aload) (aload|getfield|getstatic|ldc|invokevirtual) (imul|putstatic|getfield|ldc) (imul)?",
            "(ldc|getstatic|aload) (aload|getfield|getstatic|ldc|invokevirtual) (getfield|ldc) imul putstatic"
    };

    public static void findMultipliers(List<org.objectweb.asm.tree.ClassNode> classes) {
        for (org.objectweb.asm.tree.ClassNode classNode : classes) {
            List<MethodNode> methods = classNode.methods;
            for (MethodNode mn : methods) {
                RegexInsnSearcher searcher = new RegexInsnSearcher(mn.instructions);

                for (String pattern : PATTERNS) {
                    List<AbstractInsnNode[]> matches = searcher.search(pattern);

                    for (AbstractInsnNode[] match : matches) {
                        Integer value = null;
                        Integer refHash = null;

                        for (AbstractInsnNode insn : match) {
                            if (insn.getOpcode() == LDC) {
                                try {
                                    value = (Integer) ((LdcInsnNode) insn).cst;
                                } catch (ClassCastException cce) {
                                    break;
                                }
                            }

                            if (insn.getOpcode() == GETFIELD || insn.getOpcode() == GETSTATIC) {
                                assert insn instanceof FieldInsnNode;
                                FieldInsnNode fieldInsn = ((FieldInsnNode) insn);
                                refHash = Multipliers.getHash(fieldInsn.owner, fieldInsn.name);
                            }

                            if (insn.getOpcode() == PUTSTATIC) {
                                assert insn instanceof FieldInsnNode;
                                FieldInsnNode fieldInsn = ((FieldInsnNode) insn);
                                refHash = Multipliers.getHash(fieldInsn.owner, fieldInsn.name);
                            }
                        }

                        if (refHash != null && value != null) {
                            Multipliers.put(refHash, value);
                        }
                    }
                }
            }
        }
    }
}
