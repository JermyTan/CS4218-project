package sg.edu.nus.comp.cs4218.impl.cmd;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_ARGS;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

/**
 * A Pipe Command is a command consisting of multiple Call Commands separated with a pipe.
 * <p>
 * Command format: <Call> | <Call> ...
 */
public class PipeCommand implements Command {
    private final List<CallCommand> callCommands;

    public PipeCommand(List<CallCommand> callCommands) throws ShellException {
        if (
                callCommands == null
                        || callCommands.stream().anyMatch(Objects::isNull)
                        || callCommands.size() < 2
        ) {
            throw new ShellException(ERR_INVALID_ARGS);
        }

        this.callCommands = new ArrayList<>(callCommands);
    }

    @Override
    public void evaluate(InputStream stdin, OutputStream stdout)
            throws AbstractApplicationException, ShellException {

        InputStream nextInputStream = stdin;
        OutputStream nextOutputStream;//NOPMD

        for (int i = 0; i < callCommands.size(); i++) {
            CallCommand callCommand = callCommands.get(i);

            nextOutputStream = new ByteArrayOutputStream();
            if (i == callCommands.size() - 1) {
                nextOutputStream = stdout;
            }

            callCommand.evaluate(nextInputStream, nextOutputStream);

            if (i != callCommands.size() - 1) {
                nextInputStream = new ByteArrayInputStream(((ByteArrayOutputStream) nextOutputStream).toByteArray());//NOPMD
            }

        }
    }

    @Override
    public void terminate() {
        // Unused for now
    }

    public List<CallCommand> getCallCommands() {
        return callCommands;
    }
}
