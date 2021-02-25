package sg.edu.nus.comp.cs4218.exception;

import java.io.Serial;

public class GrepException extends AbstractApplicationException {

    @Serial
    private static final long serialVersionUID = 690613118569875767L;

    public GrepException(String message) {
        super("grep: " + message);
    }

    public GrepException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}
