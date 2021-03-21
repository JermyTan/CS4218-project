package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_REDIR_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_REDIR_OUTPUT;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;

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

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.ShellException;

class IORedirectionHandlerTest {

    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + RESOURCES_PATH + STRING_FILE_SEP + "IORedirectionHandlerTest";
    private static final String FILE_1 = "file1.txt"; // exists
    private static final String FILE_2 = "file2.txt"; // exists
    private static final String FILE_3 = "file3.txt"; // does not exist
    private static final Path PATH_1 = Path.of(TEST_DIR, FILE_2);
    private static final Path PATH_2 = Path.of(TEST_DIR, FILE_2);
    private static final Path PATH_3 = Path.of(TEST_DIR, FILE_3);

    private IORedirectionHandler redirHandler;
    private InputStream stdin;
    private OutputStream stdout;

    @BeforeAll
    static void setupBeforeAll() throws Exception {
        EnvironmentUtil.currentDirectory = TEST_DIR;

        if (Files.notExists(PATH_1)) {
            Files.createFile(PATH_1);
        }
        if (Files.notExists(PATH_2)) {
            Files.createFile(PATH_2);
        }
    }

    @AfterAll
    static void tearDownAfterAll() {
        EnvironmentUtil.currentDirectory = ORIGINAL_DIR;
    }

    private void buildRedirHandler(List<String> argsList) throws Exception {
        redirHandler = new IORedirectionHandler(argsList, stdin, stdout);
    }

    @BeforeEach
    void setup() {
        stdin = mock(InputStream.class);
        stdout = mock(OutputStream.class);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(PATH_3);
    }

    @Test
    public void constructor_NullArgList_ThrowsException() {
        assertThrows(ShellException.class, () -> buildRedirHandler(null));
    }

    @Test
    public void constructor_EmptyArgList_ThrowsException() {
        assertThrows(ShellException.class, () -> buildRedirHandler(List.of()));
    }

    @Test
    public void constructor_NullInputStream_ThrowsException() {
        assertThrows(ShellException.class, () -> new IORedirectionHandler(List.of(FILE_1, STRING_REDIR_OUTPUT, FILE_2), null, stdout));
    }

    @Test
    public void constructor_NullOutputStream_ThrowsException() {
        assertThrows(ShellException.class, () -> new IORedirectionHandler(List.of(FILE_1, STRING_REDIR_OUTPUT, FILE_2), stdin, null));
    }

    @Test
    public void extractRedirOptions_NoFileSpecified_ThrowsException() throws Exception {
        buildRedirHandler(List.of(STRING_REDIR_INPUT));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_ConsecutiveRedirOptions_ThrowsException() throws Exception {
        buildRedirHandler(List.of(STRING_REDIR_INPUT, STRING_REDIR_INPUT));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_TwoInputRedir_ThrowsException() throws Exception {
        buildRedirHandler(List.of(STRING_REDIR_INPUT, FILE_1, STRING_REDIR_INPUT, FILE_2));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_TwoOutputRedir_ThrowsException() throws Exception {
        buildRedirHandler(List.of(STRING_REDIR_OUTPUT, FILE_1, STRING_REDIR_OUTPUT, FILE_2));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_InputRedirFileDoesNotExist_ThrowsException() throws Exception {
        buildRedirHandler(List.of(STRING_REDIR_INPUT, FILE_3));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_OutputRedirMultipleFiles_ThrowsException() throws Exception {
        buildRedirHandler(List.of(STRING_REDIR_OUTPUT, FILE_1, FILE_2));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_ValidArgList_RemovesRedirOptions() throws Exception {
        buildRedirHandler(List.of("paste", STRING_REDIR_INPUT, FILE_1, STRING_REDIR_OUTPUT, FILE_2));

        assertDoesNotThrow(() -> redirHandler.extractRedirOptions());

        List<String> noRedirArgsList = redirHandler.getNoRedirArgsList();
        assertEquals(1, noRedirArgsList.size());
        assertEquals("paste", noRedirArgsList.get(0));
    }

    @Test
    public void extractRedirOptions_OutputFileDoesNotExist_OutputFileCreated() throws Exception {
        assertTrue(Files.notExists(PATH_3));

        buildRedirHandler(List.of("paste", STRING_REDIR_INPUT, FILE_1, STRING_REDIR_OUTPUT, FILE_3));

        assertDoesNotThrow(() -> redirHandler.extractRedirOptions());

        assertTrue(Files.exists(PATH_3));
    }
}