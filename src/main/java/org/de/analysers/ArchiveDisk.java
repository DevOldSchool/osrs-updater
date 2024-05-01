package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class ArchiveDisk extends Analyser {
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
            for (MethodNode methodNode : classNode.methods) {
                InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ALOAD, PUTFIELD);
                if (instructionSearch.match()) {
                    for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                        if (fieldInsnNode.owner.equals(classNode.name) &&
                                fieldInsnNode.desc.equals(String.format("L%s;", getClassAnalyser("AccessFileHandler").getNode().name))) {
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
            InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ALOAD, PUTFIELD, -1, -1, ALOAD, ALOAD, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];
                    FieldInsnNode fieldInsnNode2 = (FieldInsnNode) matches[7];

                    if (fieldInsnNode.owner.equals(classNode.name) &&
                            fieldInsnNode.desc.equals(String.format("L%s;", getClassAnalyser("AccessFileHandler").getNode().name))) {
                        addField("getDataFile()", insnToField(fieldInsnNode, classNode));
                    }

                    if (fieldInsnNode2.owner.equals(classNode.name) &&
                            fieldInsnNode2.desc.equals(String.format("L%s;", getClassAnalyser("AccessFileHandler").getNode().name))) {
                        addField("getIndexFile()", insnToField(fieldInsnNode2, classNode));
                    }
                }
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}
