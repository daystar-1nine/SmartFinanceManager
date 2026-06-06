package util;

/**
 * CSVUtil Class
 * -------------
 * Sanitizes input strings to prevent CSV format breakage when writing data
 * to flat files. Replaces commas with semicolons.
 */
public class CSVUtil {

    /**
     * Replaces all commas with semicolons and trims whitespace.
     */
    public static String sanitize(String input) {
        if (input == null) {
            return "";
        }
        return input.replace(",", ";").trim();
    }
}
