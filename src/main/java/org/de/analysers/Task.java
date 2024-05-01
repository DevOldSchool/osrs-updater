package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class Task extends Analyser {
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
            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isVolatile(fieldNode.access) && fieldNode.desc.equals("Ljava/lang/Object;")) {
                    return classNode;
                }
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (FieldNode fieldNode : classNode.fields) {
            if (Modifier.isVolatile(fieldNode.access) && fieldNode.desc.equals("Ljava/lang/Object;")) {
                addField("getValue()", fieldNode);
            }

            if (fieldNode.desc.equals(String.format("L%s;", classNode.name))) {
                addField("getTask()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}
