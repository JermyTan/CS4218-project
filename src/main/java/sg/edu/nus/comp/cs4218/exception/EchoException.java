package sg.edu.nus.comp.cs4218.exception;

import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_ECHO;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;

import java.io.Serial;

public class EchoException extends AbstractApplicationException {

    @Serial
    private static final long serialVersionUID = 1190401344228671734L;

    public EchoException(String message) {
        super(String.format(STRING_LABEL_VALUE_PAIR, APP_ECHO, message));
    }

    public EchoException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}