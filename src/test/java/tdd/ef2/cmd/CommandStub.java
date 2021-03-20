package tdd.ef2.cmd;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.InputStream;
import java.io.OutputStream;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

//Stub to simulate Command for SequenceCommandTest
public class CommandStub implements Command {
    private final Type type;

    public CommandStub(Type type) {
        this.type = type;
    }

    @Override
    public void evaluate(InputStream stdin, OutputStream stdout)
            throws AbstractApplicationException, ShellException {
        if (type == Type.EMPTY_COMMAND) {
            try {
                stdout.write(STRING_NEWLINE.getBytes());
            } catch (Exception e) {

            }
        } else if (type == Type.TEST_COMMAND) {
            String output = "Test command executed" + STRING_NEWLINE;
            try {
                stdout.write(output.getBytes());
            } catch (Exception e) {

            }
        } else if (type == Type.SHELL_EXCEPTION) {
            throw new ShellException("ShellException");
        } else if (type == Type.EXIT_EXCEPTION) {
            throw new ExitException("ExitException");
        } else {
            throw new ShellException(ERR_SYNTAX);
        }
    }

    @Override
    public void terminate() {
        // Unused for now
    }

    public enum Type {
        SHELL_EXCEPTION,
        EXIT_EXCEPTION,
        TEST_COMMAND,
        EMPTY_COMMAND
    }
}