package org.de.deobfuscation;

import org.de.Deobfuscator;
import org.objectweb.asm.tree.*;

import java.util.*;

public class ExceptionRangeOptimiser extends Deobfuscator {
    @Override
    public void onCompletion() {
        System.out.printf("Deobfuscation: Optimised %d try-catch block handler exception ranges in %d ms\n", removed, (System.currentTimeMillis() - begin));
    }

    @Override
    public void execute(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            for (MethodNode method : classNode.methods) {
                Map<Integer, List<TryCatchBlockNode>> tryCatchIndexes = new HashMap<>();

                for (TryCatchBlockNode tcb : method.tryCatchBlocks) {
                    int handlerIndex = method.instructions.indexOf(tcb.handler);
                    tryCatchIndexes.computeIfAbsent(handlerIndex, k -> new ArrayList<>()).add(tcb);
                }

                for (Map.Entry<Integer, List<TryCatchBlockNode>> entry : tryCatchIndexes.entrySet()) {
                    List<TryCatchBlockNode> tcbList = entry.getValue();
                    if (tcbList.size() > 1) {
                        int start = Integer.MAX_VALUE;
                        int end = 0;
                        LabelNode handler = tcbList.getFirst().handler;
                        Set<String> types = new HashSet<>();

                        for (TryCatchBlockNode tcb : tcbList) {
                            types.add(tcb.type);
                            int startIdx = method.instructions.indexOf(tcb.start);
                            int endIdx = method.instructions.indexOf(tcb.end);
                            if (startIdx < start) {
                                start = startIdx;
                            }
                            if (endIdx > end) {
                                end = endIdx;
                            }
                            method.tryCatchBlocks.remove(tcb);
                            removed++;
                        }

                        for (String type : types) {
                            LabelNode startLabel = findOrCreateLabel(method, start);
                            LabelNode endLabel = findOrCreateLabel(method, end);
                            TryCatchBlockNode tcb = new TryCatchBlockNode(
                                    startLabel,
                                    endLabel,
                                    handler,
                                    type
                            );
                            method.tryCatchBlocks.add(tcb);
                        }
                    }
                }
            }
        }
    }

    private LabelNode findOrCreateLabel(MethodNode method, int index) {
        AbstractInsnNode insn = method.instructions.get(index);
        if (insn instanceof LabelNode) {
            return (LabelNode) insn;
        } else {
            LabelNode label = new LabelNode();
            method.instructions.insertBefore(insn, label);
            return label;
        }
    }
}
