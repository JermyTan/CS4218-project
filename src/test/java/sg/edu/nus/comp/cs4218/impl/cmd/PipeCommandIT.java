package sg.edu.nus.comp.cs4218.impl.cmd;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

class PipeCommandIT {

    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + RESOURCES_PATH + STRING_FILE_SEP + "PipeCommandIT";

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";

    private final Path file1 = Paths.get(TEST_DIR, FILE_1);
    private final Path file2 = Paths.get(TEST_DIR, FILE_2);

    private final InputStream stdin = System.in;
    private final OutputStream stdout = System.out;
    private final ApplicationRunner appRunner = new ApplicationRunner();
    private final ArgumentResolver argumentResolver = new ArgumentResolver();
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
    public void evaluate_CatThenGrep_CommandExecuted() throws IOException {
        Files.writeString(file1, "hello world");

        assertDoesNotThrow(() -> {
            CallCommand command1 = new CallCommand(List.of("cat", FILE_1), appRunner, argumentResolver);
            CallCommand command2 = new CallCommand(List.of("grep", "hello", ">", FILE_2), appRunner, argumentResolver);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            // Grep output is stored in file2.txt
            List<String> output = Files.readAllLines(file2);
            assertEquals(1, output.size());
            assertEquals("hello world", output.get(0));
        });
    }

    @Test
    public void evaluate_ExceptionOccursInOneCommand_RestTerminated() {
        assertThrows(ShellException.class, () -> {
            CallCommand command1 = new CallCommand(List.of("echo", "abc", ">", FILE_1), appRunner, argumentResolver);
            CallCommand command2 = new CallCommand(List.of("lsa"), appRunner, argumentResolver); //Invalid command
            CallCommand command3 = new CallCommand(List.of("echo", "hello", "world"), appRunner, argumentResolver);

            buildCommand(List.of(command1, command2, command3));

            command.evaluate(stdin, stdout);

            // Only the first command executed
            List<String> output = Files.readAllLines(file1);
            assertEquals(1, output.size());
            assertEquals("abc", output.get(0));
        });
    }

}