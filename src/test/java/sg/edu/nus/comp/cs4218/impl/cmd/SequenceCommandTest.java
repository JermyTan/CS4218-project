package sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.*;
import org.mockito.*;
import sg.edu.nus.comp.cs4218.*;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.util.*;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SequenceCommandTest {

    private final InputStream stdin = mock(InputStream.class);
    private final OutputStream stdout = mock(OutputStream.class);

    @Test
    public void initialization_NullCommands_ThrowsException() {
        assertThrows(ShellException.class, () -> new SequenceCommand(null));
    }

    @Test
    public void initialization_EmptyCommands_ThrowsException() {
        assertThrows(ShellException.class, () -> new SequenceCommand(List.of()));
    }

    @Test
    public void initialization_CommandListContainsNull_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            List<Command> list = new ArrayList<>();
            list.add(mock(CallCommand.class));
            list.add(null);
            list.add(mock(PipeCommand.class));

            new SequenceCommand(list);
        });
    }

    @Test
    public void evaluate_CallCommand_CommandsExecuted() {
        assertDoesNotThrow(() -> {
            ApplicationRunner appRunner = mock(ApplicationRunner.class);
            CallCommand callCommand = spy(new CallCommand(List.of("echo", "abc"), appRunner));
            SequenceCommand command = new SequenceCommand(List.of(callCommand));

            command.evaluate(stdin, stdout);
            verify(appRunner).runApp(eq("echo"), eq(new String[]{"abc"}), eq(stdin), any());
        });
    }

    @Test
    public void evaluate_PipeCommand_CommandsExecuted() {
        assertDoesNotThrow(() -> {
            CallCommand command1 = spy(new CallCommand(List.of("echo", "hello"), new ApplicationRunner()));
            CallCommand command2 = mock(CallCommand.class);

            PipeCommand pipeCommand = new PipeCommand(List.of(command1, command2));

            SequenceCommand command = new SequenceCommand(List.of(pipeCommand));

            command.evaluate(stdin, stdout);
            ArgumentCaptor<OutputStream> command1Stdout = ArgumentCaptor.forClass(OutputStream.class);
            ArgumentCaptor<InputStream> command2Stdin = ArgumentCaptor.forClass(InputStream.class);

            // Both commands are executed
            verify(command1).evaluate(eq(stdin), command1Stdout.capture());
            verify(command2).evaluate(command2Stdin.capture(), any());

            List<String> input = IOUtils.getLinesFromInputStream(command2Stdin.getValue());

            // Check that data ("hello") written to the stdout of command1 is fed into the stdin of command2
            assertEquals(1, input.size());
            assertEquals("hello", input.get(0));
        });
    }
}
