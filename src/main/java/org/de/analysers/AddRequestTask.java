package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class AddRequestTask extends Analyser {
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
        // Introduced at revision 215
        for (ClassNode classNode : classes) {
            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.name.equals("<init>")) {
                    InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, LDC);
                    if (instructionSearch.match()) {
                        for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                            LdcInsnNode ldcInsnNode = (LdcInsnNode) matches[0];

                            if (ldcInsnNode.cst instanceof String stringCst) {
                                if (stringCst.equals("AddRequestTask")) {
                                    return classNode;
                                }
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
