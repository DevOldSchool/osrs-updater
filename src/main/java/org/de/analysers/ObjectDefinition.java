package org.de.analysers;

import org.de.Analyser;
import org.de.Updater;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

import static org.de.utilities.Wildcard.wildcard;

public class ObjectDefinition extends Analyser {
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
            if (!classNode.superName.equals(getClassAnalyser("EntityNode").getNode().name)) {
                continue;
            }
            for (MethodNode methodNode : classNode.methods) {
                if (!Modifier.isStatic(methodNode.access) &&
                        wildcard(String.format("(II[[IIIIL%s;I*)L%s;", getClassAnalyser("AnimationSequence").getNode().name, getClassAnalyser("Model").getNode().name), methodNode.desc)) {
                    return classNode;
                }
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (FieldNode fieldNode : classNode.fields) {
            if (Modifier.isStatic(fieldNode.access)) continue;
            if (fieldNode.desc.equals("Ljava/lang/String;")) addField("getName()", fieldNode);
            if (fieldNode.desc.equals("[Ljava/lang/String;")) addField("getActions()", fieldNode);
        }

        for (ClassNode cn : Updater.classes) {
            for (MethodNode methodNode : cn.methods) {
                if (!wildcard("(I*)V", methodNode.desc)) {
                    continue;
                }
                InstructionSearcher is = new InstructionSearcher(methodNode.instructions, 0, GETFIELD, -1, ISTORE, -1, ALOAD, GETFIELD);
                if (is.match()) {
                    for (AbstractInsnNode[] insnNodes : is.getMatches()) {
                        FieldInsnNode width = (FieldInsnNode) insnNodes[0];
                        FieldInsnNode height = (FieldInsnNode) insnNodes[5];
                        if (height == null) {
                            continue;
                        }

                        if (width.owner.equals(classNode.name) && width.desc.equals("I")) {
                            addField("getWidth()", insnToField(width));
                        }
                        if (height.owner.equals(classNode.name) && height.desc.equals("I")) {
                            addField("getHeight()", insnToField(height));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}
