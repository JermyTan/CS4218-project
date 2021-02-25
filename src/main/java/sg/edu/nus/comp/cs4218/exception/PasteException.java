package sg.edu.nus.comp.cs4218.exception;

import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_PASTE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;

import java.io.Serial;

public class PasteException extends AbstractApplicationException {

    @Serial
    private static final long serialVersionUID = -1899255051468905192L;

    public PasteException(String message) {
        super(String.format(STRING_LABEL_VALUE_PAIR, APP_PASTE, message));
    }

    public PasteException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}
