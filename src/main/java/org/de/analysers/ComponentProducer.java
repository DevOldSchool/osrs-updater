package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.List;

public class ComponentProducer extends Analyser {
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
        ClassNode producer = getClassAnalyser("Producer").getNode();

        if (producer == null) {
            return null;
        }

        for (ClassNode classNode : classes) {
            if (classNode.superName.equals(producer.name)) {
                return classNode;
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (FieldNode fieldNode : classNode.fields) {
            if (fieldNode.desc.equals("Ljava/awt/Component;")) {
                addField("getComponent()", fieldNode);
            }

            if (fieldNode.desc.equals("Ljava/awt/Image;")) {
                addField("getImage()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}
