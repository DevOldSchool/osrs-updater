package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class ItemStorage extends Analyser {
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
            if (!classNode.superName.equals(getClassAnalyser("Node").getNode().name)) {
                continue;
            }

            for (MethodNode methodNode : classNode.methods) {
                InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ICONST_M1, IASTORE, PUTFIELD);
                if (instructionSearch.match()) {
                    for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                        if (fieldInsnNode.owner.equals(classNode.name) &&
                                fieldInsnNode.desc.equals("[I")) {
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
            InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ICONST_M1, IASTORE, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                    if (fieldInsnNode.owner.equals(classNode.name) &&
                            fieldInsnNode.desc.equals("[I")) {
                        addField("getIds()", insnToField(fieldInsnNode, classNode));
                        break;
                    }
                }
            }

            instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ICONST_0, IASTORE, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                    if (fieldInsnNode.owner.equals(classNode.name) &&
                            fieldInsnNode.desc.equals("[I")) {
                        addField("getStackSizes()", insnToField(fieldInsnNode, classNode));
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
