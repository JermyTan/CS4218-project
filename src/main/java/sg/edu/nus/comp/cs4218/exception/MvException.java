package sg.edu.nus.comp.cs4218.exception;

import java.io.Serial;

public class MvException extends AbstractApplicationException {

    @Serial
    private static final long serialVersionUID = -1181397729455258531L;

    public MvException(String message) {
        super("mv: " + message);
    }

    public MvException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}
