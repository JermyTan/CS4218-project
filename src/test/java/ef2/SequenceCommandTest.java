package ef2;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.SequenceCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

@Disabled
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
            verify(appRunner).runApp("echo", new String[]{"abc"}, stdin, stdout);
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
            verify(command2).evaluate(command2Stdin.capture(), eq(stdout));

            List<String> input = IOUtils.getLinesFromInputStream(command2Stdin.getValue());

            // Check that data ("hello") written to the stdout of command1 is fed into the stdin of command2
            assertEquals(1, input.size());
            assertEquals("hello", input.get(0));
        });
    }
}
