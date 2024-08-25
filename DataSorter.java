import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataSorter {
    public static void main(String[] args) {
        List<String> fileNames = List.of("integers.txt", "floats.txt", "strings.txt");
        String path = "./";
        String prefix = "";
        boolean appendMode = false;
        boolean outputMode = false;
        List<String> files = new ArrayList<>();
        boolean existingFiles = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-o") && i + 1 < args.length) {
                path = args[i + 1];
                if (!isValidPath(path)) {
                    System.out.println("Error: Path " + path + " does not exist. Using current directory.");
                    path = "./";
                } 
                i++;
            } else if (args[i].equals("-p") && i + 1 < args.length) {
                prefix = args[i + 1];
                i++;
            } else if (args[i].equals("-a") && i + 1 < args.length) {
                appendMode = true;
            } else if (args[i].equals("-s") && i + 1 < args.length) {
                outputMode = false;
            } else if (args[i].equals("-f") && i + 1 < args.length) {  
                outputMode = true;
            } else {
                files.add(args[i]);
            }
        }

        if (files.isEmpty()) {
            System.out.println("Usage: java -jar DataSorter.jar -<flag1> -<flag2> ... <file1> <file2> ...");
            return;
        }

        List<Long> integers = new ArrayList<>();
        List<Float> floats = new ArrayList<>();
        List<String> strings = new ArrayList<>();

        List<BufferedReader> readers = new ArrayList<>();
        List<List<String>> allLines = new ArrayList<>();

        try {
            for (String fileName : files) {
                File file = new File(fileName);

                if (!file.exists()) {
                    System.out.println("Error: File " + fileName + " does not exist.");
                    continue;
                }

                existingFiles = true;
                BufferedReader reader = new BufferedReader(new FileReader(fileName));
                readers.add(reader);
                List<String> lines = new ArrayList<>();
                String line;

                while ((line = reader.readLine()) != null) {
                    lines.add(line.trim());
                }
                allLines.add(lines);
            }
            String intFileName = processFileName(fileNames.get(0), path, prefix);
            String floatFileName = processFileName(fileNames.get(1), path, prefix);
            String stringFileName = processFileName(fileNames.get(2), path, prefix);
            writeToFiles(allLines, intFileName, floatFileName, stringFileName, integers, floats, strings, appendMode);
            
        } catch (IOException e) {
            System.out.println("Error: IOException occurred.");
        } finally {
            for (BufferedReader reader : readers) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.out.println("Error closing file: " + e.getMessage());
                }
            }
        }

        if (existingFiles) {
            statistic(integers, floats, strings, outputMode);
        }
    }

    private static boolean isLong(String line) {
        try {
            Long.parseLong(line);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isFloat(String line) {
        try {
            Float.parseFloat(line);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static void writeToFiles(List<List<String>> allLines, String intFileName, String floatFileName, String stringFileName, List<Long> integers, List<Float> floats, List<String> strings, boolean appendMode) {
        boolean hasIntegers = false;
        boolean hasFloats = false;
        boolean hasStrings = false;

        for (List<String> lines : allLines) {
            for (String line : lines) {
                if (isLong(line)) {
                    hasIntegers = true;
                } else if (isFloat(line)) {
                    hasFloats = true;
                } else {
                    hasStrings = true;
                }
            }
        }

        try (
        BufferedWriter integerOutput = hasIntegers ? new BufferedWriter(new FileWriter(intFileName, appendMode)) : null;
        BufferedWriter floatOutput = hasFloats ? new BufferedWriter(new FileWriter(floatFileName, appendMode)) : null;
        BufferedWriter stringOutput = hasStrings ? new BufferedWriter(new FileWriter(stringFileName, appendMode)) : null
    ) {
        int maxLines = allLines.stream().mapToInt(List::size).max().orElse(0); 

        for (int i = 0; i < maxLines; i++) {
            for (List<String> lines : allLines) {
                if (i < lines.size()) {
                    String line = lines.get(i);
                    if (isLong(line)) {
                        if (integerOutput != null) {
                            integerOutput.write(line);
                            integerOutput.newLine(); 
                        }
                        integers.add(Long.parseLong(line));
                    } else if (isFloat(line)) {
                        if (floatOutput != null) {
                            floatOutput.write(line);
                            floatOutput.newLine();
                        }
                        floats.add(Float.parseFloat(line));
                    } else {
                        if (stringOutput != null) { 
                            stringOutput.write(line);
                            stringOutput.newLine();
                        }
                        strings.add(line);
                    }
                }
            }
        } 

    } catch (IOException e) {
        System.out.println("Error writing to files: " + e.getMessage());
    }
    }
    
    private static boolean isValidPath(String path) {
        File file = new File(path);
        return file.exists() && file.isDirectory();
    }

    private static String processFileName(String filename, String path, String prefix) {
        return path + "/" + prefix + filename;
    }

    private static void statistic(List<Long> integers, List<Float> floats, List<String> strings, boolean outputMode) {
        int intCount = integers.size();
        int floatCount = floats.size();
        int stringCount = strings.size();

        if (outputMode) {
            printDashes(200);
            printHeader(200);
            printDashes(200);
            System.out.printf("%-25s %-25s %-25s %-25s %-25s %-25s %-25s %-25s%n", "Data Type", "Number Lines", "Max Value", "Min Value", "Sum Value", "Avg Value", "Smallest String", "Largest String");
            printDashes(200);
            if (!integers.isEmpty()) {
                System.out.printf("%-25s %-25d %-25d %-25d %-25d %-25.9E%n", 
                "Integer", 
                intCount,
                findMaxLong(integers),
                findMinLong(integers),
                findSumLong(integers),
                findAverageLong(integers));
            }
            if (!floats.isEmpty()) {
                System.out.printf("%-25s %-25d %-25.9E %-25.9E %-25.9E %-25.9E%n", 
                "Float", 
                floatCount,
                findMaxFloat(floats),
                findMinFloat(floats),
                findSumFloat(floats),
                findAverageFloat(floats));
            }
            if (!floats.isEmpty()) {
                System.out.printf("%-25s %-25d %-25s %-25s %-25s %-25s %-25d %-25d%n", 
                "String", 
                stringCount,
                "",
                "",
                "",
                "",
                findSmallestStringLength(strings),
                findLargestStringLength(strings)
                );
            }
        } else {
            printDashes(40);
            printHeader(40);
            printDashes(40);
            System.out.printf("%-25s %-25s%n", "File Name", "Number Lines");
            printDashes(40);
            System.out.printf("%-25s %-25d%n", 
                    "integers.txt", 
                    intCount);
            System.out.printf("%-25s %-25d%n", 
                    "floats.txt", 
                    floatCount);
            System.out.printf("%-25s %-25d%n", 
                    "strings.txt", 
                    stringCount);
        }
        
    }

    private static void printDashes(Integer numberOfDashes) {
        for (int i = 0; i < numberOfDashes; i++) {
            System.out.print("-");
        }
        System.out.println("");
    }

    private static void printHeader(Integer headerLength) {
        int nameLength = 10;
        int numberOfSpaces = headerLength - nameLength;
        for (int i = 0; i < headerLength; i++) {
            if (i == numberOfSpaces / 2) {
                System.out.print("STATISTIC");
                i = i + 10;
            } else {
                System.out.print(" "); 
            }
        }
        System.out.println("");
    }

    private static Long findMaxLong(List<Long> numbers) {
        Long max = numbers.get(0);
        for (Long number : numbers) {
            if (number > max) {
                max = number;
            }
        }
        return max;
    }

    private static Long findMinLong(List<Long> numbers) {
        Long min = numbers.get(0);
        for (Long number : numbers) {
            if (number < min) {
                min = number;
            }
        }
        return min;
    }

    private static Long findSumLong(List<Long> numbers) {
        long sum = 0;
        for (Long number : numbers) {
            sum += number;
        }
        return sum;
    }

    private static double findAverageLong(List<Long> numbers) {
        double sum = findSumLong(numbers);
        return sum / numbers.size();
    }

    private static Float findMaxFloat(List<Float> numbers) {
        Float max = numbers.get(0);
        for (Float number : numbers) {
            if (number > max) {
                max = number;
            }
        }
        return max;
    }

    private static Float findMinFloat(List<Float> numbers) {
        Float min = numbers.get(0);
        for (Float number : numbers) {
            if (number < min) {
                min = number;
            }
        }
        return min;
    }

    private static double findSumFloat(List<Float> numbers) {
        double sum = 0;
        for (Float number : numbers) {
            sum += number;
        }
        return sum;
    }

    private static double findAverageFloat(List<Float> numbers) {
        double sum = findSumFloat(numbers);
        return sum / numbers.size();
    }

    private static int findSmallestStringLength(List<String> strings) {
        if (strings == null || strings.isEmpty()) {
            return 0;
        }
        int smallestLength = strings.get(0).length();

        for (String str : strings) {
            int length = str.length();
            if (length < smallestLength) {
                smallestLength = length;
            }
        }

        return smallestLength;
    }

    private static int findLargestStringLength(List<String> strings) {
        if (strings == null || strings.isEmpty()) {
            return 0;
        }
        int largestLength = strings.get(0).length();

        for (String str : strings) {
            int length = str.length();
            if (length > largestLength) {
                largestLength = length;
            }
        }

        return largestLength;
    }
}
