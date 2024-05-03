package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class KitDefinition extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 0;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            if (!classNode.superName.equals(getClassAnalyser("EntityNode").getNode().name)) {
                continue;
            }

            int shortArrayCount = 0;
            int intArrayCount = 0;
            int boolCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isStatic(fieldNode.access)) {
                    continue;
                }

                if (fieldNode.desc.equals("[S")) {
                    shortArrayCount++;
                }

                if (fieldNode.desc.equals("[I")) {
                    intArrayCount++;
                }

                if (fieldNode.desc.equals("Z")) {
                    boolCount++;
                }
            }

            if (shortArrayCount < 4 || intArrayCount < 2 || boolCount != 1) {
                continue;
            }

            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.desc.equals(String.format("(L%s;I)V", getClassAnalyser("ByteBuffer").getNode().name))) {
                    return classNode;
                }
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {

    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}
