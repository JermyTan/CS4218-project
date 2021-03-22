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

public class PairwiseTeeIT {
    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + RESOURCES_PATH + STRING_FILE_SEP + "PipePairwiseIT";
    private static final Path TEST_PATH = Path.of(TEST_DIR);

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";

    private final Path file1 = Path.of(TEST_DIR, FILE_1);
    private final Path file2 = Path.of(TEST_DIR, FILE_2);
    private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    private final ApplicationRunner appRunner = new ApplicationRunner();
    private InputStream stdin = System.in;
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

    private void provideInput(String input) {
        stdin = new ByteArrayInputStream(input.getBytes());
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
    @DisplayName("tee file1.txt | wc file1.txt -")
    public void evaluate_TeeThenWc_CommandExecuted() {
        assertDoesNotThrow(() -> {
            String expected = "hello world";
            provideInput(expected);

            CallCommand command1 = new CallCommand(List.of("tee", FILE_1), appRunner);
            CallCommand command2 = new CallCommand(List.of("wc", FILE_1, "-"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            // Tee writes user input to file1.txt
            List<String> output = Files.readAllLines(file1);
            assertEquals(1, output.size());
            assertEquals(expected, output.get(0));

            // wc read from both stdin and file1.txt
            assertEquals(String.join(
                    STRING_NEWLINE,
                    "1" + CHAR_TAB + "2" + CHAR_TAB + "12" + CHAR_TAB + FILE_1,
                    "1" + CHAR_TAB + "2" + CHAR_TAB + "12",
                    "2" + CHAR_TAB + "4" + CHAR_TAB + "24" + CHAR_TAB + "total"
            ) + STRING_NEWLINE, stdout.toString());
        });
    }

    @Test
    @DisplayName("tee file1.txt | grep hello")
    public void evaluate_TeeThenGrep_CommandExecuted() {
        assertDoesNotThrow(() -> {
            String expected = "hello world";
            provideInput(expected);

            CallCommand command1 = new CallCommand(List.of("tee", FILE_1), appRunner);
            CallCommand command2 = new CallCommand(List.of("grep", "hello"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            // Tee writes user input to file1.txt
            List<String> output = Files.readAllLines(file1);
            assertEquals(1, output.size());
            assertEquals(expected, output.get(0));

            // Grep output
            assertEquals(expected + STRING_NEWLINE, stdout.toString());
        });
    }
}
