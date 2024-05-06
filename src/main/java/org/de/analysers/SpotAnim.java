package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.util.List;

public class SpotAnim extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 0;
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

            if (classNode.name.equals(getClassAnalyser("NpcDefinition").getNode().name) ||
                    classNode.name.equals(getClassAnalyser("ObjectDefinition").getNode().name) ||
                    classNode.name.equals(getClassAnalyser("ItemDefinition").getNode().name)) {
                continue;
            }

            boolean hasCache30 = false;
            boolean hasTextureField = false;
            boolean hasContrastField = false;
            for (MethodNode methodNode : classNode.methods) {
                InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ILOAD, NEWARRAY, PUTFIELD);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[3];
                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("[S")) {
                            hasTextureField = true;
                        }
                    }
                }

                instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, IMUL, PUTFIELD);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[1];
                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                            hasContrastField = true;
                        }
                    }
                }

                instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, NEW, DUP, BIPUSH, INVOKESPECIAL, PUTSTATIC);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                        IntInsnNode intInsnNode = (IntInsnNode) abstractInsnNodes[2];
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNodes[4];

                        if (intInsnNode.operand == 30 && fieldInsnNode.owner.equals(classNode.name) &&
                                fieldInsnNode.desc.equals(String.format("L%s;", getClassAnalyser("Cache").getNode().name))) {
                            hasCache30 = true;
                        }
                    }
                }
            }

            if (hasTextureField && hasContrastField && hasCache30) {
                return classNode;
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {

    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}
