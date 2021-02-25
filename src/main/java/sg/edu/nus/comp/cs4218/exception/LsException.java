package sg.edu.nus.comp.cs4218.exception;

import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_LS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;

import java.io.Serial;

public class LsException extends AbstractApplicationException {

    @Serial
    private static final long serialVersionUID = -7362074614126428619L;

    public LsException(String message) {
        super(String.format(STRING_LABEL_VALUE_PAIR, APP_LS, message));
    }

    public LsException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}
