package sg.edu.nus.comp.cs4218.integration.pipe;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_TAB;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

public class PairwiseGrepIT {
    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + RESOURCES_PATH + STRING_FILE_SEP + "PipePairwiseIT";
    private static final Path TEST_PATH = Path.of(TEST_DIR);

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";
    private static final String GREP = "grep";
    private static final String INPUT_STRING = "hello world";
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
    @DisplayName("grep hello file1.txt | wc -cl")
    public void evaluate_GrepThenWc_CommandExecuted() {
        assertDoesNotThrow(() -> {
            String expected = INPUT_STRING;
            Files.writeString(file1, expected);

            CallCommand command1 = new CallCommand(List.of(GREP, "hello", FILE_1), appRunner);
            CallCommand command2 = new CallCommand(List.of("wc", "-cl"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            assertEquals("1" + CHAR_TAB + "12" + STRING_NEWLINE, stdout.toString());
        });
    }

    @Test
    @DisplayName("grep hello file1.txt | grep hello")
    public void evaluate_GrepThenGrep_CommandExecuted() {
        assertDoesNotThrow(() -> {
            String expected = INPUT_STRING;
            Files.writeString(file1, expected);

            CallCommand command1 = new CallCommand(List.of(GREP, "hello", FILE_1), appRunner);
            CallCommand command2 = new CallCommand(List.of(GREP, "world"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            assertEquals(expected + STRING_NEWLINE, stdout.toString());
        });
    }

    @Test
    @DisplayName("uniq file1.txt | grep hello")
    public void evaluate_UniqThenGrep_CommandExecuted() {
        assertDoesNotThrow(() -> {
            String expected1 = INPUT_STRING;
            String expected2 = "Alice";
            Files.writeString(file1, String.join(STRING_NEWLINE, expected1, expected1,
                    expected2, expected2, "Bob", expected2));

            CallCommand command1 = new CallCommand(List.of("uniq", "-c", FILE_1), appRunner);
            CallCommand command2 = new CallCommand(List.of(GREP, "2"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            assertEquals("2 " + expected1 + STRING_NEWLINE + "2 " + expected2 + STRING_NEWLINE, stdout.toString());
        });
    }

    @Test
    @DisplayName("paste - | grep hello")
    public void evaluate_PasteThenGrep_CommandExecuted() {
        provideInput(String.join(STRING_NEWLINE, "A", "B", "C", "D"));

        assertDoesNotThrow(() -> {
            Files.writeString(file1, String.join(STRING_NEWLINE, "1", "2", "3", "4"));

            CallCommand command1 = new CallCommand(List.of("paste", "-", FILE_1), appRunner);
            CallCommand command2 = new CallCommand(List.of(GREP, "A"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            assertEquals("A" + CHAR_TAB + "1" + STRING_NEWLINE, stdout.toString());
        });
    }
}
