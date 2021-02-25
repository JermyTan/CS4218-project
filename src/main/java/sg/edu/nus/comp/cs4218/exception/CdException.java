package sg.edu.nus.comp.cs4218.exception;

import java.io.Serial;

public class CdException extends AbstractApplicationException {

    @Serial
    private static final long serialVersionUID = -5127470754542073972L;

    public CdException(String message) {
        super("cd: " + message);
    }

    public CdException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}