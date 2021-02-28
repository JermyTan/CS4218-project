package sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PipeCommandTest {

    private PipeCommand command;

    private final InputStream stdin = mock(InputStream.class);
    private final OutputStream stdout = mock(OutputStream.class);

    private void buildCommand(List<CallCommand> callCommands) {
        command = new PipeCommand(callCommands);
    }

    @Test
    public void evaluate_ZeroCallCommand_DoesNothing() {
        buildCommand(new ArrayList<>());

        assertDoesNotThrow(() -> command.evaluate(stdin, stdout));
    }

    @Test
    public void evaluate_TwoCallCommands_CommandsExecuted() {
        CallCommand command1 = spy(new CallCommand(List.of("echo", "abc"), new ApplicationRunner(), new ArgumentResolver()));
        CallCommand command2 = mock(CallCommand.class);

        buildCommand(List.of(command1, command2));

        assertDoesNotThrow(() -> {
            command.evaluate(stdin, stdout);
            ArgumentCaptor<OutputStream> command1Stdout = ArgumentCaptor.forClass(OutputStream.class);
            ArgumentCaptor<InputStream> command2Stdin = ArgumentCaptor.forClass(InputStream.class);

            // Both commands are executed
            verify(command1).evaluate(eq(stdin), command1Stdout.capture());
            verify(command2).evaluate(command2Stdin.capture(), eq(stdout));

            List<String> input = IOUtils.getLinesFromInputStream(command2Stdin.getValue());

            // Check that data ("abc") written to the stdout of command1 is fed into the stdin of command2
            assertEquals(1, input.size());
            assertEquals("abc", input.get(0));
        });
    }

    @Test
    public void evaluate_ShellExceptionThrown_RestTerminated() throws AbstractApplicationException, ShellException {
        CallCommand command1 = mock(CallCommand.class);
        CallCommand command2 = mock(CallCommand.class);
        doThrow(ShellException.class)
                .when(command1)
                .evaluate(eq(stdin), any());

        buildCommand(List.of(command1, command2));

        assertThrows(ShellException.class, () -> {
            command.evaluate(stdin, stdout);
            verify(command1).evaluate(eq(stdin), any());
            verify(command2, never()).evaluate(any(), eq(stdout));
        });
    }

    @Test
    public void evaluate_AppExceptionThrown_RestTerminated() throws AbstractApplicationException, ShellException {
        CallCommand command1 = mock(CallCommand.class);
        CallCommand command2 = mock(CallCommand.class);
        doThrow(CatException.class)
                .when(command1)
                .evaluate(eq(stdin), any());

        buildCommand(List.of(command1, command2));

        assertThrows(CatException.class, () -> {
            command.evaluate(stdin, stdout);
            verify(command1).evaluate(eq(stdin), any());
            verify(command2, never()).evaluate(any(), eq(stdout));
        });
    }
}