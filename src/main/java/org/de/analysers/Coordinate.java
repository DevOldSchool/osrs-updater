package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

public class Coordinate extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 3;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            int intCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isStatic(fieldNode.access)) {
                    continue;
                }

                if (fieldNode.desc.equals("I")) {
                    intCount++;
                }
            }

            if (intCount != 3) {
                continue;
            }

            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.name.equals("<init>") && methodNode.desc.equals(String.format("(L%s;)V", classNode.name))) {
                    return classNode;
                }
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("<init>") && methodNode.desc.equals("(III)V")) {
                InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0,
                        ALOAD, ILOAD, IMUL, PUTFIELD, -1, -1,
                        ALOAD, ILOAD, IMUL, PUTFIELD, -1, -1,
                        ALOAD, ILOAD, IMUL, PUTFIELD);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[3];
                        FieldInsnNode fieldInsnNode2 = (FieldInsnNode) abstractInsnNodes[9];
                        FieldInsnNode fieldInsnNode3 = (FieldInsnNode) abstractInsnNodes[15];

                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I") &&
                                fieldInsnNode2.owner.equals(classNode.name) && fieldInsnNode2.desc.equals("I") &&
                                fieldInsnNode3.owner.equals(classNode.name) && fieldInsnNode3.desc.equals("I")) {
                            addField("getPlane()", insnToField(fieldInsnNode, classNode));
                            addField("getX()", insnToField(fieldInsnNode2, classNode));
                            addField("getY()", insnToField(fieldInsnNode3, classNode));
                            break;
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
