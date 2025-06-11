/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.entropy;


import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Entropy {

    public static void main(String[] args) {
        try {

            File dataFolder = new File("src/main/java/com/mycompany/entropy/data");            File[] textFiles = dataFolder.listFiles(file ->
            file.isFile() && !file.isHidden() && file.getName().endsWith(".txt")
);

            for (File file : textFiles) {
                System.out.println("Processing file: " + file.getName());
                System.out.println("=" + "=".repeat(50));

                String text = readAndCleanText(file);
                
                for (int n = 0; n <= 10; n++) {
                    calculateAndPrintEntropy(text, n);
                }

                System.out.println();
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String readAndCleanText(File file) throws IOException {
        String content = new String(Files.readAllBytes(file.toPath()), "UTF-8");

        content = content.toLowerCase();
        content = content.replaceAll("[^a-zçë\\s]", "");
        content = content.replaceAll("\\s+", " ");
        content = content.trim();

        return content;
    }

    private static void calculateAndPrintEntropy(String text, int n) {
        Map<String, Integer> ngramCounts = new HashMap<>();

        if (n == 0) {
            Set<Character> uniqueChars = new HashSet<>();
            for (char c : text.toCharArray()) {
                if (c != ' ') {
                    uniqueChars.add(c);
                }
            }

            double entropy = (uniqueChars.isEmpty()) ? 0.0 : Math.log(uniqueChars.size()) / Math.log(2);
            System.out.println("n=" + n + " (0-gram):");
            System.out.println("   Entropy: " + String.format("%.4f", entropy) + " bits");
            System.out.println("   Total occurrences: " + uniqueChars.size());
            System.out.println("   All characters have equal probability");
            System.out.println();
            return;
        }

        String cleanText = text.replace(" ", "");

        if (cleanText.length() < n) {
            System.out.println("n=" + n + ": Not enough characters to form " + n + "-grams.");
            System.out.println();
            return;
        }

        for (int i = 0; i <= cleanText.length() - n; i++) {
            String ngram = cleanText.substring(i, i + n);
            ngramCounts.put(ngram, ngramCounts.getOrDefault(ngram, 0) + 1);
        }

        if (ngramCounts.isEmpty()) {
            System.out.println("n=" + n + ": No n-grams found");
            System.out.println();
            return;
        }

        int totalOccurrences = ngramCounts.values().stream().mapToInt(Integer::intValue).sum();
        double entropy = 0.0;

        for (int count : ngramCounts.values()) {
            double probability = (double) count / totalOccurrences;
            entropy -= probability * (Math.log(probability) / Math.log(2));
        }

        PriorityQueue<Map.Entry<String, Integer>> minHeapForMostFrequent = new PriorityQueue<>(
            Comparator.comparingInt(Map.Entry::getValue)
        );

        PriorityQueue<Map.Entry<String, Integer>> maxHeapForLeastFrequent = new PriorityQueue<>(
            Collections.reverseOrder(Comparator.comparingInt(Map.Entry::getValue))
        );

        for (Map.Entry<String, Integer> entry : ngramCounts.entrySet()) {
            minHeapForMostFrequent.offer(entry);
            if (minHeapForMostFrequent.size() > 5) {
                minHeapForMostFrequent.poll();
            }

            maxHeapForLeastFrequent.offer(entry);
            if (maxHeapForLeastFrequent.size() > 5) {
                maxHeapForLeastFrequent.poll();
            }
        }

        List<String> mostFrequent = new ArrayList<>();
        while (!minHeapForMostFrequent.isEmpty()) {
            Map.Entry<String, Integer> entry = minHeapForMostFrequent.poll();
            mostFrequent.add(0, entry.getKey() + " (" + entry.getValue() + ")");
        }

        List<String> leastFrequent = new ArrayList<>();
        while (!maxHeapForLeastFrequent.isEmpty()) {
            Map.Entry<String, Integer> entry = maxHeapForLeastFrequent.poll();
            leastFrequent.add(0, entry.getKey() + " (" + entry.getValue() + ")");
        }

        System.out.println("n=" + n + " (" + n + "-gram):");
        System.out.println("   Entropy: " + String.format("%.4f", entropy) + " bits");
        System.out.println("   Total occurrences: " + totalOccurrences);
        System.out.println("   Unique n-grams: " + ngramCounts.size());
        System.out.println("   Five most frequent: " + mostFrequent);
        System.out.println("   Five least frequent: " + leastFrequent);
        System.out.println();
    }
}