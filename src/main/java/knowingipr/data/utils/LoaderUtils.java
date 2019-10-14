package knowingipr.data.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LoaderUtils {
    /**
     * Extracts date from node according to the specified format
     *
     * @param dateString - Json node from which to extract the date
     * @param format     - Format of the date
     * @return Parsed Date instance
     */
    public static LocalDate extractDate(String dateString, String format) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            return LocalDate.parse(dateString, formatter);
        } catch (IllegalArgumentException | DateTimeParseException e) {
            return null;
        }
    }
}
