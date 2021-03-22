package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.*;
import sg.edu.nus.comp.cs4218.exception.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.IOUtils.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.*;

class TeeApplicationTest {

    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + RESOURCES_PATH + STRING_FILE_SEP + "TeeApplicationTest";
    private static final Path TEST_PATH = Path.of(TEST_DIR);

    private static final String INPUT_1 = "hello world";
    private static final String INPUT_2 = "hello world" + STRING_NEWLINE + "How are you";

    private static final String OUTPUT_FILE_1 = "output1.txt";
    private static final String OUTPUT_FILE_2 = "output2.txt";
    private static final String OUTPUT_FILE_3 = "output3.txt";

    private static final String[] ARGS_1 = {OUTPUT_FILE_1, OUTPUT_FILE_2};
    private static final String[] ARGS_2 = {"-a", OUTPUT_FILE_1};

    private static final String FOLDER = "folder";

    private static final String FILE_CONTENT_1 = "This is old content.";
    private static final String FILE_CONTENT_2 = STRING_EMPTY;

    private final ByteArrayOutputStream ERR_OUTPUT = new ByteArrayOutputStream();
    private final ByteArrayOutputStream STD_OUTPUT = new ByteArrayOutputStream();

    private final Path file1 = Path.of(TEST_DIR, OUTPUT_FILE_1); // exists
    private final Path file2 = Path.of(TEST_DIR, OUTPUT_FILE_2); // exists
    private final Path file3 = Path.of(TEST_DIR, OUTPUT_FILE_3); // does not exist
    private final Path folder = Path.of(TEST_DIR, FOLDER); // exists
    private final List<Path> paths = List.of(file1, file2, file3, folder);

    private InputStream inputStream;
    private TeeApplication app;

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
        BufferedWriter outputStream = new BufferedWriter(new FileWriter(path.toFile(), true)); //NOPMD
        outputStream.append(content);
        outputStream.close();
    }

    private String readFromFile(Path path) {
        try {
            List<String> result = getLinesFromInputStream(Files.newInputStream(path));
            return String.join(STRING_NEWLINE, result);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        app = new TeeApplication();

        createFileWithContent(file1, FILE_CONTENT_1);
        createFileWithContent(file2, FILE_CONTENT_2);

        Files.createDirectory(folder);

        System.setOut(new PrintStream(STD_OUTPUT));
        System.setErr(new PrintStream(ERR_OUTPUT));
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

        System.setErr(System.err);
        System.setErr(System.out);
    }

    @Test
    public void teeFromStdin_NullStdin_ThrowsException() {
        assertThrows(TeeException.class, () -> app.teeFromStdin(false, null, OUTPUT_FILE_1));
    }

    @Test
    public void teeFromStdin_NullFileName_Success() {
        assertThrows(TeeException.class, () -> app.teeFromStdin(null, inputStream, (String[]) null));
    }

    @Test
    public void teeFromStdin_FileNamesContainNull_ThrowsException() {
        assertThrows(TeeException.class, () -> app.teeFromStdin(false, inputStream, OUTPUT_FILE_1, null));
    }

    @Test
    public void teeFromStdin_FileNamesContainEmptyStr_ThrowsException() {
        assertThrows(TeeException.class, () -> app.teeFromStdin(false, inputStream, OUTPUT_FILE_1, STRING_EMPTY));
    }

    @Test
    public void teeFromStdin_NullFlag_ThrowsException() {
        assertThrows(TeeException.class, () -> app.teeFromStdin(null, inputStream, OUTPUT_FILE_1));
    }

    @Test
    public void teeFromStdin_WriteSingleLineToBothStdoutAndFileAppend_ReturnsString() {
        inputStream = new ByteArrayInputStream(INPUT_1.getBytes());
        String[] filenames = {OUTPUT_FILE_1, OUTPUT_FILE_2};

        assertDoesNotThrow(() -> {
            String output = app.teeFromStdin(true, inputStream, filenames);

            String fromFile1 = readFromFile(file1);
            String fromFile2 = readFromFile(file2);

            assertEquals(INPUT_1, output);
            assertEquals(FILE_CONTENT_1 + INPUT_1, fromFile1);
            assertEquals(INPUT_1, fromFile2);
        });
    }

    @Test
    public void teeFromStdin_WriteMultiLinesToBothStdoutAndFileAppend_ReturnsString() {
        inputStream = new ByteArrayInputStream(INPUT_2.getBytes());
        String[] filenames = {OUTPUT_FILE_1, OUTPUT_FILE_2};

        assertDoesNotThrow(() -> {
            String output = app.teeFromStdin(true, inputStream, filenames);
            String fromFile1 = readFromFile(file1);
            String fromFile2 = readFromFile(file2);

            assertEquals(INPUT_2, output);
            assertEquals(FILE_CONTENT_1 + INPUT_2, fromFile1);
            assertEquals(INPUT_2, fromFile2);
        });
    }

    @Test
    public void teeFromStdin_WriteToBothStdoutAndFileNoAppend_TruncatesFile() {
        inputStream = new ByteArrayInputStream(INPUT_1.getBytes());
        String[] filenames = {OUTPUT_FILE_1, OUTPUT_FILE_2};

        assertDoesNotThrow(() -> {
            String output = app.teeFromStdin(false, inputStream, filenames);
            String fromFile1 = readFromFile(file1);
            String fromFile2 = readFromFile(file2);

            assertEquals(INPUT_1, output);
            assertEquals(INPUT_1, fromFile1);
            assertEquals(INPUT_1, fromFile2);
        });
    }

    @Test
    public void teeFromStdin_WriteToStdoutOnlyNoAppend_ReturnsString() {
        inputStream = new ByteArrayInputStream(INPUT_1.getBytes());

        assertDoesNotThrow(() -> {
            String output = app.teeFromStdin(false, inputStream);

            assertEquals(INPUT_1, output);
        });
    }

    @Test
    public void teeFromStdin_WriteToStdoutOnlyAppend_ReturnsString() {
        inputStream = new ByteArrayInputStream(INPUT_2.getBytes());

        assertDoesNotThrow(() -> {
            String output = app.teeFromStdin(true, inputStream);

            assertEquals(INPUT_2, output);
        });
    }

    @Test
    public void teeFromStdin_FileIsDirectory_WritesToStderr() {
        inputStream = new ByteArrayInputStream(INPUT_1.getBytes());
        String[] filenames = {FOLDER};

        assertDoesNotThrow(() -> {
            app.teeFromStdin(false, inputStream, filenames);

            assertEquals("tee: " + FOLDER + ": " + ERR_IS_DIR + STRING_NEWLINE, ERR_OUTPUT.toString());
        });
    }

    @Test
    public void teeFromStdin_FileNotFound_CreatesFile() {
        inputStream = new ByteArrayInputStream(INPUT_1.getBytes());
        String[] filenames = {OUTPUT_FILE_3};

        assertDoesNotThrow(() -> {
            String output = app.teeFromStdin(false, inputStream, filenames);
            String fromFile3 = readFromFile(file3);

            assertEquals(INPUT_1, fromFile3);
            assertEquals(INPUT_1, output);
        });
    }

    @Test
    public void teeFromStdin_FilenameContainsNullValues_WritesToStderr() {
        String[] filenames = {OUTPUT_FILE_1, null};
        inputStream = new ByteArrayInputStream(INPUT_1.getBytes());
        Throwable error = assertThrows(TeeException.class, () -> app.teeFromStdin(false, inputStream, filenames));
        assertEquals(String.format(STRING_LABEL_VALUE_PAIR, APP_TEE, ERR_INVALID_FILES), error.getMessage());
    }

    @Test
    public void teeFromStdin_NoInputStream_WritesToStderr() {
        String[] filenames = {OUTPUT_FILE_1};
        Throwable error = assertThrows(TeeException.class, () -> app.teeFromStdin(false, null, filenames));
        assertEquals(String.format(STRING_LABEL_VALUE_PAIR, APP_TEE, ERR_NO_ISTREAM), error.getMessage());
    }

    @Test
    public void run_NoInputStream_ThrowsException() {
        Throwable error = assertThrows(TeeException.class, () -> app.run(ARGS_1, null, STD_OUTPUT));
        assertEquals(String.format(STRING_LABEL_VALUE_PAIR, APP_TEE, ERR_NO_ISTREAM), error.getMessage());
    }

    @Test
    public void run_NoOutputStream_ThrowsException() {
        inputStream = new ByteArrayInputStream(INPUT_1.getBytes());
        Throwable error = assertThrows(TeeException.class, () -> app.run(ARGS_1, inputStream, null));
        assertEquals(String.format(STRING_LABEL_VALUE_PAIR, APP_TEE, ERR_NO_OSTREAM), error.getMessage());
    }

    @Test
    public void run_EmptyStdin_NoOutputWritten() {
        inputStream = new ByteArrayInputStream(new byte[0]);
        assertDoesNotThrow(() -> app.run(new String[0], inputStream, STD_OUTPUT));
        assertEquals(STRING_EMPTY, STD_OUTPUT.toString());
    }

    @Test
    public void run_NullArgs_Success() {
        inputStream = new ByteArrayInputStream(INPUT_1.getBytes());
        assertDoesNotThrow(() -> app.run(null, inputStream, STD_OUTPUT));
    }

    @Test
    public void run_ArgsContainNull_ThrowsException() {
        inputStream = new ByteArrayInputStream(INPUT_1.getBytes());
        Throwable exception = assertThrows(TeeException.class, () -> app.run(new String[]{null}, inputStream, STD_OUTPUT));
        assertEquals(String.format(STRING_LABEL_VALUE_PAIR, APP_TEE, ERR_NULL_ARGS), exception.getMessage());
    }

    @Test
    public void run_ReadFromStdinNoAppend_ReturnsString() {
        inputStream = new ByteArrayInputStream(INPUT_1.getBytes());

        assertDoesNotThrow(() -> {
            app.run(ARGS_1, inputStream, STD_OUTPUT);
            String fromFile1 = readFromFile(file1);
            String fromFile2 = readFromFile(file2);

            assertEquals(INPUT_1 + STRING_NEWLINE, STD_OUTPUT.toString());
            assertEquals(INPUT_1, fromFile1);
            assertEquals(INPUT_1, fromFile2);
        });
    }

    @Test
    public void run_ReadFromStdinAppend_ReturnsString() {
        inputStream = new ByteArrayInputStream(INPUT_2.getBytes());

        assertDoesNotThrow(() -> {
            app.run(ARGS_2, inputStream, STD_OUTPUT);
            String fromFile1 = readFromFile(file1);

            assertEquals(INPUT_2 + STRING_NEWLINE, STD_OUTPUT.toString());
            assertEquals(FILE_CONTENT_1 + INPUT_2, fromFile1);
        });
    }
}