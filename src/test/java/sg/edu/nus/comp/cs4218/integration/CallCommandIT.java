package sg.edu.nus.comp.cs4218.integration;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.*;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.cmd.*;
import sg.edu.nus.comp.cs4218.impl.util.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.*;

class CallCommandIT {

    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + RESOURCES_PATH + STRING_FILE_SEP + "CallCommandIT";
    private static final Path TEST_PATH = Path.of(TEST_DIR);

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";

    private final Path file1 = Path.of(TEST_DIR, FILE_1);
    private final Path file2 = Path.of(TEST_DIR, FILE_2);

    private final List<Path> paths = List.of(file1, file2);

    private final InputStream stdin = System.in;
    private final OutputStream stdout = new ByteArrayOutputStream();
    private CallCommand command;

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

    private void createFileWithContent(Path path, String content) throws IOException {
        Files.createFile(path);
        BufferedWriter outputStream = new BufferedWriter(new FileWriter(path.toFile(), true));//NOPMD
        outputStream.append(content);
        outputStream.close();
    }

    @BeforeEach
    void setUp() throws IOException {
        createFileWithContent(file1, FILE_1);
        createFileWithContent(file2, FILE_2);
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

    private void buildCommand(List<String> argList) throws ShellException {
        ApplicationRunner appRunner = new ApplicationRunner();
        command = new CallCommand(argList, appRunner);
    }

    @Test
    @DisplayName("echo abc > file1.txt")
    public void evaluate_EchoCommandWithIORedirect_CommandExecuted() {
        assertDoesNotThrow(() -> {
            buildCommand(List.of("echo", "abc", ">", FILE_1));

            command.evaluate(stdin, stdout);

            List<String> output = Files.readAllLines(file1);
            assertEquals(1, output.size());
            assertEquals("abc", output.get(0));
        });
    }

    @Test
    @DisplayName("ls -X .")
    public void evaluate_LsCommandCwd_CommandExecuted() {
        assertDoesNotThrow(() -> {
            buildCommand(List.of("ls", "-X", "."));

            command.evaluate(stdin, stdout);

            String output = stdout.toString();
            assertEquals(FILE_1 + STRING_NEWLINE + FILE_2 + STRING_NEWLINE, output);
        });
    }

    @Test
    @DisplayName("wc `echo file1.txt`")
    public void evaluate_WcCommandWithCommandSub_CommandExecuted() {
        assertDoesNotThrow(() -> {
            buildCommand(List.of("wc", "`echo file1.txt`"));

            command.evaluate(stdin, stdout);

            String output = stdout.toString();
            assertEquals(String.join(String.valueOf(CHAR_TAB),
                    "1", "1", "9", FILE_1) + STRING_NEWLINE, output);
        });
    }

    @Test
    @DisplayName("cat *.txt")
    public void evaluate_CatCommandWithGlobbing_CommandExecuted() {
        assertDoesNotThrow(() -> {
            buildCommand(List.of("cat", "*.txt"));

            command.evaluate(stdin, stdout);

            String output = stdout.toString();
            assertEquals(FILE_1 + STRING_NEWLINE + FILE_2 + STRING_NEWLINE, output);
        });
    }

    @Test
    @DisplayName("uniq -c file1.txt")
    public void evaluate_UniqCommand_CommandExecuted() {
        assertDoesNotThrow(() -> {
            String expected1 = "Hello World";
            String expected2 = "Alice";
            Files.writeString(file1, String.join(STRING_NEWLINE, expected1, expected1,
                    expected2, expected2));

            buildCommand(List.of("uniq", "-c", FILE_1));

            command.evaluate(stdin, stdout);

            String output = stdout.toString();
            assertEquals("2 " + expected1 + STRING_NEWLINE + "2 " + expected2 + STRING_NEWLINE, output);
        });
    }

    @Test
    public void evaluate_InvalidApp_ThrowsShellException() {
        assertThrows(ShellException.class, () -> {
            // Invalid command
            buildCommand(List.of("lsa"));

            command.evaluate(stdin, stdout);
        });
    }

    @Test
    public void evaluate_InvalidAppOptions_ThrowsAppException() {
        assertThrows(WcException.class, () -> {
            // Invalid flags
            buildCommand(List.of("wc", "-X", FILE_1));

            command.evaluate(stdin, stdout);
        });
    }
}