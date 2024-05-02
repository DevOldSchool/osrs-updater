package org.de;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static Map<Integer, List<Analyser>> analysers = new HashMap<>();

    public static void main(String[] args) {
        String jar = null;
        String basePath = null;
        int startRevision = 0;
        int endRevision = 0;
        boolean gamepackToFile = false;
        boolean resultsToFile = false;

        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-jar") && i + 1 < args.length) {
                    jar = args[i + 1];
                }

                if (args[i].equals("-base-path") && i + 1 < args.length) {
                    basePath = args[i + 1];
                }

                if (args[i].equals("-start-revision") && i + 1 < args.length) {
                    startRevision = Integer.parseInt(args[i + 1]);
                }

                if (args[i].equals("-end-revision") && i + 1 < args.length) {
                    endRevision = Integer.parseInt(args[i + 1]);
                }

                // Save gamepack classes to files
                if (args[i].equals("--gtf")) {
                    gamepackToFile = true;
                }

                // Save results to file
                if (args[i].equals("--rtf")) {
                    resultsToFile = true;
                }
            }
        }

        if (jar == null) {
            System.out.println("Please provide the jar argument using the '--jar' option.");
        }

        System.out.println("OSRS Updater.");
        if (basePath == null) {
            try {
                Updater.execute(jar, gamepackToFile, resultsToFile);
                Updater.reset();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {
            for (int i = startRevision; i <= endRevision; i++) {
                try {
                    Updater.execute(String.format("%sosrs-%s.jar", basePath, i), gamepackToFile, resultsToFile);
                    Updater.reset();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            // Reset standard output
            if (resultsToFile) {
                System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
            }

            System.out.println("\n");
            System.out.println("Cross revision summary");

            // Find analysers that are broken across revisions
            BrokenData brokenData = getBrokenData(startRevision);

            // After populating broken data, print the results
            brokenData.printResults();
        }
    }

    private static BrokenData getBrokenData(int startRevision) {
        BrokenData brokenData = new BrokenData();

        for (Map.Entry<Integer, List<Analyser>> entry : analysers.entrySet()) {
            Integer revision = entry.getKey();
            List<Analyser> value = entry.getValue();

            if (revision == startRevision) {
                brokenData.setBaseAnalyserList(value);
                continue;
            }

            for (Analyser baseAnalyser : brokenData.getBaseAnalyserList()) {
                for (Analyser analyser : value) {
                    if (!analyser.getClass().getSimpleName().equals(baseAnalyser.getClass().getSimpleName())) {
                        continue;
                    }

                    if (analyser.isBroken()) {
                        brokenData.addBrokenClass(analyser.getClass().getSimpleName(), revision);
                    }

                    for (Map.Entry<String, Field> fieldEntry : baseAnalyser.getFields().entrySet()) {
                        String fieldKey = fieldEntry.getKey();

                        if (!analyser.getFields().containsKey(fieldKey)) {
                            brokenData.addBrokenField(analyser.getClass().getSimpleName(), fieldKey, revision);
                        }
                    }

                    for (Map.Entry<String, Method> methodEntry : baseAnalyser.getMethods().entrySet()) {
                        String methodKey = methodEntry.getKey();

                        if (!analyser.getMethods().containsKey(methodKey)) {
                            brokenData.addBrokenMethod(analyser.getClass().getSimpleName(), methodKey, revision);
                        }
                    }
                }
            }
        }

        return brokenData;
    }
}