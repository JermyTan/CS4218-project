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

public class PairwiseEchoIT {
    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + RESOURCES_PATH + STRING_FILE_SEP + "PipePairwiseIT";
    private static final Path TEST_PATH = Path.of(TEST_DIR);

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";
    private static final String ECHO = "echo";
    private static final String INPUT_STRING = "hello world";
    private final Path file1 = Path.of(TEST_DIR, FILE_1);
    private final Path file2 = Path.of(TEST_DIR, FILE_2);
    private final InputStream stdin = System.in;
    private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
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
    @DisplayName("echo hello world | wc")
    public void evaluate_EchoThenWc_CommandExecuted() {
        assertDoesNotThrow(() -> {
            CallCommand command1 = new CallCommand(List.of(ECHO, "hello", "world"), appRunner);
            CallCommand command2 = new CallCommand(List.of("wc"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            assertEquals("1" + CHAR_TAB + "2" + CHAR_TAB + "12" + STRING_NEWLINE, stdout.toString());
        });
    }

    @Test
    @DisplayName("echo file1.txt | cat")
    public void evaluate_EchoThenCat_CommandExecuted() {
        assertDoesNotThrow(() -> {
            CallCommand command1 = new CallCommand(List.of(ECHO, FILE_1), appRunner);
            CallCommand command2 = new CallCommand(List.of("cat"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            assertEquals(FILE_1 + STRING_NEWLINE, stdout.toString());
        });
    }

    @Test
    @DisplayName("echo hello world | grep hello")
    public void evaluate_EchoThenGrep_CommandExecuted() {
        assertDoesNotThrow(() -> {
            CallCommand command1 = new CallCommand(List.of(ECHO, "hello world"), appRunner);
            CallCommand command2 = new CallCommand(List.of("grep", "hello"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            assertEquals(INPUT_STRING + STRING_NEWLINE, stdout.toString());
        });
    }

    @Test
    @DisplayName("echo \"hello world\" | tee")
    public void evaluate_EchoThenTee_CommandExecuted() {
        assertDoesNotThrow(() -> {
            CallCommand command1 = new CallCommand(List.of(ECHO, "\"hello world\""), appRunner);
            CallCommand command2 = new CallCommand(List.of("tee"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            assertEquals(INPUT_STRING + STRING_NEWLINE, stdout.toString());
        });
    }

    @Test
    @DisplayName("echo hello world\ncs4218 | split -l 1")
    public void evaluate_EchoThenSplit_CommandExecuted() {
        assertDoesNotThrow(() -> {
            CallCommand command1 = new CallCommand(List.of(ECHO, "hello world\ncs4218"), appRunner);
            CallCommand command2 = new CallCommand(List.of("split", "-l", "1"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            // Split into file "xaa" and "xab"
            Path outputFile1 = Path.of(TEST_DIR, "xaa");
            Path outputFile2 = Path.of(TEST_DIR, "xab");

            List<String> output1 = Files.readAllLines(outputFile1);
            assertEquals(1, output1.size());
            assertEquals(INPUT_STRING, output1.get(0));

            List<String> output2 = Files.readAllLines(outputFile2);
            assertEquals(1, output2.size());
            assertEquals("cs4218", output2.get(0));

            Files.delete(outputFile1);
            Files.delete(outputFile2);
        });
    }

    @Test
    @DisplayName("echo <...> | uniq -d -")
    public void evaluate_EchoThenUniq_CommandExecuted() {
        assertDoesNotThrow(() -> {
            String expected1 = INPUT_STRING;
            String expected2 = "Alice";
            CallCommand command1 = new CallCommand(List.of(ECHO,
                    String.join(STRING_NEWLINE, expected1, expected1, expected2, expected2, "Bob", expected2)),
                    appRunner);
            CallCommand command2 = new CallCommand(List.of("uniq", "-d", "-"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            assertEquals(expected1 + STRING_NEWLINE + expected2 + STRING_NEWLINE, stdout.toString());
        });
    }

    @Test
    @DisplayName("echo hello\nworld | paste -s file1.txt -")
    public void evaluate_EchoThenPaste_CommandExecuted() {
        assertDoesNotThrow(() -> {
            Files.writeString(file1, String.join(STRING_NEWLINE, "A", "B"));

            CallCommand command1 = new CallCommand(List.of(ECHO, "hello\nworld"), appRunner);
            CallCommand command2 = new CallCommand(List.of("paste", "-s", FILE_1, "-"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            assertEquals("A" + CHAR_TAB + "B" + STRING_NEWLINE
                    + "hello" + CHAR_TAB + "world" + STRING_NEWLINE, stdout.toString());
        });
    }
}
