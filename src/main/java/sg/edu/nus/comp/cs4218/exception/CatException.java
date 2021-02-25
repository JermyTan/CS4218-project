package sg.edu.nus.comp.cs4218.exception;

import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_CAT;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;

import java.io.Serial;

public class CatException extends AbstractApplicationException {

    @Serial
    private static final long serialVersionUID = -1774172207336909713L;

    public CatException(String message) {
        super(String.format(STRING_LABEL_VALUE_PAIR, APP_CAT, message));
    }

    public CatException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}