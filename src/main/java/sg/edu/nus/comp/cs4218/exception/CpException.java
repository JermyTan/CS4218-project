package sg.edu.nus.comp.cs4218.exception;

import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_CP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;

import java.io.Serial;

public class CpException extends AbstractApplicationException {
    @Serial
    private static final long serialVersionUID = 7000638571851819498L;

    public CpException(String message) {
        super(String.format(STRING_LABEL_VALUE_PAIR, APP_CP, message));
    }

    public CpException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}
