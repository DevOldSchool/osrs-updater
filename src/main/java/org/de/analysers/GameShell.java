package org.de.analysers;

import org.de.Analyser;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Modifier;
import java.util.List;

public class GameShell extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 5;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            if (!classNode.superName.equals("java/applet/Applet")) {
                continue;
            }

            if (!classNode.interfaces.contains("java/lang/Runnable") ||
                    !classNode.interfaces.contains("java/awt/event/FocusListener") ||
                    !classNode.interfaces.contains("java/awt/event/WindowListener")) {
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

            if (fieldNode.desc.equals("Ljava/awt/EventQueue;")) {
                addField("getEventQueue()", fieldNode);
            }

            if (fieldNode.desc.equals("Ljava/awt/Canvas;")) {
                addField("getCanvas()", fieldNode);
            }

            if (fieldNode.desc.equals("Ljava/awt/Frame;")) {
                addField("getFrame()", fieldNode);
            }

            if (fieldNode.desc.equals("Ljava/awt/datatransfer/Clipboard;")) {
                addField("getClipboard()", fieldNode);
            }

            if (fieldNode.desc.equals(String.format("L%s;", getClassAnalyser("MouseWheelListener").getNode().name))) {
                addField("getMouseWheelListener()", fieldNode);
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}
