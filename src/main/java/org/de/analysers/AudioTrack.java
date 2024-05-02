package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.util.List;

public class AudioTrack extends Analyser {
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
            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.name.equals("<clinit>") && methodNode.desc.equals("()V")) {
                    InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0,ICONST_0, INVOKESTATIC);
                    if (instructionSearcher.match()) {
                        for (AbstractInsnNode[] matches : instructionSearcher.getMatches()) {
                            MethodInsnNode methodInsnNode = (MethodInsnNode) matches[1];

                            if (methodInsnNode.owner.equals("javax/imageio/ImageIO")) {
                                return classNode;
                            }
                        }
                    }
                }
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
