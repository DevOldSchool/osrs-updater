package org.de.deobfuscation;

import org.de.Deobfuscator;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;

import java.util.*;

public class ControlFlowOptimiser extends Deobfuscator {
    @Override
    public void onCompletion() {
        System.out.printf("Deobfuscation: Optimised %d method control-flow graphs in %d ms\n", removed, (System.currentTimeMillis() - begin));
    }

    @Override
    public void execute(List<ClassNode> classes) {
        for (ClassNode classNode : classes) {
            for (MethodNode methodNode : classNode.methods) {
                if (!methodNode.tryCatchBlocks.isEmpty()) {
                    continue;
                }

                try {
                    ControlFlowGraph controlFlowGraph = new ControlFlowGraph(classNode, methodNode);
                    controlFlowGraph.build();
                    InsnList newInsns = new InsnList();

                    if (!controlFlowGraph.blocks.isEmpty()) {
                        LabelMap labelMap = new LabelMap();
                        Stack<Block> queue = new Stack<>();
                        HashSet<Block> done = new HashSet<>();
                        queue.add(controlFlowGraph.blocks.getFirst());

                        while (!queue.isEmpty()) {
                            Block block = queue.pop();
                            if (done.contains(block)) {
                                continue;
                            }

                            done.add(block);
                            for (Block branch : block.branches) {
                                queue.add(branch.head());
                            }

                            if (block.next != null) {
                                queue.add(block.next);
                            }

                            for (int i = block.startIndex; i < block.endIndex; i++) {
                                newInsns.add(methodNode.instructions.get(i).clone(labelMap));
                            }
                        }
                    }

                    methodNode.instructions = newInsns;
                    removed += controlFlowGraph.blocks.size();
                } catch (AnalyzerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class ControlFlowGraph extends Analyzer<BasicValue> {
        private final ClassNode classNode;
        private final MethodNode methodNode;
        private final List<Block> blocks = new ArrayList<>();
        private final Block head = new Block();

        ControlFlowGraph(ClassNode classNode, MethodNode method) {
            super(new BasicInterpreter());
            this.classNode = classNode;
            this.methodNode = method;
        }

        void build() throws AnalyzerException {
            analyze(classNode.name, methodNode);
            int id = 0;
            for (Block block : blocks) {
                block.id = id++;

                for (int i = block.startIndex; i < block.endIndex; i++) {
                    if (i >= block.instructions.size()) {
                        continue;
                    }

                    AbstractInsnNode insn = block.instructions.get(i);
                    block.instructions.add(insn);
                    if (insn instanceof LineNumberNode lineNumberNode) {
                        block.lineNumber = lineNumberNode.line;
                    }
                }
            }

            List<Block> orderedBlocks = orderedBlocks();
            blocks.clear();
            blocks.addAll(orderedBlocks);
        }

        @Override
        protected void init(String owner, MethodNode methodNode) {
            AbstractInsnNode[] insns = methodNode.instructions.toArray();
            Block cur = head;
            blocks.add(cur);

            for (int i = 0; i < insns.length; i++) {
                AbstractInsnNode insn = insns[i];
                cur.endIndex++;

                if (insn.getNext() == null) {
                    break;
                }

                if (insn.getNext().getType() == AbstractInsnNode.LABEL ||
                        insn.getType() == AbstractInsnNode.JUMP_INSN ||
                        insn.getType() == AbstractInsnNode.LOOKUPSWITCH_INSN ||
                        insn.getType() == AbstractInsnNode.TABLESWITCH_INSN) {
                    cur = new Block();
                    cur.startIndex = i + 1;
                    cur.endIndex = i + 1;
                    blocks.add(cur);
                }
            }
        }

        @Override
        protected void newControlFlowEdge(int insnIndex, int successorIndex) {
            Block block1 = findBlockContainingIndex(insnIndex);
            Block block2 = findBlockContainingIndex(successorIndex);

            if (block1 != block2) {
                if (insnIndex + 1 == successorIndex) {
                    block1.next = block2;
                    block2.prev = block1;
                } else {
                    block1.branches.add(block2);
                }
            }
        }

        private Block findBlockContainingIndex(int index) {
            for (Block block : blocks) {
                if (index >= block.startIndex && index < block.endIndex) {
                    return block;
                }
            }

            throw new IllegalArgumentException("No block contains index " + index);
        }

        private List<Block> orderedBlocks() {
            List<Block> results = new ArrayList<>();
            walk(head, results, new HashSet<>());
            Collections.reverse(results);

            return results;
        }

        private void walk(Block cur, List<Block> ordered, Set<Block> done) {
            Block next = cur.next;

            if (next != null && done.add(next)) {
                walk(next, ordered, done);
            }

            List<Block> branches = new ArrayList<>(cur.branches);
            branches.sort(Comparator.comparingInt(b -> b.lineNumber));
            for (Block branch : branches) {
                if (done.add(branch)) {
                    walk(branch, ordered, done);
                }
            }
            ordered.add(cur);
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append("CFG: ").append(methodNode.name).append("\n");
            for (Block block : blocks) {
                str.append("\tBLOCK: ").append(block).append("\n");
                for (Block branch : block.branches) {
                    str.append("\t\tBRANCH: ").append(branch).append("\n");
                }
            }
            return str.toString();
        }
    }

    private static class Block {
        int id = -1;
        Block prev;
        Block next;
        final List<Block> branches = new ArrayList<>();
        int startIndex = 0;
        int endIndex = 0;
        int lineNumber = -1;
        final List<AbstractInsnNode> instructions = new ArrayList<>();

        Block head() {
            Block cur = this;
            while (cur.prev != null) {
                cur = cur.prev;
            }
            return cur;
        }

        @Override
        public String toString() {
            return id + " - LINE: " + lineNumber;
        }
    }

    public static class LabelMap extends AbstractMap<LabelNode, LabelNode> {
        private final HashMap<LabelNode, LabelNode> map = new HashMap<>();

        @Override
        public Set<Entry<LabelNode, LabelNode>> entrySet() {
            throw new IllegalStateException();
        }

        @Override
        public LabelNode get(Object key) {
            LabelNode label = (LabelNode) key;
            return map.computeIfAbsent(label, k -> new LabelNode());
        }
    }
}
