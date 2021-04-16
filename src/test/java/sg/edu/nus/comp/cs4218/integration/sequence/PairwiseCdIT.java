package sg.edu.nus.comp.cs4218.integration.sequence;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.SequenceCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;

public class PairwiseCdIT {
    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + RESOURCES_PATH + STRING_FILE_SEP + "SequencePairwiseIT";
    private static final Path TEST_PATH = Path.of(TEST_DIR);

    private static final String FOLDER_1 = "folder1";
    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";

    private final Path file1 = Path.of(TEST_DIR, FILE_1);
    private final Path file2 = Path.of(TEST_DIR, FILE_2);
    private final Path folder1 = Path.of(TEST_DIR, FOLDER_1);

    private final List<Path> paths = List.of(file1, file2, folder1);

    private final InputStream stdin = System.in;
    private final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    private final ApplicationRunner appRunner = new ApplicationRunner();
    private OutputStream stderr;
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

    private void captureErr() {
        stderr = new ByteArrayOutputStream();
        System.setErr(new PrintStream(stderr));
    }

    private String getErrOutput() {
        System.setErr(System.err);
        return stderr.toString();
    }

    @BeforeEach
    void setUp() throws IOException {
        EnvironmentUtil.currentDirectory = TEST_DIR;

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
    @DisplayName("ls .; cd folder1; ls")
    public void evaluate_LsThenCdThenLs_CommandsExecuted() {
        assertDoesNotThrow(() -> {
            // Create a file under folder1
            Path filePath = folder1.resolve(FILE_1);
            Files.createFile(filePath);

            CallCommand command1 = new CallCommand(List.of("ls", "."), appRunner);
            CallCommand command2 = new CallCommand(List.of("cd", FOLDER_1), appRunner);
            CallCommand command3 = new CallCommand(List.of("ls"), appRunner);

            buildCommand(List.of(command1, command2, command3));

            command.evaluate(stdin, stdout);

            assertEquals(String.join(STRING_NEWLINE,
                    FILE_1,
                    FILE_2,
                    FOLDER_1,
                    FILE_1) + STRING_NEWLINE, stdout.toString());

            // Check current directory
            assertEquals(folder1.toString(), EnvironmentUtil.currentDirectory);
        });
    }

    @Test
    @DisplayName("cd folder1; cd ./..")
    public void evaluate_ConsecutiveCd_CommandsExecuted() {
        assertDoesNotThrow(() -> {
            CallCommand command1 = new CallCommand(List.of("cd", FOLDER_1), appRunner);
            CallCommand command2 = new CallCommand(List.of("cd", "./.."), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            assertEquals(TEST_DIR, EnvironmentUtil.currentDirectory);
        });
    }

    @Test
    @DisplayName("cd folder1; mv ../file1.txt .")
    public void evaluate_CdThenMv_CommandsExecuted() {
        assertDoesNotThrow(() -> {
            CallCommand command1 = new CallCommand(List.of("cd", FOLDER_1), appRunner);
            CallCommand command2 = new CallCommand(List.of("mv", "../file1.txt", "."), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            // file1.txt is moved to folder1/
            assertTrue(Files.notExists(file1));
            assertTrue(Files.exists(folder1.resolve(FILE_1)));

            // Check current directory
            assertEquals(folder1.toString(), EnvironmentUtil.currentDirectory);
        });
    }

    @Test
    @DisplayName("cd folder2; mv ../file1.txt (failure)")
    public void evaluate_CdNonExistingDirThenCp_CommandsExecuted() {
        assertDoesNotThrow(() -> {
            CallCommand command1 = new CallCommand(List.of("cd", "folder2"), appRunner);
            CallCommand command2 = new CallCommand(List.of("mv", "./../file1.txt", FILE_1), appRunner);

            buildCommand(List.of(command1, command2));

            captureErr();
            command.evaluate(stdin, stdout);

            // file1.txt is not moved to folder1/ as the cd command fails
            assertTrue(Files.exists(file1));
            assertTrue(Files.notExists(folder1.resolve(FILE_1)));

            // Check current directory (remains unchanged)
            assertEquals(TEST_DIR, EnvironmentUtil.currentDirectory);

            // Both errors written to output
            assertEquals(String.join(STRING_NEWLINE,
                    new CdException(new InvalidDirectoryException("folder2", ERR_FILE_NOT_FOUND).getMessage()).getMessage(),
                    new MvException(new InvalidDirectoryException("./../file1.txt", ERR_FILE_NOT_FOUND).getMessage()).getMessage()
            ) + STRING_NEWLINE, stderr.toString());
        });
    }

    @Test
    @DisplayName("cd folder1; rm file1.txt")
    public void evaluate_CdThenRmSuccess_CommandsExecuted() {
        assertDoesNotThrow(() -> {
            // Create a file under folder1
            Path filePath = folder1.resolve(FILE_1);
            Files.createFile(filePath);

            CallCommand command1 = new CallCommand(List.of("cd", FOLDER_1), appRunner);
            CallCommand command2 = new CallCommand(List.of("rm", FILE_1), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            // folder1/file1.txt is removed
            assertTrue(Files.notExists(filePath));
            // file1.txt at current dir still remains
            assertTrue(Files.exists(file1));

            // Check current directory
            assertEquals(folder1.toString(), EnvironmentUtil.currentDirectory);
        });
    }

    @Test
    @DisplayName("cd folder1; rm file1.txt (failure)")
    public void evaluate_CdThenRmFails_ErrorOutput() {
        assertDoesNotThrow(() -> {
            CallCommand command1 = new CallCommand(List.of("cd", FOLDER_1), appRunner);
            CallCommand command2 = new CallCommand(List.of("rm", FILE_1), appRunner);

            buildCommand(List.of(command1, command2));

            captureErr();
            command.evaluate(stdin, stdout);

            // file1.txt at current dir still remains
            assertTrue(Files.exists(file1));

            assertEquals(new RmException(new InvalidDirectoryException(FILE_1, ERR_FILE_NOT_FOUND).getMessage()).getMessage() + STRING_NEWLINE, getErrOutput());

            // Check current directory
            assertEquals(folder1.toString(), EnvironmentUtil.currentDirectory);
        });
    }

    @Test
    @DisplayName("cd folder1; cp ../file1.txt file1.txt")
    public void evaluate_CdThenCp_CommandsExecuted() {
        assertDoesNotThrow(() -> {
            CallCommand command1 = new CallCommand(List.of("cd", FOLDER_1), appRunner);
            CallCommand command2 = new CallCommand(List.of("cp", "../file1.txt", "file1.txt"), appRunner);

            buildCommand(List.of(command1, command2));

            command.evaluate(stdin, stdout);

            // file1.txt is copied to folder1/
            assertTrue(Files.exists(file1));
            assertTrue(Files.exists(folder1.resolve(FILE_1)));

            // Check current directory
            assertEquals(folder1.toString(), EnvironmentUtil.currentDirectory);
        });
    }
}
