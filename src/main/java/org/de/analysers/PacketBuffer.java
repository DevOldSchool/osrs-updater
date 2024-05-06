package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.List;

public class PacketBuffer extends Analyser {
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
            if (!classNode.superName.equals(getClassAnalyser("ByteBuffer").getNode().name)) {
                continue;
            }

            for (FieldNode fieldNode : classNode.fields) {
                if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("ISAACCipher").getNode().name))) {
                    return classNode;
                }
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (FieldNode fieldNode : classNode.fields) {
            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("ISAACCipher").getNode().name))) {
                addField("getCipher()", fieldNode);
            }

            if (fieldNode.desc.equals("I")) {
                addField("getBitOffset()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}
