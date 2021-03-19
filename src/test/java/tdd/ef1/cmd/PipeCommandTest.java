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
import static tdd.ef1.cmd.stubs.CallCommandStub.CallCommandResults.*;
import static tdd.ef1.cmd.stubs.CallCommandStub.*;

public class PipeCommandTest {
    private static InputStream inputStream;
    private static OutputStream outputStream;
    private static List<CallCommand> callCommand;
    private static List<String> argsList;
    private static ApplicationRunner appRunner;
    private static ArgumentResolver argumentResolver;

//    @BeforeAll
//    static void setupAll() {
//
//    }
//
//    @AfterAll
//    static void tearDownAll() {
//
//    }

    @BeforeEach
    void setUp() {
        inputStream = new ByteArrayInputStream("".getBytes());
        outputStream = new ByteArrayOutputStream();
        callCommand = new ArrayList<>();
        argsList = new ArrayList<>();
        appRunner = new ApplicationRunner();
        argumentResolver = new ArgumentResolver();
    }

    @AfterEach
    void tearDown() throws IOException {
        inputStream.close();
        outputStream.close();
    }

    @Test
    public void run_testPipeTwoApplications_NoExpception() throws AbstractApplicationException, ShellException, IOException {
        callCommand.add(new CallCommandStub(argsList, appRunner, argumentResolver, FIRST_SUCCESS));
        callCommand.add(new CallCommandStub(argsList, appRunner, argumentResolver, SECOND_SUCCESS));
        PipeCommand pipeCommand = new PipeCommand(callCommand);
        pipeCommand.evaluate(inputStream, outputStream);
        assertEquals(SECOND_SUCCESS_MSG, outputStream.toString());
    }

    @Test
    public void run_testPipeTwoApplications_FirstException() throws AbstractApplicationException, ShellException {
        callCommand.add(new CallCommandStub(argsList, appRunner, argumentResolver, FIRST_EXCEPTION));
        callCommand.add(new CallCommandStub(argsList, appRunner, argumentResolver, SECOND_SUCCESS));
        PipeCommand pipeCommand = new PipeCommand(callCommand);
        Exception expectedException = assertThrows(ShellException.class, () -> pipeCommand.evaluate(inputStream, outputStream));
        assertTrue(expectedException.getMessage().contains(FIRST_EXCEPTION_MSG));
    }

    @Test
    public void run_testPipeTwoApplications_SecondException() throws AbstractApplicationException, ShellException {
        callCommand.add(new CallCommandStub(argsList, appRunner, argumentResolver, FIRST_SUCCESS));
        callCommand.add(new CallCommandStub(argsList, appRunner, argumentResolver, SECOND_EXCEPTION));
        PipeCommand pipeCommand = new PipeCommand(callCommand);
        Exception expectedException = assertThrows(ShellException.class, () -> pipeCommand.evaluate(inputStream, outputStream));
        assertTrue(expectedException.getMessage().contains(SECOND_EXCEPTION_MSG));
    }

    @Test
    public void run_testPipeTwoApplications_BothException() throws AbstractApplicationException, ShellException {
        callCommand.add(new CallCommandStub(argsList, appRunner, argumentResolver, FIRST_EXCEPTION));
        callCommand.add(new CallCommandStub(argsList, appRunner, argumentResolver, SECOND_EXCEPTION));
        PipeCommand pipeCommand = new PipeCommand(callCommand);
        Exception expectedException = assertThrows(ShellException.class, () -> pipeCommand.evaluate(inputStream, outputStream));
        assertTrue(expectedException.getMessage().contains(FIRST_EXCEPTION_MSG));
    }
}
