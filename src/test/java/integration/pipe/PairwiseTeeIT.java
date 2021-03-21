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

public class PairwiseTeeIT {
    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + RESOURCES_PATH + STRING_FILE_SEP + "PipePairwiseIT";

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";

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
