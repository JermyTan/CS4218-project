package sg.edu.nus.comp.cs4218.exception;

import java.io.Serial;

public class PasteException extends AbstractApplicationException {

    @Serial
    private static final long serialVersionUID = -1899255051468905192L;

    public PasteException(String message) {
        super("paste: " + message);
    }

    public PasteException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}
