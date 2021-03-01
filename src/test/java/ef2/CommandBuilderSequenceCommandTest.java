package ef2;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.SequenceCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.CommandBuilder;

@Disabled
public class CommandBuilderSequenceCommandTest {
    @Test
    void parseCommand_SequenceCommandString_SequenceCommand() {
        assertDoesNotThrow(() -> {
            Command command = CommandBuilder.parseCommand("echo hello world ; cat -n file", new ApplicationRunner());
            assertEquals(SequenceCommand.class, command.getClass());

            List<Command> otherCommands = ((SequenceCommand) command).getCommands();

            assertEquals(2, otherCommands.size());

            assertEquals(CallCommand.class, otherCommands.get(0).getClass());
            assertEquals(List.of("echo", "hello", "world"), ((CallCommand) otherCommands.get(0)).getArgsList());

            assertEquals(CallCommand.class, otherCommands.get(1).getClass());
            assertEquals(List.of("cat", "-n", "file"), ((CallCommand) otherCommands.get(1)).getArgsList());
        });
    }

    @Test
    void parseCommand_SequenceAndPipeCommandString_SequenceCommand() {
        assertDoesNotThrow(() -> {
            Command command = CommandBuilder.parseCommand("echo hello world| grep he ; cat -n file", new ApplicationRunner());
            assertEquals(SequenceCommand.class, command.getClass());

            List<Command> otherCommands = ((SequenceCommand) command).getCommands();

            assertEquals(2, otherCommands.size());

            assertEquals(PipeCommand.class, otherCommands.get(0).getClass());
            List<CallCommand> callCommands = ((PipeCommand) otherCommands.get(0)).getCallCommands();

            assertEquals(2, callCommands.size());

            assertEquals(List.of("echo", "hello", "world"), callCommands.get(0).getArgsList());
            assertEquals(List.of("grep", "he"), callCommands.get(1).getArgsList());

            assertEquals(CallCommand.class, otherCommands.get(1).getClass());
            assertEquals(List.of("cat", "-n", "file"), ((CallCommand) otherCommands.get(1)).getArgsList());
        });
    }
}
