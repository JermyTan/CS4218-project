package tdd.bf.cmd;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolverUtil;

public class CallCommandTest {
    private List<String> argsList;
    private ApplicationRunner appRunner;
    private ArgumentResolverUtil argumentResolverUtil;
    private ByteArrayInputStream stdin;
    private ByteArrayOutputStream stdout;

    @BeforeEach
    void init() {
        String text = "Test stdin";
        stdin = new ByteArrayInputStream(text.getBytes());
        stdout = new ByteArrayOutputStream();
        argsList = new ArrayList<>();
        appRunner = new ApplicationRunner();
    }

    @Test
    void evaluate_NullArgs_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            CallCommand command = new CallCommand(null, appRunner);
            command.evaluate(stdin, stdout);
        });
    }

    @Test
    void evaluate_InvalidArgs_ThrowsException() {
        argsList.add("invalid");
        assertThrows(ShellException.class, () -> {
            CallCommand command = new CallCommand(argsList, appRunner);
            command.evaluate(stdin, stdout);
        });
    }

    @Test
    void evaluate_EmptyArgs_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            CallCommand command = new CallCommand(argsList, appRunner);
            command.evaluate(stdin, stdout);
        });
    }

    @Test
    void evaluate_NullStdin_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            CallCommand command = new CallCommand(argsList, appRunner);
            command.evaluate(null, stdout);
        });
    }

    @Test
    void evaluate_NullStdout_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            CallCommand command = new CallCommand(argsList, appRunner);
            command.evaluate(stdin, null);
        });
    }

    @Test
    void evaluate_NullAppRunner_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            CallCommand command = new CallCommand(argsList, null);
            command.evaluate(stdin, stdout);
        });
    }
}
