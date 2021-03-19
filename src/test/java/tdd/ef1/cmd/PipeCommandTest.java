package tdd.ef1.cmd;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import tdd.ef1.cmd.stubs.CallCommandStub;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static tdd.ef1.cmd.stubs.CallCommandStub.CallCommandResults.*;
import static tdd.ef1.cmd.stubs.CallCommandStub.*;

public class PipeCommandTest {
    private static InputStream inputStream;
    private static OutputStream outputStream;
    private static List<CallCommand> callCommands;
    private static List<String> argsList;
    private static ApplicationRunner appRunner;
    private static ArgumentResolver argumentResolver;

    @BeforeEach
    void setUp() {
        inputStream = new ByteArrayInputStream(STRING_EMPTY.getBytes());
        outputStream = new ByteArrayOutputStream();
        callCommands = new ArrayList<>();
        argsList = new ArrayList<>(List.of("echo", "world"));
        appRunner = new ApplicationRunner();
        argumentResolver = new ArgumentResolver();
    }

    @AfterEach
    void tearDown() throws IOException {
        inputStream.close();
        outputStream.close();
    }

    @Test
    public void run_testPipeTwoApplications_NoException() throws Exception {
        callCommands.add(new CallCommandStub(argsList, appRunner, argumentResolver, FIRST_SUCCESS));
        callCommands.add(new CallCommandStub(argsList, appRunner, argumentResolver, SECOND_SUCCESS));
        PipeCommand pipeCommand = new PipeCommand(callCommands);
        pipeCommand.evaluate(inputStream, outputStream);
        assertEquals(SECOND_SUCCESS_MSG, outputStream.toString());
    }

    @Test
    public void run_testPipeTwoApplications_FirstException() throws Exception {
        callCommands.add(new CallCommandStub(argsList, appRunner, argumentResolver, FIRST_EXCEPTION));
        callCommands.add(new CallCommandStub(argsList, appRunner, argumentResolver, SECOND_SUCCESS));
        PipeCommand pipeCommand = new PipeCommand(callCommands);
        Exception expectedException = assertThrows(ShellException.class, () -> pipeCommand.evaluate(inputStream, outputStream));
        assertTrue(expectedException.getMessage().contains(FIRST_EXCEPTION_MSG));
    }

    @Test
    public void run_testPipeTwoApplications_SecondException() throws Exception {
        callCommands.add(new CallCommandStub(argsList, appRunner, argumentResolver, FIRST_SUCCESS));
        callCommands.add(new CallCommandStub(argsList, appRunner, argumentResolver, SECOND_EXCEPTION));
        PipeCommand pipeCommand = new PipeCommand(callCommands);
        Exception expectedException = assertThrows(ShellException.class, () -> pipeCommand.evaluate(inputStream, outputStream));
        assertTrue(expectedException.getMessage().contains(SECOND_EXCEPTION_MSG));
    }

    @Test
    public void run_testPipeTwoApplications_BothException() throws Exception {
        callCommands.add(new CallCommandStub(argsList, appRunner, argumentResolver, FIRST_EXCEPTION));
        callCommands.add(new CallCommandStub(argsList, appRunner, argumentResolver, SECOND_EXCEPTION));
        PipeCommand pipeCommand = new PipeCommand(callCommands);
        Exception expectedException = assertThrows(ShellException.class, () -> pipeCommand.evaluate(inputStream, outputStream));
        assertTrue(expectedException.getMessage().contains(FIRST_EXCEPTION_MSG));
    }
}
