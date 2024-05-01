package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

import static org.de.utilities.Wildcard.wildcard;

public class Actor extends Analyser {
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
        // TODO, nothing in here is stable across versions!
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
        }

        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ICONST_0, PUTFIELD, ALOAD, ICONST_4);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[2];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("Z")) {
                        addField("isInteracting()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ICONST_4, NEWARRAY, PUTFIELD, ALOAD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[3];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("[I")) {
                        addField("getHitsplatTypes()", insnToField(fieldInsnNode));
                        break;
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ICONST_4, NEWARRAY, PUTFIELD, ALOAD, ICONST_0);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[3];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("[I") &&
                            !isDuplicate(insnToField(fieldInsnNode))) {
                        addField("getHitsplatDamage()", insnToField(fieldInsnNode));
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ICONST_4, NEWARRAY, PUTFIELD, ALOAD, ICONST_4);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[3];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("[I")) {
                        addField("getHitsplatCycles()", insnToField(fieldInsnNode));
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ILOAD, I2C, BIPUSH, INVOKESTATIC, ICONST_1);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] insnNodes : instructionSearcher.getMatches()) {
                    MethodInsnNode methodInsnNode = (MethodInsnNode) insnNodes[3];

                    if (wildcard("(*)Z", methodInsnNode.desc)) {
                        String fieldName = methodInsnNode.desc.substring(methodInsnNode.desc.indexOf('(') + 1, methodInsnNode.desc.indexOf(')')).toLowerCase();

                        for (FieldNode fieldNode : classNode.fields) {
                            if (fieldNode.name.equals(fieldName) && fieldNode.desc.equals("I")) {
                                addField("getOrientation()", fieldNode);
                                break;
                            }
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
