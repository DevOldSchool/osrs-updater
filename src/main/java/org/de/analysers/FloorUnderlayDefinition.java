package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

public class FloorUnderlayDefinition extends Analyser {
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
            if (!classNode.superName.equals(getClassAnalyser("DualNode").getNode().name)) {
                continue;
            }

            int cacheCount = 0;
            int abstractArchiveCount = 0;
            int intCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("Cache").getNode().name))) {
                    cacheCount++;
                } else if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("AbstractArchive").getNode().name))) {
                    abstractArchiveCount++;
                } else if (!Modifier.isStatic(fieldNode.access) && fieldNode.desc.equals("I")) {
                    intCount++;
                }
            }

            if (cacheCount != 1 || intCount != 5) {
                continue;
            }

            return classNode;
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (FieldNode fieldNode : classNode.fields) {
            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("Cache").getNode().name))) {
                addField("getCache()", fieldNode);
                break;
            }
        }

        InstructionSearcher instructionSearcher;
        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("<init>")) {
                instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ICONST_0, PUTFIELD);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[2];

                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                            addField("getRgbColor()", insnToField(fieldInsnNode, classNode));
                            break;
                        }
                    }
                }
            }

//            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, GETFIELD, IMUL, ALOAD, ICONST_0, PUTFIELD);
//            if (instructionSearcher.match()) {
//                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
//                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[4];
//
//                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
//                        addField("getSaturation()", insnToField(fieldInsnNode, classNode));
//                        break;
//                    }
//                }
//            }
//
//            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, DCONST_1, DLOAD, DSUB, DLOAD, DMUL, LDC, DMUL, D2I, IMUL, PUTFIELD);
//            if (instructionSearcher.match()) {
//                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
//                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[9];
//
//                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
//                        addField("getHueMultiplier()", insnToField(fieldInsnNode, classNode));
//                        break;
//                    }
//                }
//            }
//
//            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, GETFIELD, IMUL, SIPUSH, ALOAD, PUTFIELD);
//            if (instructionSearcher.match()) {
//                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
//                    IntInsnNode intInsnNode = (IntInsnNode) abstractInsnNodes[3];
//                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[5];
//
//                    if (intInsnNode.operand == 255 && fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I") &&
//                            !fieldInsnNode.name.equals(getField("getSaturation()").getField().name)) {
//                        addField("getLightness()", insnToField(fieldInsnNode, classNode));
//                        break;
//                    }
//                }
//            }
//
//            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, D2I, IMUL, PUTFIELD);
//            if (instructionSearcher.match()) {
//                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
//                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[2];
//
//                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I") &&
//                            !fieldInsnNode.name.equals(getField("getSaturation()").getField().name)) {
//                        addField("getHue()", insnToField(fieldInsnNode, classNode));
//                        break;
//                    }
//                }
//            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}
