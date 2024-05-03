package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

public class AppletParameter extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 1;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            int selfFieldCount = 0;
            int fieldCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (!Modifier.isStatic(fieldNode.access)) {
                    fieldCount++;
                }

                if (fieldNode.desc.equals(String.format("L%s;", classNode.name))) {
                    selfFieldCount++;
                }
            }

            if (fieldCount != 1 || selfFieldCount != 5) {
                continue;
            }

            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.name.equals("<init>") && methodNode.desc.equals("(Ljava/lang/String;)V")) {
                    InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ALOAD, PUTFIELD);
                    if (instructionSearch.match()) {
                        for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                            FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[2];

                            if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("Ljava/lang/String;")) {
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
        for (FieldNode fieldNode : classNode.fields) {
            if (Modifier.isStatic(fieldNode.access)) {
                continue;
            }

            if (fieldNode.desc.equals("Ljava/lang/String;")) {
                addField("getKey()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}
