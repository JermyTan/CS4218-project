package sg.edu.nus.comp.cs4218.impl.util;

import java.io.File;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

public final class StringUtils {
    public static final String STRING_LABEL_VALUE_PAIR = "%s: %s";
    public static final String STRING_EMPTY = "";
    public static final String STRING_NEWLINE = System.lineSeparator();
    public static final String STRING_CURR_DIR = ".";
    public static final String STRING_PARENT_DIR = "..";
    public static final String STRING_WHITESPACE = " ";
    public static final String STRING_STDIN_FLAG = "-";
    public static final char CHAR_FILE_SEP = File.separatorChar;
    public static final char CHAR_TAB = '\t';
    public static final char CHAR_SPACE = ' ';
    public static final char CHAR_DOUBLE_QUOTE = '"';
    public static final char CHAR_SINGLE_QUOTE = '\'';
    public static final char CHAR_BACK_QUOTE = '`';
    public static final char CHAR_REDIR_INPUT = '<';
    public static final char CHAR_REDIR_OUTPUT = '>';
    public static final char CHAR_PIPE = '|';
    public static final char CHAR_SEMICOLON = ';';
    public static final char CHAR_ASTERISK = '*';
    public static final char CHAR_FLAG_PREFIX = '-';

    private StringUtils() {
    }

    /**
     * Returns the non-null and trimmed strings.
     *
     * @param strings strings to be sanitized.
     * @return an array of non-null and trimmed strings.
     */
    public static String[] sanitizeStrings(String... strings) {
        if (strings == null) {
            return new String[0];
        }

        return Arrays.stream(strings)
                .filter(Predicate.not(Objects::isNull))
                .map(String::trim)
                .toArray(String[]::new);
    }

    /**
     * Returns the file extension of file name.
     *
     * @param fileName file name whose extension will be retrieved.
     * @return file extension.
     */
    public static String getFileExtension(String fileName) {
        if (isBlank(fileName)) {
            return STRING_EMPTY;
        }

        String trimmedFileName = fileName.trim();
        int extensionPeriodIndex = trimmedFileName.lastIndexOf('.');

        // ignore (hidden) file names without extension.
        if (extensionPeriodIndex <= 0) {
            return STRING_EMPTY;
        }

        return trimmedFileName.substring(extensionPeriodIndex + 1);
    }

    /**
     * Returns the file separator defined for a particular system.
     * Used for RegexArgument parsing only.
     *
     * @return string of file separator.
     */
    public static String fileSeparator() {
        // We need to escape \ in Windows...
        if (System.getProperty("os.name").toLowerCase().contains("win")) {//NOPMD
            return '\\' + File.separator;
        }
        return File.separator;
    }

    /**
     * Check if string contains only whitespace.
     *
     * @param str string to be checked.
     * @return true under any one of the 3 conditions:
     * 1. string is null
     * 2. string is empty
     * 3. string contains only whitespace
     */
    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    /**
     * Performs c * n (as in python).
     *
     * @param character char to be multiplied.
     * @param num if num is < 0, num is assumed to be 0.
     * @return string result of c * n.
     */
    public static String multiplyChar(char character, int num) {
        return String.valueOf(character).repeat(Math.max(0, num));
    }

    /**
     * Tokenize a string delimited by whitespace.
     *
     * @param str string to be tokenized.
     * @return string array containing the tokens.
     */
    public static String[] tokenize(String str) {
        if (isBlank(str)) {
            return new String[0];
        }
        return str.trim().split("\\s+");
    }

    /**
     * Checks if a string can be represented as a number.
     *
     * @param str string possibly representing a number.
     * @return true if str can be represented as a number.
     */
    public static boolean isNumber(String str) {
        BigInteger bigInt;
        try {
            bigInt = new BigInteger(str);
        } catch (Exception e) {
            return false;
        }
        return !bigInt.toString().isEmpty();
    }
}
