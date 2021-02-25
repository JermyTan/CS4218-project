package sg.edu.nus.comp.cs4218.exception;

import java.io.Serial;

public class ExitException extends AbstractApplicationException {

    @Serial
    private static final long serialVersionUID = 6517503252362314995L;

    /**
     * Used to send a signal to the shell to exit
     *
     * @param message exit code
     */
    public ExitException(String message) {
        super("exit: " + message);
    }

    public ExitException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}
