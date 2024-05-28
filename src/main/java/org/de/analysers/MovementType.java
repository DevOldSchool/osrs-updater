package org.de.analysers;

import org.de.Analyser;
import org.de.Updater;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class MovementType extends Analyser {
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
            int byteCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (!Modifier.isStatic(fieldNode.access) && fieldNode.desc.equals("B")) {
                    byteCount++;
                }
            }

            if (byteCount != 1) {
                continue;
            }

            for (MethodNode methodNode : classNode.methods) {
                if (Updater.gamepackRevision > 221) {
                    if (methodNode.name.equals("<init>") && methodNode.desc.equals("(BF)V")) {
                        return classNode;
                    }
                } else {
                    if (methodNode.name.equals("<init>") && methodNode.desc.equals("(B)V")) {
                        return classNode;
                    }
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
