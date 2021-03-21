package sg.edu.nus.comp.cs4218.integration.sequence;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_FILE_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
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
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.SequenceCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;

public class SequenceCommandIT {

    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + RESOURCES_PATH + STRING_FILE_SEP + "SequenceCommandIT";

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";
    private static final String INPUT_STRING = "hello world";
    private static final String GREP_PATTERN = "hello";
    private final Path file1 = Path.of(TEST_DIR, FILE_1);
    private final Path file2 = Path.of(TEST_DIR, FILE_2);
    private final InputStream stdin = System.in;
    private final OutputStream stdout = new ByteArrayOutputStream();
    private final ApplicationRunner appRunner = new ApplicationRunner();
    private SequenceCommand command;

    @BeforeAll
    static void setupBeforeAll() {
        EnvironmentUtil.currentDirectory = TEST_DIR;
    }

    @AfterAll
    static void tearDownAfterAll() {
        EnvironmentUtil.currentDirectory = ORIGINAL_DIR;
    }

    private void buildCommand(List<Command> commands) throws ShellException {
        command = new SequenceCommand(commands);
    }

    @BeforeEach
    void setUp() throws IOException {
        Files.createFile(file1);
        Files.createFile(file2);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(file1);
        Files.deleteIfExists(file2);
    }

    @Test
    public void evaluate_TwoCommandsSameType_CommandsExecuted() {
        assertDoesNotThrow(() -> {
            Files.writeString(file1, INPUT_STRING);

            CallCommand command1 = new CallCommand(List.of("mv", FILE_1, FILE_2), appRunner);
            CallCommand command2 = new CallCommand(List.of("grep", GREP_PATTERN, FILE_2), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            // Grep output is stored in file2.txt
            List<String> output = Files.readAllLines(file2);
            assertEquals(1, output.size());
            assertEquals(INPUT_STRING, output.get(0));
        });
    }

    @Test
    public void evaluate_TwoCommandsDifferentTypes_CommandsExecuted() {
        assertDoesNotThrow(() -> {
            String expected = INPUT_STRING;

            PipeCommand command1 = new PipeCommand(List.of(
                    new CallCommand(List.of("echo", expected), appRunner),
                    new CallCommand(List.of("tee", FILE_1), appRunner)));
            CallCommand command2 = new CallCommand(List.of("grep", GREP_PATTERN, FILE_1), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            // Grep output is stored in file2.txt
            List<String> output = Files.readAllLines(file1);
            assertEquals(1, output.size());
            assertEquals(expected, output.get(0));
        });
    }

    @Test
    public void evaluate_ExceptionThrownInFirstCommand_ExecutionContinues() {
        assertDoesNotThrow(() -> {
            Files.writeString(file1, INPUT_STRING);
            CallCommand command1 = new CallCommand(List.of("rm"), appRunner); // No files specified
            CallCommand command2 = new CallCommand(List.of("echo", INPUT_STRING), appRunner);
            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            assertEquals(new RmException(ERR_NO_FILE_ARGS).getMessage() + STRING_NEWLINE
                    + INPUT_STRING + STRING_NEWLINE, stdout.toString());
        });

    }
}
