package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class PlatformInfoProvider extends Analyser {
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
                InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, GETSTATIC, LDC, INVOKEVIRTUAL, IFEQ, ICONST_3);
                if (instructionSearch.match()) {
                    for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                        LdcInsnNode ldcInsnNode = (LdcInsnNode) matches[1];

                        if (ldcInsnNode.cst instanceof String stringCst) {
                            if (stringCst.equals("linux")) {
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
