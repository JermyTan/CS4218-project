package sg.edu.nus.comp.cs4218.exception;

import java.io.Serial;

public class InvalidArgsException extends Exception {

    @Serial
    private static final long serialVersionUID = 7060671468402158735L;

    public InvalidArgsException(String message) {
        super(message);
    }

    public InvalidArgsException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}
