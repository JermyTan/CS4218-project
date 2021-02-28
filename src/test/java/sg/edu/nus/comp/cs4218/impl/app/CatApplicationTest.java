package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.CatException;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;

class CatApplicationTest {

    private static final String ORIGINAL_DIR = Environment.currentDirectory;
    private static final String TESTDIR = Environment.currentDirectory + File.separator + RESOURCES_PATH + File.separator + "CatApplicationTest";

    private static final String FILE_1 = "file1.txt"; // exists
    private static final String FILE_2 = "file2.txt"; // exists
    private static final String FILE_3 = "file3.txt"; // does not exist
    private static final String EMPTY_FILE = "emptyFile.txt"; // empty file

    private static final String FOLDER_1 = "folder1"; // exists

    private final OutputStream stdout = new ByteArrayOutputStream();
    private CatApplication app;
    private InputStream stdin = System.in;
    private OutputStream stderr;

    @BeforeAll
    static void setupBeforeAll() {
        Environment.currentDirectory = TESTDIR;
    }

    @AfterAll
    static void tearDownAfterAll() {
        Environment.currentDirectory = ORIGINAL_DIR;
    }

    private void captureErr() {
        stderr = new ByteArrayOutputStream();
        System.setErr(new PrintStream(stderr));
    }

    private String getErrOutput() {
        System.setErr(System.err);
        return stderr.toString();
    }

    private void provideInput(String input) {
        stdin = new ByteArrayInputStream(input.getBytes());
    }

    @BeforeEach
    void setup() {
        app = new CatApplication();
    }

    @Test
    public void run_OneFile_FileDisplayed() {
        String expected = "line1" + STRING_NEWLINE + "line2" + STRING_NEWLINE + "line3" + STRING_NEWLINE;

        assertDoesNotThrow(() -> app.run(new String[]{FILE_1}, stdin, stdout));
        assertEquals(expected, stdout.toString());
    }

    @Test
    public void run_MultipleFiles_FilesDisplayed() {
        String expected = "line1" + STRING_NEWLINE + "line2" + STRING_NEWLINE + "line3" + STRING_NEWLINE
                + "Hello World" + STRING_NEWLINE + "CS4218" + STRING_NEWLINE;

        assertDoesNotThrow(() -> app.run(new String[]{FILE_1, FILE_2}, stdin, stdout));
        assertEquals(expected, stdout.toString());
    }

    @Test
    public void run_ReadFromStdin_ReturnStdinContent() {
        String expected = "Hello world" + STRING_NEWLINE + "CS4218" + STRING_NEWLINE;
        provideInput(expected);

        assertDoesNotThrow(() -> app.run(new String[]{}, stdin, stdout));

        assertEquals(expected, stdout.toString());
    }

    @Test
    public void run_nFlag_LineNumberDisplayed() {
        String expected = "1 line1" + STRING_NEWLINE + "2 line2" + STRING_NEWLINE + "3 line3" + STRING_NEWLINE;

        assertDoesNotThrow(() -> app.run(new String[]{"-n", FILE_1}, stdin, stdout));
        assertEquals(expected, stdout.toString());
    }

    @Test
    public void run_StdinFlag_ReadFromStdin() {
        String expected = "Hello world" + STRING_NEWLINE + "CS4218" + STRING_NEWLINE;
        provideInput(expected);

        assertDoesNotThrow(() -> app.run(new String[]{"-"}, stdin, stdout));

        assertEquals(expected, stdout.toString());
    }

    @Test
    public void run_ReadFromBothFileAndStdin_ReturnsConcatenatedContent() {
        String fileContent = "line1" + STRING_NEWLINE + "line2" + STRING_NEWLINE + "line3" + STRING_NEWLINE;
        String stdinInput = "Hello world" + STRING_NEWLINE + "CS4218" + STRING_NEWLINE;
        provideInput(stdinInput);

        assertDoesNotThrow(() -> app.run(new String[]{FILE_1, "-"}, stdin, stdout));

        assertEquals(fileContent + stdinInput, stdout.toString());
    }

    @Test
    public void run_EmptyFile_NoOutputWritten() {
        assertDoesNotThrow(() -> app.run(new String[]{EMPTY_FILE}, stdin, stdout));
        assertEquals("", stdout.toString());
    }

    @Test
    public void run_NullOutputStream_ThrowsException() {
        assertThrows(CatException.class, () -> app.run(new String[]{FILE_1}, stdin, null));
    }

    @Test
    public void run_NullArgs_ThrowsException() {
        assertThrows(CatException.class, () -> app.run(new String[]{null}, stdin, stdout));
    }

    @Test
    public void run_NoInput_ThrowsException() {
        assertThrows(CatException.class, () -> app.run(new String[]{}, null, stdout));
    }

    @Test
    public void catFiles_NullFilenames_ThrowsException() {
        assertThrows(CatException.class, () -> app.catFiles(false, (String[]) null));
    }

    @Test
    public void catFiles_EmptyFilenames_ThrowsException() {
        assertThrows(CatException.class, () -> app.catFiles(false));
    }

    @Test
    public void catFiles_FilenamesContainNull_ThrowsException() {
        assertThrows(CatException.class, () -> app.catFiles(false, FILE_1, null));
    }

    @Test
    public void catFiles_ValidArgs_ReturnsFileContent() {
        String expected = "line1" + STRING_NEWLINE + "line2" + STRING_NEWLINE + "line3";

        assertDoesNotThrow(() -> {
            String output = app.catFiles(false, FILE_1);
            assertEquals(expected, output);
        });
    }

    @Test
    public void catFiles_FileDoesNotExist_WritesErrToStderr() {
        captureErr();

        assertDoesNotThrow(() -> {
            app.catFiles(false, FILE_3);

            assertEquals(new CatException(
                            String.format(STRING_LABEL_VALUE_PAIR, FILE_3, ERR_FILE_NOT_FOUND)
                    ).getMessage() + STRING_NEWLINE,
                    getErrOutput());
        });

        System.setErr(System.err);
    }

    @Test
    public void catFiles_DirectorySupplied_WritesErrToStderr() {
        captureErr();

        assertDoesNotThrow(() -> {
            app.catFiles(false, FOLDER_1);

            assertEquals(new CatException(
                            String.format(STRING_LABEL_VALUE_PAIR, FOLDER_1, ERR_IS_DIR)
                    ).getMessage() + STRING_NEWLINE,
                    getErrOutput());
        });
    }

    @Test
    public void catStdin_NullStdin_ThrowsException() {
        assertThrows(CatException.class, () -> app.catStdin(false, null));
    }

    @Test
    public void catStdin_ReadFromStdin_ReturnStdinContent() {
        String expected = "Hello world" + STRING_NEWLINE + "CS4218";
        provideInput(expected);

        assertDoesNotThrow(() -> {
            String output = app.catStdin(false, stdin);
            assertEquals(expected, output);
        });
    }

    @Test
    public void catFileAndStdin_NullStdin_ThrowsException() {
        assertThrows(CatException.class, () -> app.catFileAndStdin(false, null, FILE_1));
    }

    @Test
    public void catFileAndStdin_NullFilenames_ThrowsException() {
        assertThrows(CatException.class, () -> app.catFileAndStdin(false, stdin, (String[]) null));
    }

    @Test
    public void catFileAndStdin_EmptyFilenames_ThrowsException() {
        assertThrows(CatException.class, () -> app.catFileAndStdin(false, stdin));
    }

    @Test
    public void catFileAndStdin_FilenamesContainNull_ThrowsException() {
        assertThrows(CatException.class, () -> app.catFileAndStdin(false, stdin, FILE_1, null));
    }

    @Test
    public void catFileAndStdin_ReadFromBothFileAndStdin_ReturnsConcatenatedContent() {
        String fileContent = "line1" + STRING_NEWLINE + "line2" + STRING_NEWLINE + "line3";
        String stdinInput = "Hello world" + STRING_NEWLINE + "CS4218" + STRING_NEWLINE;
        provideInput(stdinInput);

        assertDoesNotThrow(() -> {
            String output = app.catFileAndStdin(false, stdin, "-", FILE_1);
            assertEquals(stdinInput + fileContent, output);
        });
    }
}