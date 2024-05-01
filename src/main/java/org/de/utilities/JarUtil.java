package org.de.utilities;

import org.de.Updater;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.objectweb.asm.Opcodes.*;

public class JarUtil {
    /**
     * Load class nodes from JAR file.
     */
    public static List<ClassNode> loadNodes(File file) {
        List<ClassNode> nodes = new ArrayList<>();
        try (JarFile jar = new JarFile(file)) {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();

                if (entry.getName().endsWith(".class")) {
                    if (entry.getName().contains("bouncycastle") || entry.getName().contains("json")) {
                        continue;
                    }

                    ClassNode classNode = new ClassNode();
                    ClassReader classReader = new ClassReader(jar.getInputStream(entry));

                    classReader.accept(classNode, ClassReader.SKIP_FRAMES);
                    nodes.add(classNode);

                    // Get gamepack revision
                    if (classNode.name.equals("client")) {
                        setGamepackRevision(classNode);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return nodes;
    }

    /**
     * Find and set the gamepack revision.
     */
    public static void setGamepackRevision(ClassNode classNode) {
        for (MethodNode methodNode : classNode.methods) {
            InstructionSearcher instructionSearch = new InstructionSearcher(methodNode.instructions, 0, DUP, ALOAD, GETFIELD, SIPUSH);
            if (instructionSearch.match()) {
                for (AbstractInsnNode[] matches : instructionSearch.getMatches()) {
                    IntInsnNode intInsnNode = (IntInsnNode) matches[3];
                    Updater.gamepackRevision = intInsnNode.operand;
                    return;
                }
            }
        }
    }

    /**
     * Recompute MAXS and MAXLOCALS for each class and method.
     */
    public static void recomputeMaxsForClasses(List<ClassNode> classes) {
        // Iterate through each class
        for (ClassNode classNode : classes) {
            // Create a ClassWriter with COMPUTE_MAXS option
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);

            try {
                // Iterate through each method in the class
                for (MethodNode methodNode : classNode.methods) {
                    try {
                        // Create a MethodVisitor that recomputes MAXS and MAXLOCALS
                        MethodVisitor methodVisitor = classWriter.visitMethod(
                                methodNode.access,
                                methodNode.name,
                                methodNode.desc,
                                methodNode.signature,
                                methodNode.exceptions.toArray(new String[0])
                        );

                        // Accept the MethodVisitor to recompute MAXS and MAXLOCALS
                        methodNode.accept(methodVisitor);
                    } catch (Exception e) {
                        System.err.println("Error processing method " + methodNode.name + " in class " + classNode.name + ": " + e.getMessage());
                    }
                }

                // Convert the modified ClassNode back to a byte array
                classNode.accept(classWriter);
            } catch (Exception e) {
                System.err.println("Error processing class " + classNode.name + ": " + e.getMessage());
            }
        }
    }

    /**
     * Saves the list of ClassNode objects to disk.
     */
    public static void saveClassesToDisk(List<ClassNode> classes, String outputDirectory) {
        try {
            Files.createDirectories(Paths.get(outputDirectory));
        } catch (IOException e) {
            System.err.println("Failed to create output directory: " + e.getMessage());
            return;
        }

        for (ClassNode classNode : classes) {
            try {
                ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                classNode.accept(classWriter);
                byte[] classBytes = classWriter.toByteArray();

                String className = classNode.name.replace('/', '_') + ".class";
                String filePath = Paths.get(outputDirectory, className).toString();

                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    fos.write(classBytes);
                }
            } catch (IOException e) {
                System.err.printf("Failed to save class %s: %s\n", classNode.name, e.getMessage());
            }
        }
    }
}
