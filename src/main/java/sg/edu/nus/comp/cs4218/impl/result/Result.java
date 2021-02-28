package sg.edu.nus.comp.cs4218.impl.result;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;

import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

/**
 * Base class to represent the computed result of an application for a SINGLE input stream.
 */
public class Result {
    protected boolean isError = false;
    protected String errorMessage = "";

    protected Result(boolean isError) {
        this.isError = isError;
    }

    protected Result(boolean isError, String errorMessage) {
        if (errorMessage == null) {
            throw new IllegalArgumentException(ERR_NULL_ARGS);
        }

        this.isError = isError;
        this.errorMessage = errorMessage;
    }

    /**
     * Writes stored error message to stderr.
     * Do nothing if isError is false or errorMessage is blank.
     */
    public void outputError() {
        if (!isError || StringUtils.isBlank(errorMessage)) {
            return;
        }

        System.err.println(errorMessage);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean hasError() {
        return isError;
    }
}
