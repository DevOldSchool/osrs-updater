package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

public class Actor extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 8;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            if (!classNode.superName.equals(getClassAnalyser("RenderableNode").getNode().name)) {
                continue;
            }

            for (MethodNode methodNode : classNode.methods) {
                InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ACONST_NULL, PUTFIELD);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[2];

                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("Ljava/lang/String;")) {
//                            System.out.println("Actor class " + classNode.name);
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

            if (fieldNode.desc.contains("String")) {
                addField("getMessage()", fieldNode);
            }

            if (fieldNode.desc.equals("Z")) {
                addField("isAnimating()", fieldNode);
            }

            if (fieldNode.desc.equals(String.format("[L%s;", getClassAnalyser("MovementType").getNode().name))) {
                addField("getMovementTypes()", fieldNode);
            }

            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("NodeList").getNode().name))) {
                addField("getNodeList()", fieldNode);
            }
        }

        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, INVOKESPECIAL, PUTFIELD, -1, -1, ALOAD, PUTFIELD, -1, -1, ALOAD, ICONST_0, PUTFIELD, -1, -1, ALOAD, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[10];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("Z")) {
                        addField("isInteracting()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ICONST_4, NEWARRAY, PUTFIELD, -1, -1, ALOAD, ICONST_4, NEWARRAY, PUTFIELD, -1, -1, ALOAD, ICONST_4, NEWARRAY, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[3];
                    FieldInsnNode fieldInsnNode2 = (FieldInsnNode) abstractInsnNodes[9];
                    FieldInsnNode fieldInsnNode3 = (FieldInsnNode) abstractInsnNodes[15];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("[I") &&
                            fieldInsnNode2.owner.equals(classNode.name) && fieldInsnNode2.desc.equals("[I") &&
                            fieldInsnNode3.owner.equals(classNode.name) && fieldInsnNode3.desc.equals("[I")) {
                        addField("getHitsplatTypes()", insnToField(fieldInsnNode));
                        addField("getHitsplatDamage()", insnToField(fieldInsnNode2));
                        addField("getHitsplatCycles()", insnToField(fieldInsnNode3));
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
