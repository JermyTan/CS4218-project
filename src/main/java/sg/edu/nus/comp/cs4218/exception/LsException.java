package sg.edu.nus.comp.cs4218.exception;

import java.io.Serial;

public class LsException extends AbstractApplicationException {

    @Serial
    private static final long serialVersionUID = -7362074614126428619L;

    public LsException(String message) {
        super("ls: " + message);
    }

    public LsException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}
