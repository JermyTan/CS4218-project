package sg.edu.nus.comp.cs4218.exception;

import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_CD;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;

import java.io.Serial;

public class CdException extends AbstractApplicationException {

    @Serial
    private static final long serialVersionUID = -5127470754542073972L;

    public CdException(String message) {
        super(String.format(STRING_LABEL_VALUE_PAIR, APP_CD, message));
    }

    public CdException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}