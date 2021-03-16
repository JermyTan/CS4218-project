package sg.edu.nus.comp.cs4218.impl.cmd;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_SINGLE_WORD;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

class PipeCommandTest {

    private final InputStream stdin = mock(InputStream.class);
    private final OutputStream stdout = mock(OutputStream.class);

    @Test
    public void initialization_NullCallCommands_ThrowsException() {
        assertThrows(ShellException.class, () -> new PipeCommand(null));
    }

    @Test
    public void initialization_EmptyCallCommands_ThrowsException() {
        assertThrows(ShellException.class, () -> new PipeCommand(List.of()));
    }

    @Test
    public void initialization_OneCallCommand_ThrowsException() {
        assertThrows(ShellException.class, () -> new PipeCommand(List.of(mock(CallCommand.class))));
    }

    @Test
    public void initialization_CallCommandListContainsNull_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            List<CallCommand> list = new ArrayList<>();
            list.add(mock(CallCommand.class));
            list.add(null);
            list.add(mock(CallCommand.class));

            new PipeCommand(list);
        });
    }

    @Test
    public void evaluate_TwoCallCommands_CommandsExecuted() {
        assertDoesNotThrow(() -> {
            CallCommand command1 = spy(new CallCommand(List.of("echo", STRING_SINGLE_WORD), new ApplicationRunner(), new ArgumentResolver()));
            CallCommand command2 = mock(CallCommand.class);

            PipeCommand command = new PipeCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);
            ArgumentCaptor<OutputStream> command1Stdout = ArgumentCaptor.forClass(OutputStream.class);
            ArgumentCaptor<InputStream> command2Stdin = ArgumentCaptor.forClass(InputStream.class);

            // Both commands are executed
            verify(command1).evaluate(eq(stdin), command1Stdout.capture());
            verify(command2).evaluate(command2Stdin.capture(), eq(stdout));

            List<String> input = IOUtils.getLinesFromInputStream(command2Stdin.getValue());

            // Check that data is written to the stdout of command1 is fed into the stdin of command2
            assertEquals(1, input.size());
            assertEquals(STRING_SINGLE_WORD, input.get(0));
        });
    }

    @Test
    public void evaluate_ShellExceptionThrown_RestTerminated() throws AbstractApplicationException, ShellException {
        CallCommand command1 = mock(CallCommand.class);
        CallCommand command2 = mock(CallCommand.class);
        doThrow(ShellException.class)
                .when(command1)
                .evaluate(eq(stdin), any());

        PipeCommand command = new PipeCommand(List.of(command1, command2));

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

        PipeCommand command = new PipeCommand(List.of(command1, command2));

        assertThrows(CatException.class, () -> {
            command.evaluate(stdin, stdout);
            verify(command1).evaluate(eq(stdin), any());
            verify(command2, never()).evaluate(any(), eq(stdout));
        });
    }

    @Test
    public void terminate_BeforeEvaluate_DoesNothing() {
        assertDoesNotThrow(() -> {
            CallCommand command1 = spy(new CallCommand(List.of("echo", STRING_SINGLE_WORD), new ApplicationRunner(), new ArgumentResolver()));
            CallCommand command2 = mock(CallCommand.class);

            PipeCommand command = new PipeCommand(List.of(command1, command2));

            command.terminate();

            command.evaluate(stdin, stdout);
            ArgumentCaptor<OutputStream> command1Stdout = ArgumentCaptor.forClass(OutputStream.class);
            ArgumentCaptor<InputStream> command2Stdin = ArgumentCaptor.forClass(InputStream.class);

            // Both commands are executed
            verify(command1).evaluate(eq(stdin), command1Stdout.capture());
            verify(command2).evaluate(command2Stdin.capture(), eq(stdout));

            List<String> input = IOUtils.getLinesFromInputStream(command2Stdin.getValue());

            // Check that data is written to the stdout of command1 is fed into the stdin of command2
            assertEquals(1, input.size());
            assertEquals(STRING_SINGLE_WORD, input.get(0));
        });
    }

    @Test
    public void getCallCommands_NonEmptyCallCommandList_ReturnsNonEmptyCallCommandList() {
        assertDoesNotThrow(() -> {
            CallCommand command1 = spy(new CallCommand(List.of("echo", STRING_SINGLE_WORD), new ApplicationRunner(), new ArgumentResolver()));
            CallCommand command2 = mock(CallCommand.class);

            List<CallCommand> callCommandList = List.of(command1, command2);

            PipeCommand command = new PipeCommand(callCommandList);

            // test before evaluate
            assertEquals(callCommandList, command.getCallCommands());

            command.evaluate(stdin, stdout);

            // test after evaluation
            assertEquals(callCommandList, command.getCallCommands());
        });
    }
}