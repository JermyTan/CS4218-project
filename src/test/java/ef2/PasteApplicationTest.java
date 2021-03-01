package ef2;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.impl.app.PasteApplication;

@Disabled
class PasteApplicationTest {

    private static final String ORIGINAL_DIR = Environment.currentDirectory;
    private static final String TEST_DIR = Environment.currentDirectory + File.separator + RESOURCES_PATH + File.separator + "PasteApplicationTest";

    private static final String FILE_1 = "file1.txt"; // exists
    private static final String FILE_2 = "file2.txt"; // exists
    private static final String FILE_3 = "file3.txt"; // exists
    private static final String FILE_4 = "file4.txt"; // does not exist

    private static final String FOLDER_1 = "folder1"; // exists

    private static final String STD_INPUT = "Hello world" + STRING_NEWLINE + "CS4218" + STRING_NEWLINE;

    private final OutputStream stdout = new ByteArrayOutputStream();
    private PasteApplication app;
    private InputStream stdin = System.in;

    @BeforeAll
    static void setupBeforeAll() {
        Environment.currentDirectory = TEST_DIR;
    }

    @AfterAll
    static void tearDownAfterAll() {
        Environment.currentDirectory = ORIGINAL_DIR;
    }

    private void provideInput(String input) {
        stdin = new ByteArrayInputStream(input.getBytes());
    }

    @BeforeEach
    void setup() {
        app = new PasteApplication();
    }

    @Test
    public void run_NullOutputStream_ThrowsException() {
        assertThrows(Exception.class, () -> app.run(new String[]{FILE_1, FILE_2}, stdin, null));
    }

    @Test
    public void run_NullArgList_ThrowsException() {
        assertThrows(Exception.class, () -> app.run(null, stdin, stdout));
    }

    @Test
    public void run_ArgListContainsNull_ThrowsException() {
        assertThrows(Exception.class, () -> app.run(new String[]{null}, stdin, stdout));
    }

    @Test
    public void run_OneFile_OutputFileContent() {
        String expected = "A" + STRING_NEWLINE + "B" + STRING_NEWLINE + "C" + STRING_NEWLINE
                + "D" + STRING_NEWLINE;

        assertDoesNotThrow(() -> app.run(new String[]{FILE_1}, stdin, stdout));
        assertEquals(expected, stdout.toString());
    }

    @Test
    public void run_FileDoesNotExist_ThrowsException() {
        assertThrows(Exception.class, () -> app.run(new String[]{FILE_4}, null, stdout));
    }

    @Test
    public void run_TwoFilesEqualNumOfLines_FilesMerged() {
        String expected = "A\t1" + STRING_NEWLINE + "B\t2" + STRING_NEWLINE + "C\t3" + STRING_NEWLINE
                + "D\t4" + STRING_NEWLINE;

        assertDoesNotThrow(() -> app.run(new String[]{FILE_1, FILE_2}, stdin, stdout));
        assertEquals(expected, stdout.toString());
    }

    @Test
    public void run_TwoFilesUnequalNumOfLines_FilesMerged() {
        String expected = "A\tE" + STRING_NEWLINE + "B\tF" + STRING_NEWLINE + "C\tG" + STRING_NEWLINE
                + "D" + STRING_NEWLINE;

        assertDoesNotThrow(() -> app.run(new String[]{FILE_1, FILE_3}, stdin, stdout));
        assertEquals(expected, stdout.toString());
    }

    @Test
    public void run_EmptyArgList_ReadFromStdin() {
        String expected = STD_INPUT;
        provideInput(expected);

        assertDoesNotThrow(() -> app.run(new String[]{}, stdin, stdout));

        assertEquals(expected, stdout.toString());
    }

    @Test
    public void run_OnlyStdinFlagPresent_ReadFromStdin() {
        String expected = STD_INPUT;
        provideInput(expected);

        assertDoesNotThrow(() -> app.run(new String[]{"-"}, stdin, stdout));

        assertEquals(expected, stdout.toString());
    }

    @Test
    public void run_BothFileAndStdinFlagPresent_ReturnsMergedContent() {
        String stdinInput = STD_INPUT;
        provideInput(stdinInput);

        assertDoesNotThrow(() -> app.run(new String[]{FILE_1, "-"}, stdin, stdout));

        String expected = "A\tHello word" + STRING_NEWLINE + "B\tCS4218" + STRING_NEWLINE
                + "C" + STRING_NEWLINE + "D" + STRING_NEWLINE;
        assertEquals(expected, stdout.toString());
    }

    @Test
    public void run_SerialFlagOneFile_SerialOutput() {
        assertDoesNotThrow(() -> app.run(new String[]{"-s", FILE_1}, stdin, stdout));

        String expected = "A\tB\tC\tD" + STRING_NEWLINE;
        assertEquals(expected, stdout.toString());
    }

    @Test
    public void run_SerialFlagTwoFiles_SerialOutput() {
        assertDoesNotThrow(() -> app.run(new String[]{"-s", FILE_1}, stdin, stdout));

        String expected = "A\tB\tC\tD" + STRING_NEWLINE + "1\t2\t3\t4" + STRING_NEWLINE;
        assertEquals(expected, stdout.toString());
    }

    @Test
    public void mergeStdin_NullArgs_ThrowsException() {
        assertThrows(Exception.class, () -> app.mergeStdin(null, stdin));
        assertThrows(Exception.class, () -> app.mergeStdin(false, null));
    }

    @Test
    public void mergeStdin_ReadFromStdin_ReturnStdinContent() {
        String expected = STD_INPUT;
        provideInput(expected);

        assertDoesNotThrow(() -> {
            String output = app.mergeStdin(false, stdin);
            assertEquals(expected, output);
        });
    }

    @Test
    public void mergeFile_NullFilenames_ThrowsException() {
        assertThrows(Exception.class, () -> app.mergeFile(false, (String[]) null));
    }

    @Test
    public void mergeFile_EmptyFilenames_ThrowsException() {
        assertThrows(Exception.class, () -> app.mergeFile(false));
    }

    @Test
    public void mergeFile_FilenamesContainNull_ThrowsException() {
        assertThrows(Exception.class, () -> app.mergeFile(false, FILE_1, null));
    }

    @Test
    public void mergeFile_NonSerial_ReturnsMergedFileContent() {
        String expected = "1\tE" + STRING_NEWLINE + "2\tF" + STRING_NEWLINE + "3\tG" + STRING_NEWLINE + "4";

        assertDoesNotThrow(() -> {
            String output = app.mergeFile(false, FILE_2, FILE_3);
            assertEquals(expected, output);
        });
    }

    @Test
    public void mergeFile_IsSerial_ReturnsMergedFileContent() {
        String expected = "1\t2\t3\t4" + STRING_NEWLINE + "E\tF\tG";

        assertDoesNotThrow(() -> {
            String output = app.mergeFile(true, FILE_2, FILE_3);
            assertEquals(expected, output);
        });
    }

    @Test
    public void mergeFile_FileDoesNotExist_ThrowsException() {
        assertThrows(Exception.class, () -> app.mergeFile(false, FILE_4));
    }

    @Test
    public void mergeFile_DirectorySupplied_ThrowsException() {
        assertThrows(Exception.class, () -> app.mergeFile(false, FOLDER_1));
    }

    @Test
    public void mergeFileAndStdin_NullStdin_ThrowsException() {
        assertThrows(Exception.class, () -> app.mergeFileAndStdin(false, null, FILE_1));
    }

    @Test
    public void mergeFileAndStdin_NullFilenames_ThrowsException() {
        assertThrows(Exception.class, () -> app.mergeFileAndStdin(false, stdin, (String[]) null));
    }

    @Test
    public void mergeFileAndStdin_EmptyFilenames_ThrowsException() {
        assertThrows(Exception.class, () -> app.mergeFileAndStdin(false, stdin));
    }

    @Test
    public void mergeFileAndStdin_FilenamesContainNull_ThrowsException() {
        assertThrows(Exception.class, () -> app.mergeFileAndStdin(false, stdin, FILE_1, null));
    }

    @Test
    public void mergeFileAndStdin_ReadFromBothFileAndStdin_ReturnsMergedContent() {
        String stdinInput = STD_INPUT;
        provideInput(stdinInput);

        String expected = "A\tHello word" + STRING_NEWLINE + "B\tCS4218" + STRING_NEWLINE
                + "C" + STRING_NEWLINE + "D" + STRING_NEWLINE;

        assertDoesNotThrow(() -> {
            String output = app.mergeFileAndStdin(false, stdin, "-", FILE_1);
            assertEquals(expected, output);
        });
    }
}