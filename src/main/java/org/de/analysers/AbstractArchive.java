package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

public class AbstractArchive extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 12;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            if (!Modifier.isAbstract(classNode.access)) {
                continue;
            }

            for (MethodNode methodNode : classNode.methods) {
                InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, GETFIELD, INVOKESPECIAL, PUTFIELD);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[3];

                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals(String.format("L%s;", getClassAnalyser("IdentityTable").getNode().name))) {
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
            if (Modifier.isAbstract(fieldNode.access)) {
                continue;
            }

            if (fieldNode.desc.equals("[Ljava/lang/Object;")) {
                addField("getEntryBuffers()", fieldNode);
            }

            if (fieldNode.desc.equals("[[Ljava/lang/Object;")) {
                addField("getChildBuffers()", fieldNode);
            }

            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("IdentityTable").getNode().name))) {
                addField("getEntryIdentityTable()", fieldNode);
            }

            if (fieldNode.desc.equals(String.format("[L%s;", getClassAnalyser("IdentityTable").getNode().name))) {
                addField("getChildIdentityTables()", fieldNode);
            }
        }

        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, GETFIELD, IMUL, NEWARRAY, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[3];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("[I")) {
                        addField("getEntryIndices()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ICONST_1, ILOAD, IADD, NEWARRAY, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[5];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("[I")) {
                        addField("getEntryIdentifiers()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ILOAD, ICONST_1, IADD, NEWARRAY, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[5];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("[I")) {
                        addField("getEntryChildCounts()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, BIPUSH, ALOAD, ALOAD, BIPUSH, INVOKEVIRTUAL, IMUL, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[6];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                        addField("getEntryIndexCount()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ILOAD, PUTFIELD, -1, -1, ALOAD, ILOAD, PUTFIELD, -1, -1, RETURN);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[2];
                    FieldInsnNode fieldInsnNode2 = (FieldInsnNode) abstractInsnNodes[7];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("Z") &&
                            fieldInsnNode2.owner.equals(classNode.name) && fieldInsnNode2.desc.equals("Z")) {
                        addField("isEncrypted()", insnToField(fieldInsnNode, classNode));
                        addField("isDiscardingEntryBuffers()", insnToField(fieldInsnNode2, classNode));
                        break;
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ILOAD, ICONST_1, IADD, ANEWARRAY, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[5];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("[[I")) {
                        addField("getChildIndices()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ICONST_1, ILOAD, IADD, ANEWARRAY, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[5];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("[[I")) {
                        addField("getChildIdentifiers()", insnToField(fieldInsnNode, classNode));
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
