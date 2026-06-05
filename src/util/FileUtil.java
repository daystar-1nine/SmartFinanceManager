package util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FileUtil Class
 * --------------
 * This class provides utility methods for file operations
 * such as writing and reading data from files.
 */
public class FileUtil {

    /**
     * Writes data to a file
     *
     * @param path   file path
     * @param data   data to write
     * @param append true = append, false = overwrite
     */
    public static void writeToFile(String path, String data, boolean append) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path, append))) {
            writer.write(data);
            writer.newLine(); // move to next line
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    /**
     * Reads all lines from a file
     *
     * @param path file path
     * @return list of lines
     */
    public static List<String> readFromFile(String path) {
        List<String> lines = new ArrayList<>();

        File file = new File(path);

        // If file does not exist, return empty list
        if (!file.exists()) {
            return lines;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;

            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }

        return lines;
    }

    /**
     * Clears the content of a file
     *
     * @param path file path
     */
    public static void clearFile(String path) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(""); // overwrite with empty content
        } catch (IOException e) {
            System.out.println("Error clearing file: " + e.getMessage());
        }
    }
}