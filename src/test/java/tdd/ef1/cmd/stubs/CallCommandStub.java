package tdd.ef1.cmd.stubs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

public class CallCommandStub extends CallCommand {
    // `pipe` and `sequence` related errors for stub
    public static final String FIRST_SUCCESS_MSG = "First Application Success";
    public static final String FIRST_ERROR_MSG = "First Application Error I/O";
    public static final String SECOND_SUCCESS_MSG = "Second Application Success";
    public static final String SECOND_ERROR_MSG = "Second Application Error I/O";
    public static final String THIRD_SUCCESS_MSG = "Third Application Success";
    public static final String THIRD_ERROR_MSG = "Third Application Error I/O";
    public static final String FIRST_EXCEPTION_MSG = "First Application Exception";
    public static final String SECOND_EXCEPTION_MSG = "Second Application Exception";
    public static final String THIRD_EXCEPTION_MSG = "Third Application Exception";
    private final CallCommandResults resultType;
    private final String result;

    public CallCommandStub(
            List<String> argsList,
            ApplicationRunner appRunner,
            ArgumentResolver argumentResolver,
            CallCommandResults resultType
    ) throws ShellException {
        super(argsList, appRunner, argumentResolver);
        this.resultType = resultType;
        result = null;
    }

    @Override
    public void evaluate(InputStream stdin, OutputStream stdout) throws ShellException {

        switch (resultType) {
        case FIRST_SUCCESS:
            try {
                stdout.write(FIRST_SUCCESS_MSG.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new ShellException(FIRST_ERROR_MSG, e);
            }
            break;
        case SECOND_SUCCESS:
            try {
                String firstResult = IOUtils.getLinesFromInputStream(stdin).get(0);
                if (firstResult.equals(FIRST_SUCCESS_MSG)) {
                    stdout.write(SECOND_SUCCESS_MSG.getBytes(StandardCharsets.UTF_8));
                }
            } catch (Exception e) {
                throw new ShellException(SECOND_ERROR_MSG, e);
            }
            break;
        case FIRST_EXCEPTION:
            throw new ShellException(FIRST_EXCEPTION_MSG);
        case SECOND_EXCEPTION:
            throw new ShellException(SECOND_EXCEPTION_MSG);
        default:
            throw new ShellException("Fail");
        }
    }

    @Override
    public void terminate() {
        // Unused for now
    }

    public enum CallCommandResults {
        FIRST_SUCCESS,
        SECOND_SUCCESS,
        FIRST_EXCEPTION,
        SECOND_EXCEPTION,
        SPECIFIED_RESULT
    }
}
