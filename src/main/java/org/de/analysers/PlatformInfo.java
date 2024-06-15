package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

public class PlatformInfo extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 1;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 1;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            if (!classNode.superName.equals(getClassAnalyser("Node").getNode().name)) {
                continue;
            }

            int intArrayCount = 0;
            int stringCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isStatic(fieldNode.access)) {
                    continue;
                }

                if (fieldNode.desc.equals("[I")) {
                    intArrayCount++;
                }

                if (fieldNode.desc.equals("Ljava/lang/String;")) {
                    stringCount++;
                }
            }

            if (intArrayCount == 1 && stringCount == 8) {
                return classNode;
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.desc.equals(String.format("(L%s;B)V", getClassAnalyser("ByteBuffer").getNode().name))) {
                InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ALOAD, GETFIELD, IMUL);
                if (instructionSearch.match()) {
                    for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                        if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                            addField("getOs()", insnToField(fieldInsnNode, classNode));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.desc.equals("(I)Ljava/lang/String;")) {
                addMethod("getDeviceId()", methodNode);
            }
        }
    }
}
