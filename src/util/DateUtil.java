package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * DateUtil Class
 * --------------
 * Standardizes date formatting, parsing, and validation.
 */
public class DateUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Formats LocalDate to yyyy-MM-dd string representation.
     */
    public static String formatDate(LocalDate date) {
        return date == null ? "" : date.format(FORMATTER);
    }

    /**
     * Parses yyyy-MM-dd date string into a LocalDate instance.
     */
    public static LocalDate parseDate(String dateStr) throws DateTimeParseException {
        if (dateStr == null) return null;
        return LocalDate.parse(dateStr.trim(), FORMATTER);
    }

    /**
     * Checks if the date string is in the valid yyyy-MM-dd format.
     */
    public static boolean isValidFormat(String dateStr) {
        if (dateStr == null) return false;
        try {
            parseDate(dateStr);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
