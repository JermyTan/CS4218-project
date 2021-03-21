package sg.edu.nus.comp.cs4218.integration.pipe;

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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;

public class PairwiseWcIT {
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
        Files.writeString(file1, "hello world");

        Files.createFile(file2);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.delete(file1);
        Files.delete(file2);
    }

    @Test
    @DisplayName("wc file1.txt | wc")
    public void evaluate_WcThenWc_CommandExecuted() {
        assertDoesNotThrow(() -> {
            CallCommand command1 = new CallCommand(List.of("wc", FILE_1), appRunner);
            CallCommand command2 = new CallCommand(List.of("wc"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            // Note: this is different from the output from Unix shell
            // `wc file1.txt` output "1\t2\t11 file1.txt\n"
            // 1 line, 4 words, 17 bytes
            assertEquals("1" + CHAR_TAB + "4" + CHAR_TAB + "17" + STRING_NEWLINE, stdout.toString());
        });
    }

    @Test
    @DisplayName("wc file1.txt | cat - file1.txt")
    public void evaluate_WcThenCat_CommandExecuted() {
        assertDoesNotThrow(() -> {
            CallCommand command1 = new CallCommand(List.of("wc", FILE_1), appRunner);
            CallCommand command2 = new CallCommand(List.of("cat", "-", FILE_1), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            // 1 line, 2 words, 11 bytes
            assertEquals("1" + CHAR_TAB + "2" + CHAR_TAB + "11" + CHAR_TAB + FILE_1 + STRING_NEWLINE
                    + "hello world" + STRING_NEWLINE, stdout.toString());
        });
    }

    @Test
    @DisplayName("wc file1.txt | grep 1")
    public void evaluate_WcThenGrep_CommandExecuted() {
        assertDoesNotThrow(() -> {
            CallCommand command1 = new CallCommand(List.of("wc", FILE_1), appRunner);
            CallCommand command2 = new CallCommand(List.of("grep", "1"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            assertEquals("1" + CHAR_TAB + "2" + CHAR_TAB + "11" + CHAR_TAB + FILE_1 + STRING_NEWLINE, stdout.toString());
        });
    }

    @Test
    @DisplayName("wc file1.txt | tee")
    public void evaluate_WcThenTee_CommandExecuted() {
        assertDoesNotThrow(() -> {
            CallCommand command1 = new CallCommand(List.of("wc", FILE_1), appRunner);
            CallCommand command2 = new CallCommand(List.of("tee", FILE_2), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            // tee writes to file2
            String expected = "1" + CHAR_TAB + "2" + CHAR_TAB + "11" + CHAR_TAB + FILE_1;
            List<String> output = Files.readAllLines(file2);
            assertEquals(1, output.size());
            assertEquals(expected, output.get(0));

            // tee writes to stdout as well as well
            assertEquals(expected + STRING_NEWLINE, stdout.toString());
        });
    }

    @Test
    @DisplayName("wc file1.txt file2.txt | split -l 1")
    public void evaluate_WcThenSplit_CommandExecuted() {
        assertDoesNotThrow(() -> {
            Files.writeString(file2, "CS4218");
            CallCommand command1 = new CallCommand(List.of("wc", FILE_1, FILE_2), appRunner);
            CallCommand command2 = new CallCommand(List.of("split", "-l", "1"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            // Split into file "xaa" and "xab"
            Path outputFile1 = Path.of(TEST_DIR, "xaa");
            Path outputFile2 = Path.of(TEST_DIR, "xab");
            Path outputFile3 = Path.of(TEST_DIR, "xac");

            List<String> output1 = Files.readAllLines(outputFile1);
            assertEquals(1, output1.size());
            assertEquals("1" + CHAR_TAB + "2" + CHAR_TAB + "11" + CHAR_TAB + FILE_1, output1.get(0));

            List<String> output2 = Files.readAllLines(outputFile2);
            assertEquals(1, output2.size());
            assertEquals("1" + CHAR_TAB + "1" + CHAR_TAB + "6" + CHAR_TAB + FILE_2, output2.get(0));

            List<String> output3 = Files.readAllLines(outputFile3);
            assertEquals(1, output3.size());
            assertEquals("2" + CHAR_TAB + "3" + CHAR_TAB + "17" + CHAR_TAB + "total", output3.get(0));

            Files.delete(outputFile1);
            Files.delete(outputFile2);
            Files.delete(outputFile3);
        });
    }

    @Test
    @DisplayName("wc file1.txt file2.txt | uniq -d")
    public void evaluate_WcThenUniq_CommandExecuted() {
        assertDoesNotThrow(() -> {
            Files.writeString(file2, "hello world");

            CallCommand command1 = new CallCommand(List.of("wc", FILE_1, FILE_2), appRunner);
            CallCommand command2 = new CallCommand(List.of("uniq", "-d"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            // Wc stats are unique, hence no duplicate
            assertEquals("", stdout.toString());
        });
    }

    // TODO: Enable the test when it is fixed
    @Test
    @Disabled
    @DisplayName("wc file1.txt file2.txt | paste")
    public void evaluate_LsThenPaste_CommandExecuted() {
        assertDoesNotThrow(() -> {
            Files.writeString(file2, "CS4218" + STRING_NEWLINE + "Project");

            CallCommand command1 = new CallCommand(List.of("wc", FILE_1), appRunner);
            CallCommand command2 = new CallCommand(List.of("paste", "-", FILE_2), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            assertEquals("1" + CHAR_TAB + "2" + CHAR_TAB + "11" + CHAR_TAB + FILE_1 + CHAR_TAB + "CS4218" + STRING_NEWLINE
                    + CHAR_TAB + "Project" + STRING_NEWLINE, stdout.toString());
        });
    }
}
