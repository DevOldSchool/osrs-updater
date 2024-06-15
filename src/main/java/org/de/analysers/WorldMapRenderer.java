package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class WorldMapRenderer extends Analyser {
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
            if (!Modifier.isFinal(classNode.access)) {
                continue;
            }

            int hashMapCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (fieldNode.desc.equals("Ljava/util/HashMap;")) {
                    hashMapCount++;
                }
            }

            if (hashMapCount != 3) {
                continue;
            }

            return classNode;
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
