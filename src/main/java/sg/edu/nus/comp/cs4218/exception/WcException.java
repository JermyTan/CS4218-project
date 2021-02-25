package sg.edu.nus.comp.cs4218.exception;

import java.io.Serial;

public class WcException extends AbstractApplicationException {

    @Serial
    private static final long serialVersionUID = 9222264943061063052L;

    public WcException(String message) {
        super("wc: " + message);
    }

    public WcException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}