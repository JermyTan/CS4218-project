package tdd.bf.cmd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CallCommandTest {
    private List<String> argsList;
    private ApplicationRunner appRunner;
    private ArgumentResolver argumentResolver;
    private ByteArrayInputStream stdin;
    private ByteArrayOutputStream stdout;

    @BeforeEach
    void init() {
        String text = "Test stdin";
        stdin = new ByteArrayInputStream(text.getBytes());
        stdout = new ByteArrayOutputStream();
        argsList = new ArrayList<>();
        appRunner = new ApplicationRunner();
        argumentResolver = new ArgumentResolver();
    }

    @Test
    void evaluate_NullArgs_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            CallCommand command = new CallCommand(null, appRunner, argumentResolver);
            command.evaluate(stdin, stdout);
        });
    }

    @Test
    void evaluate_InvalidArgs_ThrowsException() {
        argsList.add("invalid");
        assertThrows(ShellException.class, () -> {
            CallCommand command = new CallCommand(argsList, appRunner, argumentResolver);
            command.evaluate(stdin, stdout);
        });
    }

    @Test
    void evaluate_EmptyArgs_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            CallCommand command = new CallCommand(argsList, appRunner, argumentResolver);
            command.evaluate(stdin, stdout);
        });
    }

    @Test
    void evaluate_NullStdin_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            CallCommand command = new CallCommand(argsList, appRunner, argumentResolver);
            command.evaluate(null, stdout);
        });
    }

    @Test
    void evaluate_NullStdout_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            CallCommand command = new CallCommand(argsList, appRunner, argumentResolver);
            command.evaluate(stdin, null);
        });
    }

    @Test
    void evaluate_NullAppRunner_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            CallCommand command = new CallCommand(argsList, null, argumentResolver);
            command.evaluate(stdin, stdout);
        });
    }

    @Test
    void evaluate_NullArgumentResolver_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            CallCommand command = new CallCommand(argsList, appRunner, null);
            command.evaluate(stdin, stdout);
        });
    }
}
