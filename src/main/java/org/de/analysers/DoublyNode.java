package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class DoublyNode extends Analyser {
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
            if (!classNode.superName.equalsIgnoreCase("java/lang/Object")) {
                continue;
            }

            for (String interfaceName : classNode.interfaces) {
                if (interfaceName.equalsIgnoreCase("java/lang/Iterable")) {
                    for (MethodNode methodNode : classNode.methods) {
                        if ((methodNode.desc.equals(String.format("()L%s;", getClassAnalyser("EntityNode").getNode().name)))) {
                            for (FieldNode fieldNode : classNode.fields) {
                                if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("EntityNode").getNode().name))) {
                                    return classNode;
                                }
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
            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("EntityNode").getNode().name))) {
                if (Modifier.isPublic(fieldNode.access)) {
                    addField("getHead()", fieldNode);
                }

                if (!fieldNode.name.equalsIgnoreCase(getField("getHead()").getField().name) &&
                        (fieldNode.access & Opcodes.ACC_STATIC) == 0) {
                    addField("getCurrent()", fieldNode);
                }
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}
