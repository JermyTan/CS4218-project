package sg.edu.nus.comp.cs4218.exception;

import java.io.Serial;

public class EchoException extends AbstractApplicationException {

    @Serial
    private static final long serialVersionUID = 1190401344228671734L;

    public EchoException(String message) {
        super("echo: " + message);
    }

    public EchoException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}