package sg.edu.nus.comp.cs4218.integration.sequence;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.*;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.cmd.*;
import sg.edu.nus.comp.cs4218.impl.util.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.*;

public class SequenceCommandIT {

    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + RESOURCES_PATH + STRING_FILE_SEP + "SequenceCommandIT";
    private static final Path TEST_PATH = Path.of(TEST_DIR);

    private static final String FOLDER_1 = "folder1";
    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";

    private static final String INPUT_STRING = "hello world";
    private static final String GREP_PATTERN = "hello";

    private final Path folder1 = Path.of(TEST_DIR, FOLDER_1);
    private final Path file1 = Path.of(TEST_DIR, FILE_1);
    private final Path file2 = Path.of(TEST_DIR, FILE_2);

    private final List<Path> paths = List.of(file1, file2, folder1);

    private final InputStream stdin = System.in;
    private final OutputStream stdout = new ByteArrayOutputStream();
    private final ApplicationRunner appRunner = new ApplicationRunner();
    private SequenceCommand command;

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

    private void buildCommand(List<Command> commands) throws ShellException {
        command = new SequenceCommand(commands);
    }

    @BeforeEach
    void setUp() throws IOException {
        Files.createFile(file1);
        Files.createFile(file2);
        Files.createDirectory(folder1);
    }

    @AfterEach
    void tearDown() throws IOException {
        for (Path path : paths) {
            if (Files.isDirectory(path)) {
                Files.walk(path)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } else {
                Files.deleteIfExists(path);
            }
        }
    }

    @Test
    @DisplayName("mv file1.txt file2.txt; grep hello file2.txt")
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
    @DisplayName("echo hello world | tee file1.txt; grep hello file1.txt")
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
    @DisplayName("cd folder1; rm ../file1.txt; cd ..")
    public void evaluate_ThreeCommands_CommandsExecuted() {
        assertDoesNotThrow(() -> {
            Files.writeString(file1, INPUT_STRING);

            CallCommand command1 = new CallCommand(List.of("cd", FOLDER_1), appRunner);
            CallCommand command2 = new CallCommand(List.of("rm", "../file1.txt"), appRunner);
            CallCommand command3 = new CallCommand(List.of("cd", "./.."), appRunner);

            buildCommand(List.of(command1, command2, command3));

            command.evaluate(stdin, stdout);

            assertTrue(Files.notExists(file1));
            assertEquals(TEST_DIR, EnvironmentUtil.currentDirectory);

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
