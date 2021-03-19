package tdd.bf.cmd;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

//Stub to simulate CallCommand for PipeCommandTest
public class CallCommandStub extends CallCommand {
    private final List<String> argsList;
    private final Type type;

    public CallCommandStub(List<String> argsList, ApplicationRunner appRunner, ArgumentResolver argumentResolver,
                           Type type) throws ShellException {
        super(argsList, appRunner, argumentResolver);
        this.argsList = argsList;
        this.type = type;
    }

    @Override
    public void evaluate(InputStream stdin, OutputStream stdout)
            throws AbstractApplicationException, ShellException {
        if (type == Type.LEFT_COMMAND) {
            String text = "Test command piped";
            try {
                stdout.write(text.getBytes());
            } catch (Exception e) {

            }
        } else if (type == Type.MIDDLE_COMMAND) {
            String pipedText = "Test command piped";
            String text = "Test command received at second pipe";
            try {
                if (IOUtils.getLinesFromInputStream(stdin).equals(pipedText)) {
                    stdout.write(text.getBytes());
                }
            } catch (Exception e) {

            }
        } else if (type == Type.RIGHT_COMMAND) {
            String pipedText = "Test command received at second pipe";
            String text = "Test command received at last pipe";
            try {
                if (IOUtils.getLinesFromInputStream(stdin).equals(pipedText)) {
                    stdout.write(text.getBytes());
                }
            } catch (Exception e) {

            }
        } else if (type == Type.SHELL_EXCEPTION) {
            throw new ShellException("ShellException");
        } else {
            throw new ShellException(ERR_SYNTAX);
        }
    }

    @Override
    public void terminate() {
        // Unused for now
    }

    public List<String> getArgsList() {
        return argsList;
    }

    public enum Type {
        SHELL_EXCEPTION,
        LEFT_COMMAND,
        MIDDLE_COMMAND,
        RIGHT_COMMAND
    }
}
