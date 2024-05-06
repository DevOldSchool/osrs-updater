package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class ByteBuffer extends Analyser {
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
            if (!classNode.superName.equals(getClassAnalyser("Node").getNode().name)) {
                continue;
            }

            boolean hasIntInitMethod = false;
            boolean hasByteArrayInitMethod = false;
            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.name.equals("<init>") && methodNode.desc.equals("(I)V")) {
                    hasIntInitMethod = true;
                }

                if (methodNode.name.equals("<init>") && methodNode.desc.equals("([B)V")) {
                    hasByteArrayInitMethod = true;
                }
            }

            if (hasIntInitMethod && hasByteArrayInitMethod) {
                return classNode;
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

            if (fieldNode.desc.equals("[B")) {
                addField("getBytes()", fieldNode);
            }

            if (fieldNode.desc.equals("I")) {
                addField("getOffset()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}
