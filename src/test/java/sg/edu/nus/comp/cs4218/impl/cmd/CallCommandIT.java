package sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;

class CallCommandIT {

    private static final String ORIGINAL_DIR = Environment.currentDirectory;
    private static final String TEST_DIR = Environment.currentDirectory + File.separator + RESOURCES_PATH + File.separator + "CallCommandIT";

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";

    private final Path file1 = Paths.get(TEST_DIR, FILE_1);
    private final Path file2 = Paths.get(TEST_DIR, FILE_2);

    private final List<Path> paths = List.of(file1, file2);

    private final InputStream stdin = System.in;
    private final OutputStream stdout = new ByteArrayOutputStream();
    private CallCommand command;

    @BeforeAll
    static void setupBeforeAll() {
        Environment.currentDirectory = TEST_DIR;
    }

    @AfterAll
    static void tearDownAfterAll() {
        Environment.currentDirectory = ORIGINAL_DIR;
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
        ArgumentResolver argumentResolver = new ArgumentResolver();
        command = new CallCommand(argList, appRunner, argumentResolver);
    }

    @Test
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
    public void evaluate_CatCommandWithGlobbing_CommandExecuted() {
        assertDoesNotThrow(() -> {
            buildCommand(List.of("cat", "*.txt"));

            command.evaluate(stdin, stdout);

            String output = stdout.toString();
            assertEquals(FILE_1 + STRING_NEWLINE + FILE_2 + STRING_NEWLINE, output);
        });
    }

    @Test
    public void evaluate_LsCommandCwd_CommandExecuted() {
        assertDoesNotThrow(() -> {
            buildCommand(List.of("ls", "-X", "."));

            command.evaluate(stdin, stdout);

            String output = stdout.toString();
            assertEquals(FILE_1 + STRING_NEWLINE + FILE_2 + STRING_NEWLINE, output);
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