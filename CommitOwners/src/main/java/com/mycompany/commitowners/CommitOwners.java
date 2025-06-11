package com.mycompany.commitowners;

import java.io.*;
import java.util.*;

public class CommitOwners {

    private static Map<String, String> employeesMap = new HashMap<>();
    private static Set<String> employeeIds = new HashSet<>();
    private static int maxIdLength = 0;

    public static void main(String[] args) {
        Scanner consoleScanner = new Scanner(System.in);

        System.out.print("Enter the path to the employee file (e.g., employees.txt): ");
        String filePath = consoleScanner.nextLine();

        try {
            readEmployeeData(filePath);
        } catch (FileNotFoundException e) {
            System.err.println("Error: Employee file not found at " + filePath);
            consoleScanner.close();
            return;
        }

        if (employeesMap.isEmpty()) {
            System.err.println("Error: No employee data loaded. Please check the file format and content.");
            consoleScanner.close();
            return;
        }

        System.out.print("Enter the weld string: ");
        String weld = consoleScanner.nextLine().trim();

        consoleScanner.close();

        OptimalResult result = findOptimalDecomposition(weld);

        if (result.combination.isEmpty()) {
            System.out.println("No valid commit combinations found for the given weld.");
        } else {
            System.out.println("\nOptimal Commit Owners (most commits):");
            for (String empId : result.combination) {
                System.out.println(employeesMap.get(empId));
            }
            System.out.println("\nTotal number of possible decompositions: " + result.totalDecompositions);
        }
    }

    private static void readEmployeeData(String filePath) throws FileNotFoundException {
        File file = new File(filePath);
        Scanner fileScanner = new Scanner(file);

        while (fileScanner.hasNextLine()) {
            String line = fileScanner.nextLine();
            String[] parts = line.split(",", 3);

            if (parts.length == 3) {
                String id = parts[0].trim();
                String surname = parts[1].trim();
                String name = parts[2].trim();
                employeesMap.put(id, surname + " " + name);
                employeeIds.add(id);
                maxIdLength = Math.max(maxIdLength, id.length());
            } else {
                System.err.println("Warning: Skipping malformed line in employee file: " + line);
            }
        }
        fileScanner.close();
    }

    private static class OptimalResult {
        List<String> combination;
        long totalDecompositions;
        
        OptimalResult(List<String> combination, long totalDecompositions) {
            this.combination = combination;
            this.totalDecompositions = totalDecompositions;
        }
    }

    private static OptimalResult findOptimalDecomposition(String weld) {
        int n = weld.length();

        List<List<String>> matchesAtPosition = precomputeMatches(weld);
        
        int[] maxCommits = new int[n + 1];
        long[] totalWays = new long[n + 1];
        long[] optimalWays = new long[n + 1];
        String[] bestChoice = new String[n + 1];
        
        maxCommits[n] = 0;
        totalWays[n] = 1;
        optimalWays[n] = 1;
        
        for (int i = n - 1; i >= 0; i--) {
            maxCommits[i] = -1;
            totalWays[i] = 0;
            optimalWays[i] = 0;
            
            for (String empId : matchesAtPosition.get(i)) {
                int nextPos = i + empId.length();
                
                if (maxCommits[nextPos] != -1) {
                    int currentCommits = 1 + maxCommits[nextPos];
                    
                    totalWays[i] += totalWays[nextPos];
                    
                    if (maxCommits[i] < currentCommits) {
                        maxCommits[i] = currentCommits;
                        bestChoice[i] = empId;
                        optimalWays[i] = optimalWays[nextPos];
                    } else if (maxCommits[i] == currentCommits) {
                        optimalWays[i] += optimalWays[nextPos];
                    }
                }
            }
        }
        
        List<String> optimalCombination = new ArrayList<>();
        if (maxCommits[0] != -1) {
            int pos = 0;
            while (pos < n) {
                String empId = bestChoice[pos];
                optimalCombination.add(empId);
                pos += empId.length();
            }
        }
        
        long totalDecompositions = maxCommits[0] != -1 ? totalWays[0] : 0;
        return new OptimalResult(optimalCombination, totalDecompositions);
    }

    private static List<List<String>> precomputeMatches(String weld) {
        int n = weld.length();
        List<List<String>> matchesAtPosition = new ArrayList<>();
        
        for (int i = 0; i <= n; i++) {
            matchesAtPosition.add(new ArrayList<>());
        }

        for (int i = 0; i < n; i++) {
            int maxLen = Math.min(maxIdLength, n - i);
            for (int len = 1; len <= maxLen; len++) {
                String candidate = weld.substring(i, i + len);
                if (employeeIds.contains(candidate)) {
                    matchesAtPosition.get(i).add(candidate);
                }
            }
        }
        
        return matchesAtPosition;
    }
}