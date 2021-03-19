package sg.edu.nus.comp.cs4218.exception;

import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_UNIQ;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;

public class UniqException extends AbstractApplicationException {
    public UniqException(String message) {
        super(String.format(STRING_LABEL_VALUE_PAIR, APP_UNIQ, message));
    }

    public UniqException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}
