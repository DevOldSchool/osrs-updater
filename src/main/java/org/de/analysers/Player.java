package org.de.analysers;

import org.de.Analyser;
import org.de.Updater;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.util.List;

import static org.de.utilities.Wildcard.wildcard;

public class Player extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 7;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            if (!classNode.superName.equals(getClassAnalyser("Actor").getNode().name)) {
                continue;
            }

            for (FieldNode fieldNode : classNode.fields) {
                if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("Model").getNode().name))) {
                    return classNode;
                }
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (FieldNode fieldNode : classNode.fields) {
            if (fieldNode.access == 0 && fieldNode.desc.contains("String")) {
                addField("getName()", fieldNode);
            }
            if (fieldNode.access == 0 & fieldNode.desc.contains(getClassAnalyser("Model").getNode().name)) {
                addField("getModel()", fieldNode);
            }
        }
        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher insn = new InstructionSearcher(methodNode.instructions, 0, ALOAD, GETFIELD, ICONST_1);
            if (insn.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : insn.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[1];

                    if (fieldInsnNode.owner.equals(classNode.name) && wildcard("L*;", fieldInsnNode.desc)) {
//                        System.out.println("FOUND FIELD " + fieldInsnNode.name + " " + fieldInsnNode.desc);
                        addField("getPlayerDefinition()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }
        }
        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher insn = new InstructionSearcher(methodNode.instructions, 0, ALOAD, GETFIELD, IFNE);
            if (insn.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : insn.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[1];
                    if (fieldInsnNode.desc.equals("Z")) {
                        addField("isVisible()", insnToField(fieldInsnNode));
                    }
                }
            }
        }
        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher insn = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ALOAD, LDC, INVOKEVIRTUAL, LDC, IMUL, PUTFIELD);
            if (insn.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : insn.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[6];
                    if (fieldInsnNode.desc.equals("I")) {
                        addField("getCombatLevel()", insnToField(fieldInsnNode));
                    }
                }
            }
        }
        for (MethodNode methodNode : classNode.methods) {
            if (!wildcard(String.format("(*)L%s;", getClassAnalyser("Model").getNode().name), methodNode.desc)) {
                continue;
            }
            InstructionSearcher insn = new InstructionSearcher(methodNode.instructions, 0, GETFIELD, -1, IF_ICMPGE);
            if (insn.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : insn.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[0];
                    if (fieldInsnNode.desc.equals("I")) {
                        addField("getSkullIcon()", insnToField(fieldInsnNode));
                    }
                }
            }
        }
        for (ClassNode cn : Updater.classes) {
            for (MethodNode methodNode : cn.methods) {
                if (!wildcard("(*)V", methodNode.desc)) {
                    continue;
                }
                InstructionSearcher insn = new InstructionSearcher(methodNode.instructions, 0, GETSTATIC, -1, ALOAD, GETFIELD, -1, AALOAD);
                if (insn.match()) {
                    for (AbstractInsnNode[] abstractInsnNodes : insn.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[3];
                        if (fieldInsnNode != null && fieldInsnNode.desc.equals("I")) {
                            addField("getPrayerIcon()", insnToField(fieldInsnNode));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}
