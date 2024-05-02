package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class SoundSystem extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 4;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            if (!classNode.superName.equals(getClassAnalyser("AbstractSoundSystem").getNode().name)) {
                continue;
            }

            return classNode;
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (FieldNode fieldNode : classNode.fields) {
            if (Modifier.isStatic(fieldNode.access)) {
                continue;
            }

            if (fieldNode.desc.equals("[B")) {
                addField("getBytes()", fieldNode);
            }

            if (fieldNode.desc.equals("I")) {
                addField("getLength()", fieldNode);
            }

            if (fieldNode.desc.equals("Ljavax/sound/sampled/AudioFormat;")) {
                addField("getAudioFormat()", fieldNode);
            }

            if (fieldNode.desc.equals("Ljavax/sound/sampled/SourceDataLine;")) {
                addField("getSourceDataLine()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}
