package org.de.utilities;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InstructionSearcher {
    private final InsnList nodes;
    private final int[] opcodes;
    private final List<AbstractInsnNode[]> matches = new ArrayList<>();
    private int highestBreakpoint = 0;
    private final int maxJumps;
    private int jumps = 0;

    /**
     * Constructs a new instruction searcher.
     */
    public InstructionSearcher(InsnList nodes, int maxJumps, int... opcodes) {
        this.nodes = nodes;
        this.maxJumps = maxJumps;
        this.opcodes = opcodes;
        matches.add(new AbstractInsnNode[opcodes.length]);
    }

    /**
     * Searches for the specified opcode patterns in the instruction list.
     *
     * @return {@code true} if a pattern was found, {@code false} otherwise.
     */
    public boolean match() {
        int matchIndex = 0;
        boolean anyMatches = false;
        AbstractInsnNode currentNode = nodes.getFirst();
        AbstractInsnNode lastFailedStartNode = null;

        while (currentNode != null) {
            // If there was a previous-failed match, reset currentNode to start searching from the instruction following the last failed start node
            if (lastFailedStartNode != null) {
                currentNode = lastFailedStartNode.getNext();
                lastFailedStartNode = null;
            }

            // Check if the current node matches the pattern
            if (matchesOpcode(currentNode, opcodes[matchIndex])) {
                matches.get(size() - 1)[matchIndex] = currentNode;
                matchIndex++;
            } else {
                // Store the starting point of the failed match
                if (matchIndex > 0) {
                    lastFailedStartNode = matches.get(size() - 1)[0];
                }

                // Pattern mismatch, reset pattern and clear last match
                handlePatternMismatch(matchIndex);
                matchIndex = 0;
            }

            // Pattern match found, create a new match entry and reset
            if (matchIndex == opcodes.length) {
                matches.add(new AbstractInsnNode[opcodes.length]);
                anyMatches = true;
                matchIndex = 0;
            }

            // Handle jumps and proceed to the next node
            currentNode = handleJump(currentNode, matchIndex);
        }

        // Remove the last (empty) match entry if no pattern found
        if (!anyMatches) {
            matches.remove(size() - 1);
        }

        return anyMatches;
    }

    /**
     * Clears the last match entry in the matches list.
     */
    private void clearLastMatch() {
        Arrays.fill(matches.get(size() - 1), null);
    }

    /**
     * Helper method to check if the current node's opcode matches the specified opcode.
     *
     * @return {@code true} if the current node matches the specified opcode, {@code false} otherwise.
     */
    private boolean matchesOpcode(AbstractInsnNode currentNode, int opcode) {
        return currentNode.getOpcode() == opcode || opcode == -1;
    }

    /**
     * Handles pattern mismatch: clears the last match entry and tracks highest breakpoints.
     */
    private void handlePatternMismatch(int matchIndex) {
        if (matchIndex > highestBreakpoint) {
            highestBreakpoint = matchIndex;
        }
        clearLastMatch();
    }

    /**
     * Handles jumps in the instruction list.
     *
     * @return The next instruction node to process.
     */
    private AbstractInsnNode handleJump(AbstractInsnNode currentNode, int matchIndex) {
        // If a jump instruction and jumps within allowed range
        if (matchIndex > 0 && currentNode instanceof JumpInsnNode jumpInsn && jumps < maxJumps) {
            currentNode = jumpInsn.label.getNext();
            jumps++;
        } else {
            currentNode = currentNode.getNext();
        }

        return currentNode;
    }

    /**
     * Returns the array of matches respective to the opcodes they matched to
     * Note: The array will be empty unless <code>match()</code> returned <b>true</b>.
     */
    public List<AbstractInsnNode[]> getMatches() {
        List<AbstractInsnNode[]> cleanMatches = new ArrayList<>();

        for (AbstractInsnNode[] match : matches) {
            boolean allNodesNull = true;

            // Check if all nodes in the match array are null
            for (AbstractInsnNode node : match) {
                if (node != null) {
                    allNodesNull = false;
                    break;
                }
            }

            // Add the match to cleanMatches only if not all nodes are null
            if (!allNodesNull) {
                cleanMatches.add(match);
            }
        }

        return cleanMatches;
    }

    /**
     * Returns the size of the given match array
     */
    public int size() {
        return matches.size();
    }
}
