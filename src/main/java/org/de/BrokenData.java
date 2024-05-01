package org.de;

import java.util.*;

public class BrokenData {
    private List<Analyser> baseAnalyserList;
    private final Map<String, Set<Integer>> brokenClasses;
    private final Map<String, Map<String, Set<Integer>>> brokenMethods;
    private final Map<String, Map<String, Set<Integer>>> brokenFields;

    public BrokenData() {
        this.baseAnalyserList = new ArrayList<>();
        this.brokenClasses = new HashMap<>();
        this.brokenMethods = new HashMap<>();
        this.brokenFields = new HashMap<>();
    }

    public void setBaseAnalyserList(List<Analyser> baseAnalyserList) {
        this.baseAnalyserList = baseAnalyserList;
    }

    public List<Analyser> getBaseAnalyserList() {
        return baseAnalyserList;
    }

    public void addBrokenClass(String className, int revision) {
        brokenClasses.computeIfAbsent(className, k -> new HashSet<>()).add(revision);
    }

    public void addBrokenMethod(String className, String methodName, int revision) {
        brokenMethods
                .computeIfAbsent(className, k -> new HashMap<>())
                .computeIfAbsent(methodName, k -> new HashSet<>())
                .add(revision);
    }

    public void addBrokenField(String className, String fieldName, int revision) {
        brokenFields
                .computeIfAbsent(className, k -> new HashMap<>())
                .computeIfAbsent(fieldName, k -> new HashSet<>())
                .add(revision);
    }

    public void printResults() {
        int totalFields = 0;
        int totalBrokenFields = 0;
        int totalMethods = 0;
        int totalBrokenMethods = 0;

        for (Analyser analyser : baseAnalyserList) {
            totalFields += analyser.getExpectedFieldsSize();
            totalMethods += analyser.getExpectedMethodsSize();
            String className = analyser.getClass().getSimpleName();
            Set<Integer> classRevisions = brokenClasses.getOrDefault(className, Collections.emptySet());
            Map<String, Set<Integer>> fieldRevisions = brokenFields.getOrDefault(className, Collections.emptyMap());
            Map<String, Set<Integer>> methodRevisions = brokenMethods.getOrDefault(className, Collections.emptyMap());

            boolean hasBrokenClass = !classRevisions.isEmpty();
            boolean hasBrokenField = !fieldRevisions.isEmpty();
            boolean hasBrokenMethod = !methodRevisions.isEmpty();

            if (hasBrokenClass || hasBrokenField || hasBrokenMethod) {
                if (hasBrokenClass) {
                    System.out.printf("[ - %s broken in revisions - ] %s\n", className, classRevisions);
                } else {
                    System.out.printf("[ - %s - ]\n", className);
                }

                for (Map.Entry<String, Set<Integer>> fieldEntry : fieldRevisions.entrySet()) {
                    String fieldName = fieldEntry.getKey();
                    Set<Integer> fieldRevisionsSet = fieldEntry.getValue();
                    totalBrokenFields++;
                    System.out.printf("    [> %s.%s broken in revisions - ] %s\n", className, fieldName, fieldRevisionsSet);
                }

                for (Map.Entry<String, Set<Integer>> methodEntry : methodRevisions.entrySet()) {
                    String methodName = methodEntry.getKey();
                    Set<Integer> methodRevisionsSet = methodEntry.getValue();
                    totalBrokenMethods++;
                    System.out.printf("    [> Method %s.%s() broken in revisions - ] %s\n", className, methodName, methodRevisionsSet);
                }
            }
        }

        System.out.printf("\nBroken classes %s/%s\n", brokenClasses.size(), baseAnalyserList.size());
        System.out.printf("Broken methods %s/%s\n", totalBrokenMethods, totalMethods);
        System.out.printf("Broken fields %s/%s\n", totalBrokenFields, totalFields);
    }
}
