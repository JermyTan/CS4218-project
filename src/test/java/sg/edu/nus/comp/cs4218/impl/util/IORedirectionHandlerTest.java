package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_REDIR_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_REDIR_OUTPUT;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
    private final String STRING_REDIR_INPUT = String.valueOf(CHAR_REDIR_INPUT);
    private final String STRING_REDIR_OUTPUT = String.valueOf(CHAR_REDIR_OUTPUT);
    private final Path file3 = Path.of(TEST_DIR, FILE_3);

    private IORedirectionHandler redirHandler;
    private InputStream stdin;
    private OutputStream stdout;

    @BeforeAll
    static void setupBeforeAll() {
        EnvironmentUtil.currentDirectory = TEST_DIR;
    }

    @AfterAll
    static void tearDownAfterAll() {
        EnvironmentUtil.currentDirectory = ORIGINAL_DIR;
    }

    private void buildRedirHandler(List<String> argsList) {
        redirHandler = new IORedirectionHandler(argsList, stdin, stdout);
    }

    @BeforeEach
    void setup() {
        stdin = mock(InputStream.class);
        stdout = mock(OutputStream.class);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(file3);
    }

    @Test
    public void extractRedirOptions_NullArgList_ThrowsException() {
        buildRedirHandler(null);
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_EmptyArgList_ThrowsException() {
        buildRedirHandler(new ArrayList<>());
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_NoFileSpecified_ThrowsException() {
        buildRedirHandler(List.of(STRING_REDIR_INPUT));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_ConsecutiveRedirOptions_ThrowsException() {
        buildRedirHandler(List.of(STRING_REDIR_INPUT, STRING_REDIR_INPUT));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_TwoInputRedir_ThrowsException() {
        buildRedirHandler(List.of(STRING_REDIR_INPUT, FILE_1, STRING_REDIR_INPUT, FILE_2));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_TwoOutputRedir_ThrowsException() {
        buildRedirHandler(List.of(STRING_REDIR_OUTPUT, FILE_1, STRING_REDIR_OUTPUT, FILE_2));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_InputRedirFileDoesNotExist_ThrowsException() {
        buildRedirHandler(List.of(STRING_REDIR_INPUT, FILE_3));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_OutputRedirMultipleFiles_ThrowsException() {
        buildRedirHandler(List.of(STRING_REDIR_OUTPUT, FILE_1, FILE_2));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_ValidArgList_RemovesRedirOptions() {
        buildRedirHandler(List.of("paste", STRING_REDIR_INPUT, FILE_1, STRING_REDIR_OUTPUT, FILE_2));

        assertDoesNotThrow(() -> redirHandler.extractRedirOptions());

        List<String> noRedirArgsList = redirHandler.getNoRedirArgsList();
        assertEquals(1, noRedirArgsList.size());
        assertEquals("paste", noRedirArgsList.get(0));
    }

    @Test
    public void extractRedirOptions_OutputFileDoesNotExist_OutputFileCreated() {
        assertTrue(Files.notExists(file3));

        buildRedirHandler(List.of("paste", STRING_REDIR_INPUT, FILE_1, STRING_REDIR_OUTPUT, FILE_3));

        assertDoesNotThrow(() -> redirHandler.extractRedirOptions());

        assertTrue(Files.exists(file3));
    }
}