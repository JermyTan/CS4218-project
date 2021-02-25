package sg.edu.nus.comp.cs4218.exception;

import java.io.Serial;

public class ShellException extends Exception {

    @Serial
    private static final long serialVersionUID = -2526980738797157495L;

    public ShellException(String message) {
        super("shell: " + message);
    }

    public ShellException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}