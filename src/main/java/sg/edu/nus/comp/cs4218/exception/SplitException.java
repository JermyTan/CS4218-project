package sg.edu.nus.comp.cs4218.exception;

public class SplitException extends AbstractApplicationException {

    private static final long serialVersionUID = -5883292222072101576L;

    public SplitException(String message) {
        super("split: " + message);
    }

    public SplitException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}
