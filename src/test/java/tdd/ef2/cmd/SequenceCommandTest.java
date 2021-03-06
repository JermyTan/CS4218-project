package tdd.ef2.cmd;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.SequenceCommand;

public class SequenceCommandTest {
    public static final String TEST_COMMAND_STRING = "Test command executed" + STRING_NEWLINE;
    public static final String SHELL_EXCEPTION_STRING = "shell: ShellException" + STRING_NEWLINE;
    private OutputStream stderr;

    private void captureErr() {
        stderr = new ByteArrayOutputStream();
        System.setErr(new PrintStream(stderr));
    }

    private String getErrOutput() {
        System.setErr(System.err);
        return stderr.toString();
    }

    @Test
    void evaluate_TwoCommands_DisplaysBothResults() throws AbstractApplicationException, ShellException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<Command> commands = new ArrayList<>();
        commands.add(new CommandStub(CommandStub.Type.TEST_COMMAND));
        commands.add(new CommandStub(CommandStub.Type.TEST_COMMAND));
        new SequenceCommand(commands).evaluate(System.in, output);
        assertArrayEquals((TEST_COMMAND_STRING + TEST_COMMAND_STRING).getBytes(), output.toByteArray());
    }

    @Test
    void evaluate_TwoCommandsFirstException_DisplaysExceptionAndSecondResult() throws AbstractApplicationException, ShellException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<Command> commands = new ArrayList<>();
        commands.add(new CommandStub(CommandStub.Type.SHELL_EXCEPTION));
        commands.add(new CommandStub(CommandStub.Type.TEST_COMMAND));

        captureErr();
        new SequenceCommand(commands).evaluate(System.in, output);
        assertArrayEquals(TEST_COMMAND_STRING.getBytes(), output.toByteArray());
        assertEquals(SHELL_EXCEPTION_STRING, getErrOutput());
    }

    @Test
    void evaluate_TwoCommandsSecondException_DisplaysFirstResultAndException() throws AbstractApplicationException, ShellException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<Command> commands = new ArrayList<>();
        commands.add(new CommandStub(CommandStub.Type.TEST_COMMAND));
        commands.add(new CommandStub(CommandStub.Type.SHELL_EXCEPTION));

        captureErr();
        new SequenceCommand(commands).evaluate(System.in, output);
        assertArrayEquals(TEST_COMMAND_STRING.getBytes(), output.toByteArray());
        assertEquals(SHELL_EXCEPTION_STRING, getErrOutput());
    }

    @Test
    void evaluate_TwoCommandsBothException_DisplaysBothExceptions() throws AbstractApplicationException, ShellException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<Command> commands = new ArrayList<>();
        commands.add(new CommandStub(CommandStub.Type.SHELL_EXCEPTION));
        commands.add(new CommandStub(CommandStub.Type.SHELL_EXCEPTION));

        captureErr();
        new SequenceCommand(commands).evaluate(System.in, output);
        assertEquals(SHELL_EXCEPTION_STRING + SHELL_EXCEPTION_STRING, getErrOutput());
    }

    @Test
    void evaluate_TwoCommandsFirstEmpty_DisplaysNewlineAndSecondResult() throws AbstractApplicationException, ShellException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<Command> commands = new ArrayList<>();
        commands.add(new CommandStub(CommandStub.Type.EMPTY_COMMAND));
        commands.add(new CommandStub(CommandStub.Type.TEST_COMMAND));
        new SequenceCommand(commands).evaluate(System.in, output);
        assertArrayEquals((STRING_NEWLINE + TEST_COMMAND_STRING).getBytes(), output.toByteArray());
    }

    @Test
    void evaluate_TwoCommandsSecondEmpty_DisplaysFirstResultAndNewline() throws AbstractApplicationException, ShellException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<Command> commands = new ArrayList<>();
        commands.add(new CommandStub(CommandStub.Type.TEST_COMMAND));
        commands.add(new CommandStub(CommandStub.Type.EMPTY_COMMAND));
        new SequenceCommand(commands).evaluate(System.in, output);
        assertArrayEquals((TEST_COMMAND_STRING + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void evaluate_TwoCommandsBothEmpty_DisplaysTwoNewlines() throws AbstractApplicationException, ShellException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<Command> commands = new ArrayList<>();
        commands.add(new CommandStub(CommandStub.Type.EMPTY_COMMAND));
        commands.add(new CommandStub(CommandStub.Type.EMPTY_COMMAND));
        new SequenceCommand(commands).evaluate(System.in, output);
        assertArrayEquals((STRING_NEWLINE + STRING_NEWLINE).getBytes(), output.toByteArray());
    }
}
