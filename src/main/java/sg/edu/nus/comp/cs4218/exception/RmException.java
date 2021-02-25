package sg.edu.nus.comp.cs4218.exception;

import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_RM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;

import java.io.Serial;

public class RmException extends AbstractApplicationException {

    @Serial
    private static final long serialVersionUID = 6146162077392622131L;

    public RmException(String message) {
        super(String.format(STRING_LABEL_VALUE_PAIR, APP_RM, message));
    }

    public RmException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}