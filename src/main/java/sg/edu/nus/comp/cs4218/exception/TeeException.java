package sg.edu.nus.comp.cs4218.exception;

import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_TEE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;

import java.io.Serial;

public class TeeException extends AbstractApplicationException {

    @Serial
    private static final long serialVersionUID = 8084356118117600186L;

    public TeeException(String message) {
        super(String.format(STRING_LABEL_VALUE_PAIR, APP_TEE, message));
    }

    public TeeException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}
