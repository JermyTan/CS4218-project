package sg.edu.nus.comp.cs4218.exception;

import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.SHELL;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;

import java.io.Serial;

public class ShellException extends Exception {

    @Serial
    private static final long serialVersionUID = -2526980738797157495L;

    public ShellException(String message) {
        super(String.format(STRING_LABEL_VALUE_PAIR, SHELL, message));
    }

    public ShellException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}