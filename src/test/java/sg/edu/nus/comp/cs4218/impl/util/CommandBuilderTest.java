package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_BLANK;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_EMPTY;

import java.util.List;

import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
class CommandBuilderTest {
    @Test
    void parseCommand_EmptyString_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            CommandBuilder.parseCommand(STRING_EMPTY, new ApplicationRunner());
        });
    }

    @Test
    void parseCommand_BlankString_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            CommandBuilder.parseCommand(STRING_BLANK, new ApplicationRunner());
        });
    }

    @Test
    void parseCommand_SequentialPipes_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            CommandBuilder.parseCommand("||||", new ApplicationRunner());
        });
    }

    @Test
    void parseCommand_SemicolonBeforeCommand_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            CommandBuilder.parseCommand(";wc", new ApplicationRunner());
        });
    }

    @Test
    void parseCommand_EmptyInputPipe_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            CommandBuilder.parseCommand("|cat", new ApplicationRunner());
        });
    }

    @Test
    void parseCommand_HasNewline_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            CommandBuilder.parseCommand("ls \n", new ApplicationRunner());
        });
    }

    @Test
    void parseCommand_IncompleteQuote_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            CommandBuilder.parseCommand("echo \"a ", new ApplicationRunner());
        });
    }

    @Test
    void parseCommand_CallCommandString_CallCommand() {
        assertDoesNotThrow(() -> {
            Command command = CommandBuilder.parseCommand("  echo hello world", new ApplicationRunner());
            assertEquals(CallCommand.class, command.getClass());
            assertEquals(List.of("echo", "hello", "world"), ((CallCommand) command).getArgsList());
        });
    }

    @Test
    void parseCommand_CallCommandStringWithRedirectIn_CallCommand() {
        assertDoesNotThrow(() -> {
            Command command = CommandBuilder.parseCommand("echo test > file", new ApplicationRunner());
            assertEquals(CallCommand.class, command.getClass());
            assertEquals(List.of("echo", "test", ">", "file"), ((CallCommand) command).getArgsList());
        });
    }

    @Test
    void parseCommand_CallCommandStringWithRedirectOut_CallCommand() {
        assertDoesNotThrow(() -> {
            Command command = CommandBuilder.parseCommand("echo < file", new ApplicationRunner());
            assertEquals(CallCommand.class, command.getClass());
            assertEquals(List.of("echo", "<", "file"), ((CallCommand) command).getArgsList());
        });
    }

    @Test
    void parseCommand_PipeCommandString_PipeCommand() {
        assertDoesNotThrow(() -> {
            Command command = CommandBuilder.parseCommand("echo hello | grep he", new ApplicationRunner());
            assertEquals(PipeCommand.class, command.getClass());

            List<CallCommand> callCommands = ((PipeCommand) command).getCallCommands();

            assertEquals(2, callCommands.size());
            assertEquals(List.of("echo", "hello"), callCommands.get(0).getArgsList());
            assertEquals(List.of("grep", "he"), callCommands.get(1).getArgsList());
        });
    }
}