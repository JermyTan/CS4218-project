package sg.edu.nus.comp.cs4218.exception;

import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_UNIQ;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;

import java.io.Serial;

public class UniqException extends AbstractApplicationException {
    @Serial
    private static final long serialVersionUID = 1559743212827472602L;

    public UniqException(String message) {
        super(String.format(STRING_LABEL_VALUE_PAIR, APP_UNIQ, message));
    }

    public UniqException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}
