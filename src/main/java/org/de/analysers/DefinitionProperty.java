package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

public class DefinitionProperty extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 2;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            if (!classNode.superName.equals(getClassAnalyser("EntityNode").getNode().name)) {
                continue;
            }

            int boolCount = 0;
            int typeCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isStatic(fieldNode.access)) {
                    continue;
                }

                if (fieldNode.desc.equals("Z")) {
                    boolCount++;
                } else if (fieldNode.desc.equals("C")) {
                    typeCount++;
                }
            }

            if (boolCount != 1 || typeCount != 1) {
                continue;
            }

            return classNode;
        }
        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, NEW, DUP, BIPUSH, INVOKESPECIAL, PUTSTATIC);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    IntInsnNode intInsnNode = (IntInsnNode) abstractInsnNodes[2];
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[4];

                    if (intInsnNode.operand == 64 && fieldInsnNode.owner.equals(classNode.name) &&
                            fieldInsnNode.desc.equals(String.format("L%s;", getClassAnalyser("Cache").getNode().name))) {
                        addField("getCache()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ILOAD, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[2];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("C")) {
                        addField("getType()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}
