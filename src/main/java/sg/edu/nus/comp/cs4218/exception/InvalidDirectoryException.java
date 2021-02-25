package sg.edu.nus.comp.cs4218.exception;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;

import java.io.Serial;

public class InvalidDirectoryException extends Exception {

    @Serial
    private static final long serialVersionUID = 752688152312224883L;

    public InvalidDirectoryException(String directory, String message) {
        super(String.format(STRING_LABEL_VALUE_PAIR, directory, message));
    }

    public InvalidDirectoryException(String directory, String message, Throwable cause) {
        this(directory, message);

        this.initCause(cause);
    }
}
