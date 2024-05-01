package org.de.analysers;

import org.de.Analyser;
import org.de.utilities.EIS;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class Tile extends Analyser {
    @Override
    public int getExpectedFieldsSize() {
        return 6;
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

            for (MethodNode methodNode : classNode.methods) {
                if (!methodNode.name.equals("<init>")) {
                    continue;
                }
                if (methodNode.desc.equals("(III)V")) {
                    return classNode;
                }
            }
        }

        return null;
    }

    @Override
    public void matchFields(ClassNode classNode) {
        ClassNode gameObject = getClassAnalyser("ItemLayer").getNode();
        ClassNode floorDecoration = getClassAnalyser("FloorDecoration").getNode();
        ClassNode wallDecoration = getClassAnalyser("WallDecoration").getNode();

        for (FieldNode fieldNode : classNode.fields) {
            if (gameObject != null && fieldNode.desc.equals(String.format("[L%s;", gameObject.name))) {
                addField("getGameObjects()", fieldNode);
            }
            if (floorDecoration != null && fieldNode.desc.equals(String.format("L%s;", floorDecoration.name))) {
                addField("getFloorDecoration()", fieldNode);
            }
            if (wallDecoration != null && fieldNode.desc.equals(String.format("L%s;", wallDecoration.name))) {
                addField("getWallDecoration()", fieldNode);
            }
        }

        for (final MethodNode methodNode : getNode().methods) {
            final EIS eis = new EIS(methodNode, "putfield ldc imul");
            if (eis.found() > 0) {
                final FieldInsnNode fin = (FieldInsnNode) eis.getNodesAt(0)[0];
                if (fin.desc.equals("I")) {
                    addField("getPlane()", insnToField(fin));
                }
            }
        }

//        ClassNode reg = getClassAnalyser("Region").getNode();
//        getX:
//        for (MethodNode mn : reg.methods) {
//            InstructionSearcher insn = new InstructionSearcher(mn.instructions, 0, ALOAD, GETFIELD, LDC, IMUL, ISTORE);
//            if (insn.match()) {
//                for (AbstractInsnNode[] matches : insn.getMatches()) {
//                    FieldInsnNode fin = (FieldInsnNode) matches[1];
//                    if (fin.desc.equals("I") && fin.owner.equals(classNode.name)) {
//                        addField("getX()", insnToField(fin, classNode));
//                        break getX;
//                    }
//                }
//            }
//        }
//        getY:
//        for (MethodNode mn : reg.methods) {
//            InstructionSearcher insn = new InstructionSearcher(mn.instructions, 0, ISTORE, ALOAD, GETFIELD, LDC, IMUL);
//            if (insn.match()) {
//                for (AbstractInsnNode[] matches : insn.getMatches()) {
//                    FieldInsnNode fin = (FieldInsnNode) matches[2];
//                    if (fin.desc.equals("I") && fin.owner.equals(classNode.name)) {
//                        addField("getY()", insnToField(fin, classNode));
//                        break getY;
//                    }
//                }
//            }
//        }
    }

    @Override
    public void matchMethods(ClassNode classNode) {

    }
}
