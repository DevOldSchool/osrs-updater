package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.List;

public class SoftReference extends Analyser {
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
            if (!classNode.superName.equals(getClassAnalyser("Wrapper").getNode().name)) {
                continue;
            }

            for (FieldNode fieldNode : classNode.fields) {
                if (fieldNode.desc.equals("Ljava/lang/ref/SoftReference;")) {
                    return classNode;
                }
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (FieldNode fieldNode : classNode.fields) {
            if (fieldNode.desc.equals("Ljava/lang/ref/SoftReference;")) {
                addField("getSoftReference()", fieldNode);
                break;
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}
