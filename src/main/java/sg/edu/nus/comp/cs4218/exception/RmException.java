package sg.edu.nus.comp.cs4218.exception;

import java.io.Serial;

public class RmException extends AbstractApplicationException {

    @Serial
    private static final long serialVersionUID = 6146162077392622131L;

    public RmException(String message) {
        super("rm: " + message);
    }

    public RmException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}