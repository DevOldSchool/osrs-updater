package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class AbstractRequester extends Analyser {
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
            if (!classNode.interfaces.contains("java/lang/Runnable")) {
                continue;
            }

            if (!Modifier.isAbstract(classNode.access)) {
                continue;
            }

            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isStatic(fieldNode.access)) {
                    continue;
                }

                if (fieldNode.desc.equals("Ljava/lang/Thread;")) {
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

            if (fieldNode.desc.equals("Ljava/lang/Thread;")) {
                addField("getThread()", fieldNode);
            }

            if (fieldNode.desc.equals("Ljava/util/Queue;")) {
                addField("getRequests()", fieldNode);
            }

            if (fieldNode.desc.equals("Z")) {
                addField("isClosed()", fieldNode);
            }

            if (fieldNode.desc.equals("I")) {
                addField("getClientRevision()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}
