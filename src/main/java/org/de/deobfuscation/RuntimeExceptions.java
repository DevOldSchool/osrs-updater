package org.de.deobfuscation;

import org.de.Deobfuscator;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import java.util.ArrayList;
import java.util.List;


public class RuntimeExceptions extends Deobfuscator {
    @Override
    public void onCompletion() {
        label = "RuntimeException try-catch blocks";
        super.onCompletion();
    }

    @Override
    public void execute(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            for (MethodNode methodNode : classNode.methods) {
                if (classNode.name.equals("client") && methodNode.name.equals("<init>")) {
                    continue;
                }

                // Check each try catch block for runtime exceptions
                List<TryCatchBlockNode> exceptionsToRemove = new ArrayList<>();
                for (TryCatchBlockNode exceptionNode : methodNode.tryCatchBlocks) {
                    if (exceptionNode.type != null && exceptionNode.type.equals("java/lang/RuntimeException")) {
                        exceptionsToRemove.add(exceptionNode);
                    }
                }

                // Remove identified exceptions
                for (TryCatchBlockNode exceptionNode : exceptionsToRemove) {
                    methodNode.tryCatchBlocks.remove(exceptionNode);
                    removed++;
                }
            }
        }
    }
}
