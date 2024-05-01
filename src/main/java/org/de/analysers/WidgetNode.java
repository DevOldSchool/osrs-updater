package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.InstructionSearcher;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Modifier;
import java.util.List;

import static org.de.utilities.Wildcard.wildcard;

public class WidgetNode extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 1;
    }

    @Override
    public int getExpectedMethodsSize() {
        return 0;
    }

    @Override
    public ClassNode matchClassNode(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            if (!classNode.superName.equals(getClassAnalyser("Node").getNode().name)) {
                continue;
            }
            int i = 0;
            int b = 0;
            for (FieldNode fn : classNode.fields) {
                if (fn.desc.equals("I") && !Modifier.isStatic(fn.access)) {
                    i++;
                }
                if (fn.desc.equals("Z") && !Modifier.isStatic(fn.access)) {
                    b++;
                }
                if (i == 2 && b == 1) {
                    return classNode;
                }
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        for (MethodNode mn : classNode.methods) {
            if (!wildcard("(*II)V", mn.desc)) {
                continue;
            }
            InstructionSearcher instructionSearcher = new InstructionSearcher(mn.instructions, 0, ALOAD, GETFIELD, -1, -1);
            if (instructionSearcher.match()) {
                for (AbstractInsnNode[] insnNodes : instructionSearcher.getMatches()) {
                    FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNodes[1];
                    if (fieldInsnNode.owner.equals(classNode.name) && fieldInsnNode.desc.equals("I")) {
                        addField("getId()", insnToField(fieldInsnNode));
                    }
                }
            }
        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}
