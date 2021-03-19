package sg.edu.nus.comp.cs4218.exception;

import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_RM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;

public class RmException extends AbstractApplicationException {
    public RmException(String message) {
        super(String.format(STRING_LABEL_VALUE_PAIR, APP_RM, message));
    }

    public RmException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}
