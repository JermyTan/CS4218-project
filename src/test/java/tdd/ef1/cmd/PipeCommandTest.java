package tdd.ef1.cmd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static tdd.ef1.cmd.stubs.CallCommandStub.CallCommandResults.FIRST_EXCEPTION;
import static tdd.ef1.cmd.stubs.CallCommandStub.CallCommandResults.FIRST_SUCCESS;
import static tdd.ef1.cmd.stubs.CallCommandStub.CallCommandResults.SECOND_EXCEPTION;
import static tdd.ef1.cmd.stubs.CallCommandStub.CallCommandResults.SECOND_SUCCESS;
import static tdd.ef1.cmd.stubs.CallCommandStub.FIRST_EXCEPTION_MSG;
import static tdd.ef1.cmd.stubs.CallCommandStub.SECOND_EXCEPTION_MSG;
import static tdd.ef1.cmd.stubs.CallCommandStub.SECOND_SUCCESS_MSG;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import tdd.ef1.cmd.stubs.CallCommandStub;

public class PipeCommandTest {
    private static InputStream inputStream;
    private static OutputStream outputStream;
    private static List<CallCommand> callCommands;
    private static List<String> argsList;
    private static ApplicationRunner appRunner;

    @BeforeEach
    void setUp() {
        inputStream = new ByteArrayInputStream(STRING_EMPTY.getBytes());
        outputStream = new ByteArrayOutputStream();
        callCommands = new ArrayList<>();
        argsList = new ArrayList<>(List.of("echo", "world"));
        appRunner = new ApplicationRunner();
    }

    @AfterEach
    void tearDown() throws IOException {
        inputStream.close();
        outputStream.close();
    }

    @Test
    public void run_testPipeTwoApplications_NoException() throws Exception {
        callCommands.add(new CallCommandStub(argsList, appRunner, FIRST_SUCCESS));
        callCommands.add(new CallCommandStub(argsList, appRunner, SECOND_SUCCESS));
        PipeCommand pipeCommand = new PipeCommand(callCommands);
        pipeCommand.evaluate(inputStream, outputStream);
        assertEquals(SECOND_SUCCESS_MSG, outputStream.toString());
    }

    @Test
    public void run_testPipeTwoApplications_FirstException() throws Exception {
        callCommands.add(new CallCommandStub(argsList, appRunner, FIRST_EXCEPTION));
        callCommands.add(new CallCommandStub(argsList, appRunner, SECOND_SUCCESS));
        PipeCommand pipeCommand = new PipeCommand(callCommands);
        Exception expectedException = assertThrows(ShellException.class, () -> pipeCommand.evaluate(inputStream, outputStream));
        assertTrue(expectedException.getMessage().contains(FIRST_EXCEPTION_MSG));
    }

    @Test
    public void run_testPipeTwoApplications_SecondException() throws Exception {
        callCommands.add(new CallCommandStub(argsList, appRunner, FIRST_SUCCESS));
        callCommands.add(new CallCommandStub(argsList, appRunner, SECOND_EXCEPTION));
        PipeCommand pipeCommand = new PipeCommand(callCommands);
        Exception expectedException = assertThrows(ShellException.class, () -> pipeCommand.evaluate(inputStream, outputStream));
        assertTrue(expectedException.getMessage().contains(SECOND_EXCEPTION_MSG));
    }

    @Test
    public void run_testPipeTwoApplications_BothException() throws Exception {
        callCommands.add(new CallCommandStub(argsList, appRunner, FIRST_EXCEPTION));
        callCommands.add(new CallCommandStub(argsList, appRunner, SECOND_EXCEPTION));
        PipeCommand pipeCommand = new PipeCommand(callCommands);
        Exception expectedException = assertThrows(ShellException.class, () -> pipeCommand.evaluate(inputStream, outputStream));
        assertTrue(expectedException.getMessage().contains(FIRST_EXCEPTION_MSG));
    }
}
