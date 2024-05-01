package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class AbstractArchive extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 6;
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
        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, GETFIELD, INVOKESPECIAL, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[3];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals(String.format("L%s;", getClassAnalyser("IdentityTable").getNode().name))) {
                        addField("getEntryIdentityTable()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, GETFIELD, LDC, IMUL, NEWARRAY, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[4];

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

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, BIPUSH, ALOAD, ALOAD, BIPUSH, INVOKEVIRTUAL, LDC, IMUL, PUTFIELD);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[7];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                        addField("getEntryIndexCount()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ILOAD, PUTFIELD, -1, -1, RETURN);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[2];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("Z")) {
                        addField("isEncrypted()", insnToField(fieldInsnNode, classNode));
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
