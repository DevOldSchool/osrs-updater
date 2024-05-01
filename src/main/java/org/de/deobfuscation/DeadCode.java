package org.de.deobfuscation;

import org.de.Deobfuscator;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.*;

import java.util.List;

public class DeadCode extends Deobfuscator {
    @Override
    public void onCompletion() {
        label = "dead instructions";
        super.onCompletion();
    }

    @Override
    public void execute(List<ClassNode> classes) {
        Analyzer<BasicValue> analyzer = new Analyzer<>(new BasicInterpreter());

        for (ClassNode classNode : classes) {
            for (MethodNode methodNode : classNode.methods) {
                try {
                    AbstractInsnNode[] insns = methodNode.instructions.toArray();
                    Frame<BasicValue>[] frames = analyzer.analyze(classNode.name, methodNode);

                    for (int i = 0; i < insns.length; i++) {
                        if (frames[i] == null) {
                            if (insns[i] == null) {
                                continue;
                            }

                            methodNode.instructions.remove(insns[i]);
                            removed++;

//                            System.out.println("Removed instructions in " + classNode.name + " " + methodNode.name + " " + insns[i]);
                        }
                    }
                } catch (AnalyzerException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}