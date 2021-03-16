package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_BLANK;

import java.util.List;

import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;

class CommandBuilderHelperTest {
    @Test
    void parseCommand_EmptyString_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            CommandBuilderHelper.parseCommand(STRING_EMPTY, new ApplicationRunner());
        });
    }

    @Test
    void parseCommand_BlankString_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            CommandBuilderHelper.parseCommand(STRING_BLANK, new ApplicationRunner());
        });
    }

    @Test
    void parseCommand_SequentialPipes_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            CommandBuilderHelper.parseCommand("||||", new ApplicationRunner());
        });
    }

    @Test
    void parseCommand_SemicolonBeforeCommand_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            CommandBuilderHelper.parseCommand(";wc", new ApplicationRunner());
        });
    }

    @Test
    void parseCommand_EmptyInputPipe_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            CommandBuilderHelper.parseCommand("|cat", new ApplicationRunner());
        });
    }

    @Test
    void parseCommand_HasNewline_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            CommandBuilderHelper.parseCommand("ls \n", new ApplicationRunner());
        });
    }

    @Test
    void parseCommand_IncompleteQuote_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            CommandBuilderHelper.parseCommand("echo \"a ", new ApplicationRunner());
        });
    }

    @Test
    void parseCommand_CallCommandString_CallCommand() {
        assertDoesNotThrow(() -> {
            Command command = CommandBuilderHelper.parseCommand("  echo hello world", new ApplicationRunner());
            assertEquals(CallCommand.class, command.getClass());
            assertEquals(List.of("echo", "hello", "world"), ((CallCommand) command).getArgsList());
        });
    }

    @Test
    void parseCommand_CallCommandStringWithRedirectIn_CallCommand() {
        assertDoesNotThrow(() -> {
            Command command = CommandBuilderHelper.parseCommand("cat test > file", new ApplicationRunner());
            assertEquals(CallCommand.class, command.getClass());
            assertEquals(List.of("cat", "test", ">", "file"), ((CallCommand) command).getArgsList());
        });
    }

    @Test
    void parseCommand_CallCommandStringWithRedirectOut_CallCommand() {
        assertDoesNotThrow(() -> {
            Command command = CommandBuilderHelper.parseCommand("grep < file", new ApplicationRunner());
            assertEquals(CallCommand.class, command.getClass());
            assertEquals(List.of("grep", "<", "file"), ((CallCommand) command).getArgsList());
        });
    }

    @Test
    void parseCommand_PipeCommandString_PipeCommand() {
        assertDoesNotThrow(() -> {
            Command command = CommandBuilderHelper.parseCommand("echo hello | grep he", new ApplicationRunner());
            assertEquals(PipeCommand.class, command.getClass());

            List<CallCommand> callCommands = ((PipeCommand) command).getCallCommands();

            assertEquals(2, callCommands.size());
            assertEquals(List.of("echo", "hello"), callCommands.get(0).getArgsList());
            assertEquals(List.of("grep", "he"), callCommands.get(1).getArgsList());
        });
    }
}