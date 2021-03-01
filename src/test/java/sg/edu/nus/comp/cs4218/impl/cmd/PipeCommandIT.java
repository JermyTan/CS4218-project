package sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;

class PipeCommandIT {

    private static final String ORIGINAL_DIR = Environment.currentDirectory;
    private static final String TEST_DIR = Environment.currentDirectory + File.separator + RESOURCES_PATH + File.separator + "PipeCommandIT";

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";

    private final Path file1 = Paths.get(TEST_DIR, FILE_1);
    private final Path file2 = Paths.get(TEST_DIR, FILE_2);

    private final InputStream stdin = System.in;
    private final OutputStream stdout = System.out;
    private final ApplicationRunner appRunner = new ApplicationRunner();
    private final ArgumentResolver argumentResolver = new ArgumentResolver();
    private PipeCommand command;

    private void buildCommand(List<CallCommand> callCommands) throws ShellException {
        command = new PipeCommand(callCommands);
    }

    @BeforeAll
    static void setupBeforeAll() {
        Environment.currentDirectory = TEST_DIR;
    }

    @AfterAll
    static void tearDownAfterAll() {
        Environment.currentDirectory = ORIGINAL_DIR;
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