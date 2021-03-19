package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_LS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_FILES;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_NOT_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.LsException;

class LsApplicationTest {

    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + RESOURCES_PATH + STRING_FILE_SEP + "LsApplicationTest";

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";
    private static final String FILE_WO_EXT = "fileX";

    private static final String FOLDER_1 = "folder1";
    private static final String FOLDER_2 = "folder2";
    private static final String NON_EXISTENT_FOLDER = "folder3"; // does not exist
    private static final String EMPTY_FOLDER = "emptyFolder";
    private static final String FOLDER_WITH_HIDDEN_FILE = "folderWithHiddenFile";
    private static final String NESTED_FOLDER = "nestedFolder";

    private static final String[] FILES = new String[]{
            FILE_1, FOLDER_1, FOLDER_2, EMPTY_FOLDER, FOLDER_WITH_HIDDEN_FILE, NESTED_FOLDER
    };
    private final OutputStream stdout = new ByteArrayOutputStream();
    private final InputStream stdin = mock(InputStream.class);
    private LsApplication app;
    private OutputStream stderr;

    @BeforeAll
    static void setupBeforeAll() {
        EnvironmentUtil.currentDirectory = TEST_DIR;
    }

    @AfterAll
    static void tearDownAfterAll() {
        EnvironmentUtil.currentDirectory = ORIGINAL_DIR;
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
    void setup() {
        app = new LsApplication();
    }

    @Test
    public void run_NullOutputStream_ThrowsException() {
        Throwable exception = assertThrows(LsException.class, () -> app.run(new String[]{}, stdin, null));
        assertEquals(String.format(STRING_LABEL_VALUE_PAIR, APP_LS, ERR_NO_OSTREAM), exception.getMessage());
    }

    @Test
    public void run_NullArgs_Success() {
        assertDoesNotThrow(() -> app.run(null, stdin, stdout));
    }

    @Test
    public void run_ArgsContainNull_ThrowsException() {
        Throwable exception = assertThrows(LsException.class, () -> app.run(new String[]{null}, stdin, stdout));
        assertEquals(String.format(STRING_LABEL_VALUE_PAIR, APP_LS, ERR_NULL_ARGS), exception.getMessage());
    }

    @Test
    public void run_OneFolder_FilesListed() {
        Set<String> expected = new HashSet<>();
        expected.add(FILE_1);
        expected.add(FILE_2);

        assertDoesNotThrow(() -> app.run(new String[]{FOLDER_1}, stdin, stdout));

        String[] actual = stdout.toString().split(STRING_NEWLINE);
        assertEquals(expected.size(), actual.length);
        for (String fileName : actual) {
            assertTrue(expected.contains(fileName));
        }
    }

    @Test
    public void run_EmptyFolder_EmptyOutput() {
        assertDoesNotThrow(() -> app.run(new String[]{EMPTY_FOLDER}, stdin, stdout));
        assertEquals(STRING_EMPTY, stdout.toString());
    }

    @Test
    public void run_FolderWithHiddenFile_HiddenFileNotListed() {
        assertDoesNotThrow(() -> app.run(new String[]{FOLDER_WITH_HIDDEN_FILE}, stdin, stdout));
        assertEquals(FILE_1 + STRING_NEWLINE, stdout.toString());
    }

    @Test
    public void run_NoFolderSpecified_FilesAtCwdListed() {
        Set<String> expected = new HashSet<>();
        for (String folder : FILES) {
            expected.add(folder);
        }

        assertDoesNotThrow(() -> app.run(new String[]{}, stdin, stdout));

        String[] actual = stdout.toString().split(STRING_NEWLINE);
        assertEquals(expected.size(), actual.length);
        for (String fileName : actual) {
            assertTrue(expected.contains(fileName));
        }
    }

    @Test
    public void run_MultipleFolders_FilesListed() {
        Map<String, Set<String>> folders = new HashMap<>();
        folders.put(FOLDER_1 + ":", new HashSet<>(List.of(FILE_1, FILE_2)));
        folders.put(FOLDER_2 + ":", new HashSet<>(List.of(FILE_1, FILE_2)));

        assertDoesNotThrow(() -> app.run(new String[]{FOLDER_1, FOLDER_2}, stdin, stdout));

        String[] actual = stdout.toString().split(STRING_NEWLINE + STRING_NEWLINE);
        for (String folderStr : actual) {
            String[] lines = folderStr.split(STRING_NEWLINE);
            assertTrue(lines.length >= 1);

            String folder = lines[0];
            assertTrue(folders.containsKey(folder));

            Set<String> expectedFiles = folders.get(folder);
            assertEquals(expectedFiles.size(), lines.length - 1);
            for (int i = 1; i < lines.length; i++) {
                assertTrue(expectedFiles.contains(lines[i]));
            }
        }
    }

    @Test
    public void run_FoldersOnly_OnlyFoldersListed() {
        Set<String> expected = new HashSet<>();
        expected.add(FOLDER_1);
        expected.add(FOLDER_2);

        assertDoesNotThrow(() -> app.run(new String[]{"-d", NESTED_FOLDER}, stdin, stdout));

        String[] actual = stdout.toString().split(STRING_NEWLINE);
        assertEquals(expected.size(), actual.length);
        for (String fileName : actual) {
            assertTrue(expected.contains(fileName));
        }
    }

    @Test
    public void run_Recursive_FilesAndSubfoldersListed() {
        Map<String, Set<String>> folders = new HashMap<>();
        folders.put(NESTED_FOLDER + ":", new HashSet<>(List.of(FILE_1, FOLDER_1, FOLDER_2)));
        folders.put(NESTED_FOLDER + STRING_FILE_SEP + FOLDER_1 + ":",
                new HashSet<>(List.of(FILE_1, FILE_WO_EXT)));
        folders.put(NESTED_FOLDER + STRING_FILE_SEP + FOLDER_2 + ":",
                new HashSet<>(List.of(FILE_1, FILE_2)));

        assertDoesNotThrow(() -> app.run(new String[]{"-R", NESTED_FOLDER}, stdin, stdout));

        String[] actual = stdout.toString().split(STRING_NEWLINE + STRING_NEWLINE);
        for (String folderStr : actual) {
            String[] lines = folderStr.split(STRING_NEWLINE);
            assertTrue(lines.length >= 1);

            String folder = lines[0];
            System.out.println(folder);
            assertTrue(folders.containsKey(folder));

            Set<String> expectedFiles = folders.get(folder);
            assertEquals(expectedFiles.size(), lines.length - 1);
            for (int i = 1; i < lines.length; i++) {
                assertTrue(expectedFiles.contains(lines[i]));
            }
        }
    }

    @Test
    public void run_SortByExt_FileWithoutExtensionSortedFirst() {
        String expected = FILE_WO_EXT + STRING_NEWLINE + FILE_1 + STRING_NEWLINE;

        assertDoesNotThrow(() -> app.run(new String[]{"-X", NESTED_FOLDER + STRING_FILE_SEP + FOLDER_1},
                stdin, stdout));

        assertEquals(expected, stdout.toString());
    }

    @Test
    public void run_SortByExt_FilesSortedByExtension() {
        String expected = FILE_1 + STRING_NEWLINE + FILE_2 + STRING_NEWLINE;

        assertDoesNotThrow(() -> app.run(new String[]{"-X", NESTED_FOLDER + STRING_FILE_SEP + FOLDER_2},
                stdin, stdout));

        assertEquals(expected, stdout.toString());
    }

    @Test
    public void listFolderContent_FolderNamesContainNull_ThrowsException() {
        Throwable exception = assertThrows(LsException.class, () ->
                app.listFolderContent(false, false, false, FOLDER_1, null)
        );
        assertEquals(String.format(STRING_LABEL_VALUE_PAIR, APP_LS, ERR_INVALID_FILES), exception.getMessage());
    }

    @Test
    public void listFolderContent_FolderDoesNotExist_WritesErrToStderr() {
        captureErr();

        assertDoesNotThrow(() -> {
            app.listFolderContent(false, false, false, NON_EXISTENT_FOLDER);

            assertEquals(new LsException(
                            String.format(STRING_LABEL_VALUE_PAIR, NON_EXISTENT_FOLDER, ERR_FILE_NOT_FOUND)
                    ).getMessage() + STRING_NEWLINE,
                    getErrOutput());
        });
    }

    @Test
    public void listFolderContent_NullFlags_ThrowsException() {
        Throwable exception1 = assertThrows(LsException.class, () ->
                app.listFolderContent(null, false, false, FOLDER_1)
        );
        assertEquals(String.format(STRING_LABEL_VALUE_PAIR, APP_LS, ERR_NULL_ARGS), exception1.getMessage());

        Throwable exception2 = assertThrows(LsException.class, () ->
                app.listFolderContent(false, null, false, FOLDER_1)
        );
        assertEquals(String.format(STRING_LABEL_VALUE_PAIR, APP_LS, ERR_NULL_ARGS), exception2.getMessage());

        Throwable exception3 = assertThrows(LsException.class, () ->
                app.listFolderContent(false, false, null, FOLDER_1)
        );
        assertEquals(String.format(STRING_LABEL_VALUE_PAIR, APP_LS, ERR_NULL_ARGS), exception3.getMessage());
    }

    @Test
    public void listFolderContent_FileSupplied_WritesErrToStderr() {
        captureErr();

        assertDoesNotThrow(() -> {
            app.listFolderContent(false, false, false, FILE_1);

            assertEquals(new LsException(
                            String.format(STRING_LABEL_VALUE_PAIR, FILE_1, ERR_IS_NOT_DIR)
                    ).getMessage() + STRING_NEWLINE,
                    getErrOutput());
        });
    }
}