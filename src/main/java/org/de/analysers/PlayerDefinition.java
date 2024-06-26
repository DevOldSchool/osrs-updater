package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

public class PlayerDefinition extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 3;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 1;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (final ClassNode classNode : classes) {
            for (MethodNode methodNode : classNode.methods) {
                InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ALOAD, PUTFIELD);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[2];

                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals(String.format("[L%s;", getClassAnalyser("AppearanceCustomization").getNode().name))) {
                            return classNode;
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (FieldNode fieldNode : classNode.fields) {
            if (Modifier.isStatic(fieldNode.access)) {
                continue;
            }
            if (fieldNode.desc.equals("Z")) {
                addField("isFemale()", fieldNode);
            }
            if (fieldNode.desc.equals("[I")) {
                addField("getEquipment()", fieldNode);
            }
        }

        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, LMUL, ICONST_4, LSHL, LMUL, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[4];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("J")) {
                        addField("getHash()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.desc.endsWith(String.format(")L%s;", getClassAnalyser("ModelHeader").getNode().name))) {
                addMethod("getModelHeader()", methodNode);
            }
        }
    }
}
