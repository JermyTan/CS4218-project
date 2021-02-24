package sg.edu.nus.comp.cs4218.impl.exception;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;

public class InvalidDirectoryException extends Exception {
    public InvalidDirectoryException(String directory, String message) {
        super(String.format(STRING_LABEL_VALUE_PAIR, directory, message));
    }
}
