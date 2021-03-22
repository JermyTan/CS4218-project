package sg.edu.nus.comp.cs4218.integration.pipe;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_TAB;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;

class PipeCommandIT {

    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + RESOURCES_PATH + STRING_FILE_SEP + "PipeCommandIT";
    private static final Path TEST_PATH = Path.of(TEST_DIR);

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";

    private final Path file1 = Path.of(TEST_DIR, FILE_1);
    private final Path file2 = Path.of(TEST_DIR, FILE_2);

    private final InputStream stdin = System.in;
    private final OutputStream stdout = new ByteArrayOutputStream();
    private final ApplicationRunner appRunner = new ApplicationRunner();
    private PipeCommand command;

    @BeforeAll
    static void setupBeforeAll() throws IOException {
        if (Files.notExists(TEST_PATH)) {
            Files.createDirectory(TEST_PATH);
        }
        EnvironmentUtil.currentDirectory = TEST_DIR;
    }

    @AfterAll
    static void tearDownAfterAll() throws IOException {
        EnvironmentUtil.currentDirectory = ORIGINAL_DIR;
        Files.delete(TEST_PATH);
    }

    private void buildCommand(List<CallCommand> callCommands) throws ShellException {
        command = new PipeCommand(callCommands);
    }

    @BeforeEach
    void setUp() throws IOException {
        Files.createFile(file1);
        Files.createFile(file2);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.delete(file1);
        Files.delete(file2);
    }

    @Test
    @DisplayName("cat file1.txt | grep hello > file2.txt")
    public void evaluate_TwoPipes_CommandExecuted() {
        assertDoesNotThrow(() -> {
            Files.writeString(file1, "hello world");

            CallCommand command1 = new CallCommand(List.of("cat", FILE_1), appRunner);
            CallCommand command2 = new CallCommand(List.of("grep", "hello", ">", FILE_2), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            // Grep output is stored in file2.txt
            List<String> output = Files.readAllLines(file2);
            assertEquals(1, output.size());
            assertEquals("hello world", output.get(0));

            assertEquals(STRING_EMPTY, stdout.toString());
        });
    }

    @Test
    @DisplayName("cat file1.txt | wc - | grep 1 > file2.txt")
    public void evaluate_ThreePipes_CommandExecuted() {
        assertDoesNotThrow(() -> {
            Files.writeString(file1, "hello world");

            CallCommand command1 = new CallCommand(List.of("cat", FILE_1), appRunner);
            CallCommand command2 = new CallCommand(List.of("wc", "-"), appRunner);
            CallCommand command3 = new CallCommand(List.of("grep", "1", ">", FILE_2), appRunner);

            buildCommand(List.of(command1, command2, command3));

            command.evaluate(stdin, stdout);

            // Grep output is stored in file2.txt
            List<String> output = Files.readAllLines(file2);
            assertEquals(1, output.size());
            assertEquals("1" + CHAR_TAB + "2" + CHAR_TAB + "12", output.get(0));
        });
    }

    @Test
    public void evaluate_ExceptionOccursInOneCommand_RestTerminated() {
        assertThrows(ShellException.class, () -> {
            CallCommand command1 = new CallCommand(List.of("echo", "abc", ">", FILE_1), appRunner);
            CallCommand command2 = new CallCommand(List.of("lsa"), appRunner); //Invalid command
            CallCommand command3 = new CallCommand(List.of("echo", "hello", "world"), appRunner);

            buildCommand(List.of(command1, command2, command3));

            command.evaluate(stdin, stdout);

            // Only the first command executed
            List<String> output = Files.readAllLines(file1);
            assertEquals(1, output.size());
            assertEquals("abc", output.get(0));

            // The third command is not executed
            assertEquals("", stdout.toString());
        });
    }

}