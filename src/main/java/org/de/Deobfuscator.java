package org.de;

import org.objectweb.asm.tree.ClassNode;

import java.util.List;

public class Deobfuscator {
    public long begin = 0;
    public int removed = 0;
    public String label = "";

    public void initialize() {
        begin = System.currentTimeMillis();
    }

    public void onCompletion() {
        System.out.printf("Deobfuscation: Removed %d %s in %s ms\n", removed, label, (System.currentTimeMillis() - begin));
    }

    public void execute(List<ClassNode> classes) {
    }

    public void execute(List<ClassNode> classes, List<Analyser> analysers) {
    }
}
