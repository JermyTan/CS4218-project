package sg.edu.nus.comp.cs4218.exception;

import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_CP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;

public class CpException extends AbstractApplicationException {
    public CpException(String message) {
        super(String.format(STRING_LABEL_VALUE_PAIR, APP_CP, message));
    }

    public CpException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}
