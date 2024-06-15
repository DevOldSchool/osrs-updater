package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class ChatSetting extends Analyser {
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
            int selfFieldCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (Modifier.isFinal(fieldNode.access) && Modifier.isStatic(fieldNode.access) &&
                        fieldNode.desc.equals(String.format("L%s;", classNode.name))) {
                    selfFieldCount++;
                }
            }

            if (selfFieldCount != 3 || classNode.fields.size() > 4) {
                continue;
            }

            for (MethodNode methodNode : classNode.methods) {
                if (methodNode.name.equals("<init>") && methodNode.desc.equals("()V")) {
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
