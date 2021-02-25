package sg.edu.nus.comp.cs4218.exception;

import java.io.Serial;

public class CatException extends AbstractApplicationException {

    @Serial
    private static final long serialVersionUID = -1774172207336909713L;

    public CatException(String message) {
        super("cat: " + message);
    }

    public CatException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}