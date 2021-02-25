package sg.edu.nus.comp.cs4218.exception;

import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_GREP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;

import java.io.Serial;

public class GrepException extends AbstractApplicationException {

    @Serial
    private static final long serialVersionUID = 690613118569875767L;

    public GrepException(String message) {
        super(String.format(STRING_LABEL_VALUE_PAIR, APP_GREP, message));
    }

    public GrepException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}
