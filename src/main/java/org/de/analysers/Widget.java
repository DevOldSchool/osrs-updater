package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

public class Widget extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 4;
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
            int count = 0;
            for (FieldNode field : classNode.fields) {
                if (field.desc.equals("[Ljava/lang/Object;"))
                    count++;
            }
            if (count > 20) {
                return classNode;
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (FieldNode fieldNode : classNode.fields) {
            if (!Modifier.isStatic(fieldNode.access)) {
                if (fieldNode.desc.equals(String.format("L%s;", classNode.name))) {
                    addField("getParent()", fieldNode);
                }

                if (fieldNode.desc.equals(String.format("[L%s;", classNode.name))) {
                    addField("getChildren()", fieldNode);
                }

                if (fieldNode.desc.equals("[[I")) {
                    addField("getOpcodes()", fieldNode);
                }
            }
        }

        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, ALOAD, ILOAD, ANEWARRAY, PUTFIELD);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) matches[3];

                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("[Ljava/lang/String;")) {
                        addField("getActions()", insnToField(fieldInsnNode, classNode));
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
