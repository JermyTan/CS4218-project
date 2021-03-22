package sg.edu.nus.comp.cs4218.integration.pipe;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.*;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.cmd.*;
import sg.edu.nus.comp.cs4218.impl.util.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.*;

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