package org.de.utilities;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InsnMatcher {
    private final Pattern regex;
    private static final Map<String, int[]> OPCODE_GROUPS;

    private InsnMatcher(Pattern regex) {
        this.regex = regex;
    }

    public List<List<AbstractInsnNode>> match(MethodNode method) {
        return match(method.instructions);
    }

    public List<List<AbstractInsnNode>> match(InsnList list) {
        List<AbstractInsnNode> insns = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        for (AbstractInsnNode insn : list) {
            if (insn.getOpcode() != -1) {
                insns.add(insn);
                builder.append(opcodeToCodepoint(insn.getOpcode()));
            }
        }

        Matcher matcher = regex.matcher(builder);
        List<List<AbstractInsnNode>> matches = new ArrayList<>();
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            matches.add(insns.subList(start, end));
        }

        return matches;
    }

    private static char opcodeToCodepoint(int opcode) {
        return (char) (PRIVATE_USE_AREA + opcode);
    }

    private static void appendOpcodeRegex(StringBuilder pattern, String opcode) {
        try {
            int i = (int) Opcodes.class.getDeclaredField(opcode).get(null);
            if (i != -1) {
                pattern.append(opcodeToCodepoint(i));
                return;
            }
        } catch (Exception ignored) {
        }

        int[] group = OPCODE_GROUPS.get(opcode);
        if (group != null) {
            pattern.append('(');
            for (int op : group) {
                pattern.append(opcodeToCodepoint(op)).append('|');
            }
            pattern.setCharAt(pattern.length() - 1, ')');
            return;
        }
        if (opcode.equals("AbstractInsnNode")) {
            pattern.append('.');
            return;
        }
        throw new IllegalArgumentException(opcode + " is not a valid opcode or opcode group");
    }

    public static InsnMatcher compile(String regex) {
        StringBuilder pattern = new StringBuilder();
        StringBuilder opcode = new StringBuilder();
        for (char c : regex.toCharArray()) {
            if (Character.isLetterOrDigit(c) || c == '_') {
                opcode.append(c);
            } else {
                if (!opcode.isEmpty()) {
                    appendOpcodeRegex(pattern, opcode.toString());
                    opcode.delete(0, opcode.length());
                }
                if (!Character.isWhitespace(c)) {
                    pattern.append(c);
                }
            }
        }
        if (!opcode.isEmpty()) {
            appendOpcodeRegex(pattern, opcode.toString());
            opcode.delete(0, opcode.length());
        }

        return new InsnMatcher(Pattern.compile(pattern.toString()));
    }

    private static final int PRIVATE_USE_AREA = 0xE000;

    static {
        OPCODE_GROUPS = new HashMap<>();
        OPCODE_GROUPS.put("InsnNode", new int[]{
                Opcodes.NOP, Opcodes.ACONST_NULL, Opcodes.ICONST_M1, Opcodes.ICONST_0,
                Opcodes.ICONST_1, Opcodes.ICONST_2, Opcodes.ICONST_3, Opcodes.ICONST_4,
                Opcodes.ICONST_5, Opcodes.LCONST_0, Opcodes.LCONST_1, Opcodes.FCONST_0,
                Opcodes.FCONST_1, Opcodes.FCONST_2, Opcodes.DCONST_0, Opcodes.DCONST_1,
                Opcodes.IALOAD, Opcodes.LALOAD, Opcodes.FALOAD, Opcodes.DALOAD, Opcodes.AALOAD,
                Opcodes.BALOAD, Opcodes.CALOAD, Opcodes.SALOAD, Opcodes.IASTORE, Opcodes.LASTORE,
                Opcodes.FASTORE, Opcodes.DASTORE, Opcodes.AASTORE, Opcodes.BASTORE, Opcodes.CASTORE,
                Opcodes.SASTORE, Opcodes.POP, Opcodes.POP2, Opcodes.DUP, Opcodes.DUP_X1, Opcodes.DUP_X2,
                Opcodes.DUP2, Opcodes.DUP2_X1, Opcodes.DUP2_X2, Opcodes.SWAP, Opcodes.IADD, Opcodes.LADD,
                Opcodes.FADD, Opcodes.DADD, Opcodes.ISUB, Opcodes.LSUB, Opcodes.FSUB, Opcodes.DSUB,
                Opcodes.IMUL, Opcodes.LMUL, Opcodes.FMUL, Opcodes.DMUL, Opcodes.IDIV, Opcodes.LDIV,
                Opcodes.FDIV, Opcodes.DDIV, Opcodes.IREM, Opcodes.LREM, Opcodes.FREM, Opcodes.DREM,
                Opcodes.INEG, Opcodes.LNEG, Opcodes.FNEG, Opcodes.DNEG, Opcodes.ISHL, Opcodes.LSHL,
                Opcodes.ISHR, Opcodes.LSHR, Opcodes.IUSHR, Opcodes.LUSHR, Opcodes.IAND, Opcodes.LAND,
                Opcodes.IOR, Opcodes.LOR, Opcodes.IXOR, Opcodes.LXOR, Opcodes.I2L, Opcodes.I2F,
                Opcodes.I2D, Opcodes.L2I, Opcodes.L2F, Opcodes.L2D, Opcodes.F2I, Opcodes.F2L,
                Opcodes.F2D, Opcodes.D2I, Opcodes.D2L, Opcodes.D2F, Opcodes.I2B, Opcodes.I2C,
                Opcodes.I2S, Opcodes.LCMP, Opcodes.FCMPL, Opcodes.FCMPG, Opcodes.DCMPL, Opcodes.DCMPG,
                Opcodes.IRETURN, Opcodes.LRETURN, Opcodes.FRETURN, Opcodes.DRETURN, Opcodes.ARETURN,
                Opcodes.RETURN, Opcodes.ARRAYLENGTH, Opcodes.ATHROW, Opcodes.MONITORENTER, Opcodes.MONITOREXIT
        });
        OPCODE_GROUPS.put("IntInsnNode", new int[]{
                Opcodes.BIPUSH, Opcodes.SIPUSH, Opcodes.NEWARRAY
        });
        OPCODE_GROUPS.put("VarInsnNode", new int[]{
                Opcodes.ILOAD, Opcodes.LLOAD, Opcodes.FLOAD, Opcodes.DLOAD, Opcodes.ALOAD,
                Opcodes.ISTORE, Opcodes.LSTORE, Opcodes.FSTORE, Opcodes.DSTORE, Opcodes.ASTORE,
                Opcodes.RET
        });
        OPCODE_GROUPS.put("TypeInsnNode", new int[]{
                Opcodes.NEW, Opcodes.ANEWARRAY, Opcodes.CHECKCAST, Opcodes.INSTANCEOF
        });
        OPCODE_GROUPS.put("FieldInsnNode", new int[]{
                Opcodes.GETSTATIC, Opcodes.PUTSTATIC, Opcodes.GETFIELD, Opcodes.PUTFIELD
        });
        OPCODE_GROUPS.put("MethodInsnNode", new int[]{
                Opcodes.INVOKEVIRTUAL, Opcodes.INVOKESPECIAL, Opcodes.INVOKESTATIC, Opcodes.INVOKEINTERFACE
        });
        OPCODE_GROUPS.put("InvokeDynamicInsnNode", new int[]{
                Opcodes.INVOKEDYNAMIC
        });
        OPCODE_GROUPS.put("JumpInsnNode", new int[]{
                Opcodes.IFEQ, Opcodes.IFNE, Opcodes.IFLT, Opcodes.IFGE, Opcodes.IFGT, Opcodes.IFLE,
                Opcodes.IF_ICMPEQ, Opcodes.IF_ICMPNE, Opcodes.IF_ICMPLT, Opcodes.IF_ICMPGE,
                Opcodes.IF_ICMPGT, Opcodes.IF_ICMPLE, Opcodes.IF_ACMPEQ, Opcodes.IF_ACMPNE,
                Opcodes.GOTO, Opcodes.JSR, Opcodes.IFNULL, Opcodes.IFNONNULL
        });
        OPCODE_GROUPS.put("LdcInsnNode", new int[]{
                Opcodes.LDC
        });
        OPCODE_GROUPS.put("IincInsnNode", new int[]{
                Opcodes.IINC
        });
        OPCODE_GROUPS.put("TableSwitchInsnNode", new int[]{
                Opcodes.TABLESWITCH
        });
        OPCODE_GROUPS.put("LookupSwitchInsnNode", new int[]{
                Opcodes.LOOKUPSWITCH
        });
        OPCODE_GROUPS.put("MultiANewArrayInsnNode", new int[]{
                Opcodes.MULTIANEWARRAY
        });
        OPCODE_GROUPS.put("ICONST", new int[]{
                Opcodes.ICONST_M1, Opcodes.ICONST_0, Opcodes.ICONST_1, Opcodes.ICONST_2,
                Opcodes.ICONST_3, Opcodes.ICONST_4, Opcodes.ICONST_5
        });
        OPCODE_GROUPS.put("FCONST", new int[]{
                Opcodes.FCONST_0, Opcodes.FCONST_1, Opcodes.FCONST_2
        });
        OPCODE_GROUPS.put("DCONST", new int[]{
                Opcodes.DCONST_0, Opcodes.DCONST_1
        });
    }
}

