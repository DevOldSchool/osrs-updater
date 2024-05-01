package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class Queue extends Analyser {
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
            if (!classNode.superName.equalsIgnoreCase("java/lang/Object")) {
                continue;
            }

            // TODO find a better way to identify this class
            if (classNode.fields.size() != 1) {
                continue;
            }

            for (MethodNode methodNode : classNode.methods) {
                if ((methodNode.name.equals("<init>") && methodNode.desc.equals("()V"))) {
                    for (FieldNode fieldNode : classNode.fields) {
                        if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("EntityNode").getNode().name))) {
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
        for (FieldNode fieldNode : classNode.fields) {
            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("EntityNode").getNode().name))) {
                addField("getEntityNode()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}
