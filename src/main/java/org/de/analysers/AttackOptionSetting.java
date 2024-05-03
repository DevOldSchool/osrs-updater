package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.util.List;

public class AttackOptionSetting extends Analyser {
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
            int selfFieldCount = 0;
            int selfArrayFieldCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (fieldNode.desc.equals(String.format("L%s;", classNode.name))) {
                    selfFieldCount++;
                }

                if (fieldNode.desc.equals(String.format("[L%s;", classNode.name))) {
                    selfArrayFieldCount++;
                }
            }

            if (selfFieldCount < 6 || selfArrayFieldCount != 1) {
                continue;
            }

            for (MethodNode methodNode : classNode.methods) {
                InstructionSearcher instructionSearcher = new InstructionSearcher(methodNode.instructions, 0, BIPUSH, ANEWARRAY, PUTSTATIC);
                if (instructionSearcher.match()) {
                    for (AbstractInsnNode[] abstractInsnNodes : instructionSearcher.getMatches()) {
                        IntInsnNode intInsnNode = (IntInsnNode) abstractInsnNodes[0];

                        if (intInsnNode.operand == 32) {
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

    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}
