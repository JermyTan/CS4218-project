package sg.edu.nus.comp.cs4218.exception;

import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_WC;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;

import java.io.Serial;

public class WcException extends AbstractApplicationException {

    @Serial
    private static final long serialVersionUID = 9222264943061063052L;

    public WcException(String message) {
        super(String.format(STRING_LABEL_VALUE_PAIR, APP_WC, message));
    }

    public WcException(String message, Throwable cause) {
        this(message);

        this.initCause(cause);
    }
}