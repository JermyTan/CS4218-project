package integration.pipe;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_TAB;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;

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

public class PairwiseLsIT {
    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + RESOURCES_PATH + STRING_FILE_SEP + "PipePairwiseIT";

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";

    private final Path file1 = Path.of(TEST_DIR, FILE_1);
    private final Path file2 = Path.of(TEST_DIR, FILE_2);

    private final InputStream stdin = System.in;
    private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    private final ApplicationRunner appRunner = new ApplicationRunner();
    private PipeCommand command;

    @BeforeAll
    static void setupBeforeAll() {
        EnvironmentUtil.currentDirectory = TEST_DIR;
    }

    @AfterAll
    static void tearDownAfterAll() {
        EnvironmentUtil.currentDirectory = ORIGINAL_DIR;
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
    @DisplayName("ls | wc -l")
    public void evaluate_LsThenWc_CommandExecuted() {
        assertDoesNotThrow(() -> {
            CallCommand command1 = new CallCommand(List.of("ls"), appRunner);
            CallCommand command2 = new CallCommand(List.of("wc", "-l"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            // output 2 as only 2 files are present in the directory
            assertEquals("2" + STRING_NEWLINE, stdout.toString());
        });
    }

    @Test
    @DisplayName("ls | cat -n")
    public void evaluate_LsThenCat_CommandExecuted() {
        assertDoesNotThrow(() -> {
            CallCommand command1 = new CallCommand(List.of("ls"), appRunner);
            CallCommand command2 = new CallCommand(List.of("cat", "-n"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            assertEquals("1 " + FILE_1 + STRING_NEWLINE + "2 " + FILE_2 + STRING_NEWLINE, stdout.toString());
        });
    }

    @Test
    @DisplayName("ls | grep file")
    public void evaluate_LsThenGrep_CommandExecuted() {
        assertDoesNotThrow(() -> {
            CallCommand command1 = new CallCommand(List.of("ls"), appRunner);
            CallCommand command2 = new CallCommand(List.of("grep", "file"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            assertEquals(FILE_1 + STRING_NEWLINE + FILE_2 + STRING_NEWLINE, stdout.toString());
        });
    }

    @Test
    @DisplayName("ls | tee")
    public void evaluate_LsThenTee_CommandExecuted() {
        assertDoesNotThrow(() -> {
            CallCommand command1 = new CallCommand(List.of("ls"), appRunner);
            CallCommand command2 = new CallCommand(List.of("tee"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            assertEquals(FILE_1 + STRING_NEWLINE + FILE_2 + STRING_NEWLINE, stdout.toString());
        });
    }

    @Test
    @DisplayName("ls | split -l 1")
    public void evaluate_LsThenSplit_CommandExecuted() {
        assertDoesNotThrow(() -> {
            CallCommand command1 = new CallCommand(List.of("ls"), appRunner);
            CallCommand command2 = new CallCommand(List.of("split", "-l", "1"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            // Split into file "xaa" and "xab"
            Path outputFile1 = Path.of(TEST_DIR, "xaa");
            Path outputFile2 = Path.of(TEST_DIR, "xab");

            List<String> output1 = Files.readAllLines(outputFile1);
            assertEquals(1, output1.size());
            assertEquals(FILE_1, output1.get(0));

            List<String> output2 = Files.readAllLines(outputFile2);
            assertEquals(1, output2.size());
            assertEquals(FILE_2, output2.get(0));

            Files.delete(outputFile1);
            Files.delete(outputFile2);
        });
    }

    @Test
    @DisplayName("ls | uniq -D")
    public void evaluate_LsThenUniq_CommandExecuted() {
        assertDoesNotThrow(() -> {
            CallCommand command1 = new CallCommand(List.of("ls"), appRunner);
            CallCommand command2 = new CallCommand(List.of("uniq", "-D"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            // Expect empty string as all file names are unique
            assertEquals("", stdout.toString());
        });
    }

    @Test
    @DisplayName("ls | paste -s")
    public void evaluate_LsThenPaste_CommandExecuted() {
        assertDoesNotThrow(() -> {
            CallCommand command1 = new CallCommand(List.of("ls"), appRunner);
            CallCommand command2 = new CallCommand(List.of("paste", "-s"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            assertEquals(FILE_1 + CHAR_TAB + FILE_2 + STRING_NEWLINE, stdout.toString());
        });
    }
}
