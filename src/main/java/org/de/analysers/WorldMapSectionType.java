package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.List;

public class WorldMapSectionType extends Analyser {
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
            if (!classNode.interfaces.contains(getClassAnalyser("Enumerated").getNode().name)) {
                continue;
            }

            int selfFieldCount = 0;
            int byteCount = 0;
            for (FieldNode fieldNode : classNode.fields) {
                if (fieldNode.desc.equals(String.format("L%s;", classNode.name))) {
                    selfFieldCount++;
                }

                if (fieldNode.desc.equals("B")) {
                    byteCount++;
                }
            }

            if (selfFieldCount != 4 || byteCount != 1) {
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
