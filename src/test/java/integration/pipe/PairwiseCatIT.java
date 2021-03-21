package integration.pipe;

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

public class PairwiseCatIT {
    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + RESOURCES_PATH + STRING_FILE_SEP + "PipePairwiseIT";

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";
    private static final String CAT = "cat";
    private static final String INPUT_STRING = "hello world";
    private final Path file1 = Path.of(TEST_DIR, FILE_1);
    private final Path file2 = Path.of(TEST_DIR, FILE_2);
    private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    private final ApplicationRunner appRunner = new ApplicationRunner();
    private InputStream stdin = System.in;
    private PipeCommand command;

    @BeforeAll
    static void setupBeforeAll() {
        EnvironmentUtil.currentDirectory = TEST_DIR;
    }

    @AfterAll
    static void tearDownAfterAll() {
        EnvironmentUtil.currentDirectory = ORIGINAL_DIR;
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
    @DisplayName("cat file1.txt | wc")
    public void evaluate_CatThenWc_CommandExecuted() {
        assertDoesNotThrow(() -> {
            String expected = INPUT_STRING;
            Files.writeString(file1, expected);

            CallCommand command1 = new CallCommand(List.of(CAT, FILE_1), appRunner);
            CallCommand command2 = new CallCommand(List.of("wc", "-c", FILE_2), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            // stdin is not read
            // file2.txt is empty
            assertEquals("0" + CHAR_TAB + FILE_2 + STRING_NEWLINE, stdout.toString());
        });
    }

    @Test
    @DisplayName("cat -n file1.txt | cat -n -")
    public void evaluate_CatThenCat_CommandExecuted() {
        assertDoesNotThrow(() -> {
            String expected = INPUT_STRING;
            Files.writeString(file1, expected);

            CallCommand command1 = new CallCommand(List.of(CAT, "-n", FILE_1), appRunner);
            CallCommand command2 = new CallCommand(List.of("cat", "-n", "-"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            assertEquals("1 1 " + expected + STRING_NEWLINE, stdout.toString());
        });
    }

    @Test
    @DisplayName("cat file1.txt | grep hello > file2.txt")
    public void evaluate_CatThenGrep_CommandExecuted() {
        assertDoesNotThrow(() -> {
            String expected = INPUT_STRING;
            Files.writeString(file1, expected);

            CallCommand command1 = new CallCommand(List.of(CAT, FILE_1), appRunner);
            CallCommand command2 = new CallCommand(List.of("grep", "hello", ">", FILE_2), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            // Grep output is stored in file2.txt
            List<String> output = Files.readAllLines(file2);
            assertEquals(1, output.size());
            assertEquals(expected, output.get(0));
        });
    }

    @Test
    @DisplayName("cat file1.txt | uniq -d -")
    public void evaluate_CatThenUniq_CommandExecuted() {
        assertDoesNotThrow(() -> {
            String expected1 = INPUT_STRING;
            String expected2 = "Alice";
            Files.writeString(file1, String.join(STRING_NEWLINE, expected1, expected1, expected2, expected2, "Bob", expected2));
            CallCommand command1 = new CallCommand(List.of(CAT, FILE_1), appRunner);
            CallCommand command2 = new CallCommand(List.of("uniq", "-dD"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            assertEquals(String.join(STRING_NEWLINE, expected1, expected1, expected2, expected2) + STRING_NEWLINE, stdout.toString());
        });
    }

    @Test
    @DisplayName("cat | paste file1.txt -")
    public void evaluate_EchoThenPaste_CommandExecuted() {
        assertDoesNotThrow(() -> {
            provideInput(INPUT_STRING);
            Files.writeString(file1, String.join(STRING_NEWLINE, "A", "B"));

            CallCommand command1 = new CallCommand(List.of(CAT), appRunner);
            CallCommand command2 = new CallCommand(List.of("paste", FILE_1, "-"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            assertEquals("A" + CHAR_TAB + INPUT_STRING + STRING_NEWLINE
                    + "B" + STRING_NEWLINE, stdout.toString());
        });
    }
}
