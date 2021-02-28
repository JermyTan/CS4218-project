package sg.edu.nus.comp.cs4218.exception;

import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_SPLIT;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;

public class SplitException extends AbstractApplicationException {

    private static final long serialVersionUID = -5883292222072101576L;

    public SplitException(String message) {
        super(String.format(STRING_LABEL_VALUE_PAIR, APP_SPLIT, message));
    }

    public SplitException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}
