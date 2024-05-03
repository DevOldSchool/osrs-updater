package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class UrlRequest extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 2;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 1;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isStatic(fieldNode.access)) {
                    continue;
                }

                if (fieldNode.desc.equals("Ljava/net/URL;")) {
                    return classNode;
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

            if (fieldNode.desc.equals("Ljava/net/URL;")) {
                addField("getUrl()", fieldNode);
            }

            if (fieldNode.desc.equals("[B")) {
                addField("getResponse()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            if (Modifier.isStatic(methodNode.access)) {
                continue;
            }

            if (methodNode.desc.endsWith(")Z")) {
                addMethod("isComplete()", methodNode);
            }
        }
    }
}
