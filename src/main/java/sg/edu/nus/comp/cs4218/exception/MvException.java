package sg.edu.nus.comp.cs4218.exception;

import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_MV;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;

import java.io.Serial;

public class MvException extends AbstractApplicationException {

    @Serial
    private static final long serialVersionUID = -1181397729455258531L;

    public MvException(String message) {
        super(String.format(STRING_LABEL_VALUE_PAIR, APP_MV, message));
    }

    public MvException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}
