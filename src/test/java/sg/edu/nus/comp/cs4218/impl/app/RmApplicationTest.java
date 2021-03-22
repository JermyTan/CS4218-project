package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_FILES;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_FILE_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.RmException;

class RmApplicationTest {

    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + RESOURCES_PATH + STRING_FILE_SEP + "RmApplicationTest";
    private static final Path TEST_PATH = Path.of(TEST_DIR);

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";
    private static final String FILE_3 = "file3.txt"; // does not exist

    private static final String FOLDER_1 = "folder1";
    private static final String FOLDER_2 = "folder2";
    private static final String FOLDER_3 = "folder3"; // does not exist
    private static final String EMPTY_FOLDER = "emptyFolder";

    private final Path file1 = Path.of(TEST_DIR, FILE_1);
    private final Path file2 = Path.of(TEST_DIR, FILE_2);
    private final Path folder1 = Path.of(TEST_DIR, FOLDER_1);
    private final Path folder2 = Path.of(TEST_DIR, FOLDER_2);
    private final Path emptyFolder = Path.of(TEST_DIR, EMPTY_FOLDER);

    private final List<Path> paths = List.of(file1, file2, folder1, folder2, emptyFolder);
    private final InputStream stdin = mock(InputStream.class);
    private final OutputStream stdout = mock(OutputStream.class);
    private RmApplication app;

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

    @BeforeEach
    void setup() throws IOException {
        app = spy(new RmApplication());

        Files.createFile(file1);
        Files.createFile(file2);

        Files.createDirectory(folder1);
        Files.createDirectory(folder2);
        Files.createDirectory(emptyFolder);
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
    public void run_NullArgList_ThrowsException() {
        assertThrows(RmException.class, () -> app.run(null, stdin, stdout));
    }

    @Test
    public void run_EmptyArgList_ThrowsException() {
        assertThrows(RmException.class, () -> app.run(new String[0], stdin, stdout));
    }

    @Test
    public void run_ArgListContainsNull_ThrowsException() {
        assertThrows(RmException.class, () -> app.run(new String[]{null}, stdin, stdout));
    }

    @Test
    public void run_MissingArgs_ThrowsException() {
        assertThrows(RmException.class, () -> app.run(new String[]{"-d"}, stdin, stdout));
        assertThrows(RmException.class, () -> app.run(new String[]{"-r"}, stdin, stdout));
    }

    @Test
    public void run_FileDoesNotExist_ThrowsException() {
        Throwable exception = assertThrows(RmException.class, () -> app.run(new String[]{FILE_3}, stdin, stdout));
        assertEquals(new RmException(ERR_FILE_NOT_FOUND).getMessage(), exception.getMessage());
    }

    @Test
    public void run_FolderDoesNotExistWithoutFlags_ThrowsException() {
        Throwable exception = assertThrows(RmException.class, () -> app.run(new String[]{FOLDER_3}, stdin, stdout));
        assertEquals(new RmException(ERR_FILE_NOT_FOUND).getMessage(), exception.getMessage());
    }

    @Test
    public void run_FolderDoesNotExistWithFlags_ThrowsException() {
        Throwable exception = assertThrows(RmException.class, () -> app.run(new String[]{"-d",
                FOLDER_3}, stdin, stdout));
        assertEquals(new RmException(ERR_FILE_NOT_FOUND).getMessage(), exception.getMessage());
        exception = assertThrows(Exception.class, () -> app.run(new String[]{"-r", FOLDER_3}, stdin, stdout));
        assertEquals(new RmException(ERR_FILE_NOT_FOUND).getMessage(), exception.getMessage());
    }

    // Commented out this test because this test is testing for permission,
    // but its behavior is OS dependent
//    @Test
//    public void run_FileNotWritable_ThrowsException() {
//        file1.toFile().setWritable(false);
//
//        assertThrows(Exception.class, () -> app.run(new String[]{FILE_1}, stdin, stdout));
//    }

    @Test
    public void run_RemoveSingleFile_FileRemoved() {
        assertDoesNotThrow(() -> {
            app.run(new String[]{FILE_1}, stdin, stdout);
            verify(app).remove(false, false, FILE_1);
        });

        assertTrue(Files.notExists(file1));
    }

    @Test
    public void run_RemoveMultipleFiles_FilesRemoved() {
        assertDoesNotThrow(() -> {
            app.run(new String[]{FILE_1, FILE_2}, stdin, stdout);
            verify(app).remove(false, false, FILE_1, FILE_2);
        });

        assertTrue(Files.notExists(file1));
        assertTrue(Files.notExists(file2));
    }

    @Test
    public void run_RemoveEmptyDirWithoutEmptyDirFlag_ThrowsException() {
        Throwable exception = assertThrows(RmException.class, () -> app.run(new String[]{FILE_1,
                EMPTY_FOLDER,
                FILE_2}, stdin, stdout));
        assertEquals(new RmException(ERR_IS_DIR).getMessage(), exception.getMessage());

        // Only file1 removed
        assertTrue(Files.notExists(file1));

        // emptyDir remains
        assertTrue(Files.exists(emptyFolder));

        // file2 remains (as the previous rm operation failed)
        assertTrue(Files.exists(file2));
    }

    @Test
    public void run_RemoveEmptyDirWithEmptyDirFlag_FolderRemoved() {
        assertDoesNotThrow(() -> {
            app.run(new String[]{"-d", EMPTY_FOLDER}, stdin, stdout);
            verify(app).remove(true, false, EMPTY_FOLDER);
        });

        assertTrue(Files.notExists(emptyFolder));
    }

    @Test
    public void run_RemoveNonEmptyDirWithEmptyDirFlag_ThrowsException() throws IOException {
        // Create a file under folder1
        Files.createFile(folder1.resolve(FILE_1));

        Throwable exception = assertThrows(RmException.class, () -> app.run(new String[]{"-d",
                FOLDER_1}, stdin, stdout));
        assertEquals(new RmException(ERR_IS_DIR).getMessage(), exception.getMessage());

        // folder1 not removed
        assertTrue(Files.exists(folder1));
    }

    @Test
    public void run_RemoveFileWithEmptyDirFlag_FileRemoved() {
        assertDoesNotThrow(() -> {
            app.run(new String[]{"-d", FILE_1}, stdin, stdout);
            verify(app).remove(true, false, FILE_1);
        });

        assertTrue(Files.notExists(file1));
    }

    @Test
    public void run_RemoveFileWithRecursiveFlag_FileRemoved() {
        assertDoesNotThrow(() -> {
            app.run(new String[]{"-r", FILE_1}, stdin, stdout);
            verify(app).remove(false, true, FILE_1);
        });

        assertTrue(Files.notExists(file1));
    }

    @Test
    public void run_RemoveDirectoryRecursive_FolderRemoved() throws IOException {
        // Create a file under folder1
        Path filePath = folder1.resolve(FILE_1);
        Files.createFile(filePath);

        // Create a folder under folder1
        Path folderPath = folder1.resolve(FOLDER_2);
        Files.createDirectory(folderPath);

        assertDoesNotThrow(() -> {
            app.run(new String[]{"-r", FOLDER_1}, stdin, stdout);
            verify(app).remove(false, true, FOLDER_1);
        });

        // Both folder and its content get deleted
        assertTrue(Files.notExists(folder1));
        assertTrue(Files.notExists(filePath));
        assertTrue(Files.notExists(folderPath));
    }

    @Test
    public void run_RemoveNonEmptyDirWithBothRecursiveAndEmptyFlags_FolderRemoved() throws IOException {
        // Create a file under folder1
        Path filePath = folder1.resolve(FILE_1);
        Files.createFile(filePath);

        // Create a folder under folder1
        Path folderPath = folder1.resolve(FOLDER_2);
        Files.createDirectory(folderPath);

        assertDoesNotThrow(() -> {
            app.run(new String[]{"-r", "-d", FOLDER_1}, stdin, stdout);
            verify(app).remove(true, true, FOLDER_1);
        });

        // Both folder and its content get deleted
        assertTrue(Files.notExists(folder1));
        assertTrue(Files.notExists(filePath));
        assertTrue(Files.notExists(folderPath));
    }

    @Test
    public void run_RemoveEmptyFolderRecursive_FolderRemoved() {
        assertDoesNotThrow(() -> {
            app.run(new String[]{"-r", EMPTY_FOLDER}, stdin, stdout);
            verify(app).remove(false, true, EMPTY_FOLDER);
        });

        assertTrue(Files.notExists(emptyFolder));
    }

    @Test
    public void run_RemoveMultipleDirectoriesRecursive_FolderRemoved() throws IOException {
        // Create a file under folder1
        Path filePath1 = folder1.resolve(FILE_1);
        Files.createFile(filePath1);

        // Create a file under folder2
        Path filePath2 = folder2.resolve(FILE_1);
        Files.createFile(filePath2);

        assertDoesNotThrow(() -> {
            app.run(new String[]{"-r", FOLDER_1, FOLDER_2}, stdin, stdout);
            verify(app).remove(false, true, FOLDER_1, FOLDER_2);
        });

        // Both folders and their contents get deleted
        assertTrue(Files.notExists(folder1));
        assertTrue(Files.notExists(filePath1));
        assertTrue(Files.notExists(folder2));
        assertTrue(Files.notExists(filePath2));
    }

    @Test
    public void run_InvalidOption_ThrowsException() {
        assertThrows(RmException.class, () -> app.run(new String[]{"-X", FILE_1}, stdin, stdout));
    }

    @Test
    public void remove_NullArgs_ThrowsException() {
        Throwable exception = assertThrows(Exception.class, () -> app.remove(null, false, FILE_1));
        assertEquals(new Exception(ERR_NULL_ARGS).getMessage(), exception.getMessage());
        exception = assertThrows(Exception.class, () -> app.remove(false, null, FILE_1));
        assertEquals(new Exception(ERR_NULL_ARGS).getMessage(), exception.getMessage());
    }

    @Test
    public void remove_NoFilenames_ThrowsException() {
        Throwable exception = assertThrows(Exception.class, () -> app.remove(false, false));
        assertEquals(new Exception(ERR_NO_FILE_ARGS).getMessage(), exception.getMessage());
        exception = assertThrows(Exception.class, () -> app.remove(false, false, (String[]) null));
        assertEquals(new Exception(ERR_NO_FILE_ARGS).getMessage(), exception.getMessage());
    }

    @Test
    public void remove_InvalidFilenames_ThrowsException() {
        Throwable exception = assertThrows(Exception.class, () -> app.remove(false, false, null, FILE_1));
        assertEquals(new Exception(ERR_INVALID_FILES).getMessage(), exception.getMessage());
    }
}