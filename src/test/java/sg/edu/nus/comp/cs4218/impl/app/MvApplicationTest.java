package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_CANNOT_RENAME;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_ARG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_NOT_DIR;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.MvException;

class MvApplicationTest {

    private static final String ORIGINAL_DIR = Environment.currentDirectory;
    private static final String TESTDIR = Environment.currentDirectory + File.separator + RESOURCES_PATH + File.separator + "MvApplicationTest";

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";
    private static final String FILE_3 = "file3.txt";

    private static final String FOLDER_1 = "folder1";
    private static final String FOLDER_2 = "folder2";
    private static final String FOLDER_3 = "folder3";

    private final Path file1 = Paths.get(TESTDIR, FILE_1); // exists
    private final Path file2 = Paths.get(TESTDIR, FILE_2); // does not exist
    private final Path file3 = Paths.get(TESTDIR, FILE_3); // exists

    private final Path folder1 = Paths.get(TESTDIR, FOLDER_1); // exists
    private final Path folder2 = Paths.get(TESTDIR, FOLDER_2); // does not exist
    private final Path folder3 = Paths.get(TESTDIR, FOLDER_3); // exists

    private final List<Path> paths = List.of(file1, file2, file3, folder1, folder2, folder3);

    private MvApplication app;
    private InputStream stdin;
    private OutputStream stdout;

    @BeforeAll
    static void setupBeforeAll() {
        Environment.currentDirectory = TESTDIR;
    }

    @AfterAll
    static void tearDownAfterAll() {
        Environment.currentDirectory = ORIGINAL_DIR;
    }

    private void createFileWithContent(Path path, String content) throws IOException {
        Files.createFile(path);
        BufferedWriter outputStream = new BufferedWriter(new FileWriter(path.toFile(), true));
        outputStream.append(content);
        outputStream.close();
    }

    private String constructRenameErrorMsg(String srcFile, String destFile, String error) {
        return String.format("rename %s to %s: %s", srcFile, destFile, error);
    }

    @BeforeEach
    void setup() throws IOException {
        app = spy(new MvApplication());
        stdin = mock(InputStream.class);
        stdout = mock(OutputStream.class);

        createFileWithContent(file1, FILE_1);
        createFileWithContent(file3, FILE_3);

        Files.createDirectory(folder1);
        Files.createDirectory(folder3);
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
    public void run_RenameFile_CorrectMethodCall() {
        assertTrue(Files.exists(file1));
        assertTrue(Files.notExists(file2));

        assertDoesNotThrow(() -> app.run(new String[]{FILE_1, FILE_2}, stdin, stdout));

        assertDoesNotThrow(() -> verify(app).mvSrcFileToDestFile(FILE_1, FILE_2));
        verifyNoInteractions(stdin);
        verifyNoInteractions(stdout);

        // File renamed
        assertTrue(Files.notExists(file1));
        assertTrue(Files.exists(file2));
    }

    @Test
    public void run_MoveFiles_CorrectMethodCall() {
        Path destPath1 = Paths.get(TESTDIR, FOLDER_3, FILE_1);
        assertTrue(Files.notExists(destPath1));

        Path destPath2 = Paths.get(TESTDIR, FOLDER_3, FILE_3);
        assertTrue(Files.notExists(destPath2));

        assertDoesNotThrow(() -> app.run(new String[]{FILE_1, FILE_3, FOLDER_3}, stdin, stdout));

        assertDoesNotThrow(() -> verify(app).mvFilesToFolder(FOLDER_3, FILE_1, FILE_3));
        verifyNoInteractions(stdin);
        verifyNoInteractions(stdout);

        // Files moved
        assertTrue(Files.notExists(file1));
        assertTrue(Files.notExists(file3));
        assertTrue(Files.exists(destPath1));
        assertTrue(Files.exists(destPath2));
    }

    @Test
    public void run_DoesNotOverwrite_FileNotOverwritten() {
        assertDoesNotThrow(() -> app.run(new String[]{"-n", FILE_1, FILE_3}, stdin, stdout));

        assertDoesNotThrow(() -> {
            String fileContent = new String(Files.readAllBytes(file3));
            assertEquals(FILE_3, fileContent);
        });
    }

    @Test
    public void run_DoesNotOverwrite_FileNotMoved() {
        Path destPath = Paths.get(TESTDIR, FOLDER_1, FILE_1);
        String originalFileContent = destPath.toString();
        assertDoesNotThrow(() -> createFileWithContent(destPath, originalFileContent));

        assertDoesNotThrow(() -> app.run(new String[]{"-n", FILE_1, FOLDER_1}, stdin, stdout));

        assertDoesNotThrow(() -> {
            String fileContent = new String(Files.readAllBytes(destPath));
            assertEquals(originalFileContent, fileContent);
        });
    }

    @Test
    public void run_DoesNotOverwrite_ConflictingFilesUnmoved() {
        // folder1 contains file1.txt only
        Path destPath = Paths.get(TESTDIR, FOLDER_1, FILE_1);
        String originalFileContent = destPath.toString();
        assertDoesNotThrow(() -> createFileWithContent(destPath, originalFileContent));

        // Move file1.txt and file3.txt to folder1
        assertDoesNotThrow(() -> app.run(new String[]{"-n", FILE_1, FILE_3, FOLDER_1}, stdin, stdout));

        // Only file3.txt moved
        assertTrue(Files.exists(Paths.get(TESTDIR, FOLDER_1, FILE_3)));

        // folder1/file1.txt content not overwritten
        assertDoesNotThrow(() -> {
            String fileContent = new String(Files.readAllBytes(destPath));
            assertEquals(originalFileContent, fileContent);
        });
    }

    @Test
    public void run_FailedToMv_MvExceptionThrown() {
        // file2.txt does not exist, hence cannot rename
        assertThrows(MvException.class, () -> app.run(new String[]{FILE_2, FILE_1}, stdin, stdout));
    }


    @Test
    public void mvSrcFileToDestFile_SameDirectoryRenameFile_FileRenamed() {
        assertTrue(Files.exists(file1));
        assertTrue(Files.notExists(file2));

        assertDoesNotThrow(() -> app.mvSrcFileToDestFile(FILE_1, FILE_2));

        assertTrue(Files.notExists(file1));
        assertTrue(Files.exists(file2));
    }

    @Test
    public void mvSrcFileToDestFile_DifferentDirectorySameFileName_FileRenamed() {
        Path destPath = Paths.get(TESTDIR, FOLDER_1, FILE_1);

        assertTrue(Files.exists(file1));
        assertTrue(Files.notExists(destPath));

        assertDoesNotThrow(() -> app.mvSrcFileToDestFile(FILE_1, Paths.get(FOLDER_1, FILE_1).toString()));

        assertTrue(Files.notExists(file1));
        assertTrue(Files.exists(destPath));
    }

    @Test
    public void mvSrcFileToDestFile_DifferentDirectoryDifferentFileName_FileRenamed() {
        Path destPath = Paths.get(TESTDIR, FOLDER_1, FILE_2);

        assertTrue(Files.exists(file1));
        assertTrue(Files.notExists(destPath));

        assertDoesNotThrow(() -> app.mvSrcFileToDestFile(FILE_1, Paths.get(FOLDER_1, FILE_2).toString()));

        assertTrue(Files.notExists(file1));
        assertTrue(Files.exists(destPath));
    }

    @Test
    public void mvSrcFileToDestFile_DestFileExists_FileOverwritten() {
        assertTrue(Files.exists(file1));
        assertTrue(Files.exists(file3));

        assertDoesNotThrow(() -> app.mvSrcFileToDestFile(FILE_1, FILE_3));

        assertTrue(Files.notExists(file1));
        assertTrue(Files.exists(file3));

        assertDoesNotThrow(() -> {
            String fileContent = new String(Files.readAllBytes(file3));
            assertEquals(FILE_1, fileContent);
        });
    }

    @Test
    public void mvSrcFileToDestFile_SameDirectoryRenameFolder_FolderRenamed() {
        assertTrue(Files.exists(folder1));
        assertTrue(Files.notExists(folder2));

        assertDoesNotThrow(() -> app.mvSrcFileToDestFile(FOLDER_1, FOLDER_2));

        assertTrue(Files.notExists(folder1));
        assertTrue(Files.exists(folder2));
    }

    @Test
    public void mvSrcFileToDestFile_DifferentDirectorySameFolderName_FolderRenamed() {
        Path destPath = Paths.get(TESTDIR, FOLDER_3, FOLDER_1);

        assertTrue(Files.exists(folder1));
        assertTrue(Files.notExists(destPath));

        assertDoesNotThrow(() -> app.mvSrcFileToDestFile(FOLDER_1, Paths.get(FOLDER_3, FOLDER_1).toString()));

        assertTrue(Files.notExists(folder1));
        assertTrue(Files.exists(destPath));
    }

    @Test
    public void mvSrcFileToDestFile_DifferentDirectoryDifferentFolderName_FolderRenamed() {
        Path destPath = Paths.get(TESTDIR, FOLDER_3, FOLDER_2);

        assertTrue(Files.exists(folder1));
        assertTrue(Files.notExists(destPath));

        assertDoesNotThrow(() -> app.mvSrcFileToDestFile(FOLDER_1, Paths.get(FOLDER_3, FOLDER_2).toString()));

        assertTrue(Files.notExists(folder1));
        assertTrue(Files.exists(destPath));
    }

    @Test
    public void mvSrcFileToDestFile_SrcFileDoesNotExist_ThrowsException() {
        assertTrue(Files.notExists(file2));
        String srcFile = FILE_2;
        String destFile = FILE_1;

        Throwable error = assertThrows(Exception.class, () -> app.mvSrcFileToDestFile(srcFile, destFile));
        assertEquals(constructRenameErrorMsg(srcFile, destFile, ERR_FILE_NOT_FOUND), error.getMessage());
    }

    @Test
    public void mvSrcFileToDestFile_SrcFolderDoesNotExist_ThrowsException() {
        assertTrue(Files.notExists(folder2));
        String srcFile = FOLDER_2;
        String destFile = FOLDER_1;

        Throwable error = assertThrows(Exception.class, () -> app.mvSrcFileToDestFile(srcFile, destFile));
        assertEquals(constructRenameErrorMsg(srcFile, destFile, ERR_FILE_NOT_FOUND), error.getMessage());
    }

    @Test
    public void mvSrcFileToDestFile_DestFolderDirDoesNotExist_ThrowsException() {
        String srcFile = FILE_1;
        String destFile = Paths.get(FOLDER_2, FILE_1).toString();

        Throwable error = assertThrows(Exception.class, () -> app.mvSrcFileToDestFile(srcFile, destFile));
        assertEquals(constructRenameErrorMsg(srcFile, destFile, ERR_FILE_NOT_FOUND), error.getMessage());
    }

    @Test
    public void mvSrcFileToDestFile_DestFolderExists_ThrowsException() {
        assertTrue(Files.exists(folder3));

        assertThrows(Exception.class, () -> app.mvSrcFileToDestFile(FOLDER_1, FOLDER_3));
    }

    @Test
    public void mvSrcFileToDestFile_FolderToFile_ThrowsException() {
        assertTrue(Files.exists(file1));
        String srcFile = FOLDER_1;
        String destFile = FILE_1;

        Throwable error = assertThrows(Exception.class, () -> app.mvSrcFileToDestFile(srcFile, destFile));
        assertEquals(constructRenameErrorMsg(srcFile, destFile, ERR_IS_NOT_DIR), error.getMessage());
    }

    @Test
    public void mvSrcFileToDestFile_SrcFolderContainsDestFolder_ThrowsException() {
        String srcFile = FOLDER_1;
        String destFile = Paths.get(FOLDER_1, FOLDER_2).toString();

        Throwable error = assertThrows(Exception.class, () -> app.mvSrcFileToDestFile(srcFile, destFile));
        assertEquals(constructRenameErrorMsg(srcFile, destFile, ERR_INVALID_ARG), error.getMessage());
    }

    @Test
    public void mvSrcFileToDestFile_DestFileNotWritable_ThrowsException() {
        String srcFile = FILE_1;
        String destFile = Paths.get(FOLDER_1, FILE_1).toString();
        folder1.toFile().setWritable(false);

        Throwable error = assertThrows(Exception.class, () -> app.mvSrcFileToDestFile(srcFile, destFile));
        assertEquals(constructRenameErrorMsg(srcFile, destFile, ERR_CANNOT_RENAME), error.getMessage());
    }

    @Test
    public void mvFilesToFolder_DestFolderDoesNotExist_ThrowsException() {
        assertTrue(Files.notExists(folder2));

        assertThrows(Exception.class, () -> app.mvFilesToFolder(FOLDER_2, FILE_1));
    }

    @Test
    public void mvFilesToFolder_DestFolderIsNotDirectory_ThrowsException() {
        assertFalse(Files.isDirectory(file1));

        assertThrows(Exception.class, () -> app.mvFilesToFolder(FILE_1, FILE_3));
    }

    @Test
    public void mvFilesToFolder_NoFileName_ThrowsException() {
        assertThrows(Exception.class, () -> app.mvFilesToFolder(FOLDER_1));
    }

    @Test
    public void mvFilesToFolder_FileDoesNotExist_ThrowsException() {
        assertThrows(Exception.class, () -> app.mvFilesToFolder(FOLDER_1, FILE_2));
    }

    @Test
    public void mvFilesToFolder_SrcFolderContainsDestFolder_ThrowsException() {
        // Create dir folder1/folder2
        try {
            Path destPath = Paths.get(TESTDIR, FOLDER_1, FOLDER_2);
            Files.createDirectory(destPath);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        String srcFile = FOLDER_1;
        String destFile = Paths.get(FOLDER_1, FOLDER_2).toString();

        // Move folder1 to folder1/folder2
        assertThrows(Exception.class, () -> app.mvFilesToFolder(destFile, srcFile));
    }

    @Test
    public void mvFilesToFolder_SingleFile_FileMoved() {
        Path destPath = Paths.get(TESTDIR, FOLDER_1, FILE_1);
        assertTrue(Files.notExists(destPath));

        assertDoesNotThrow(() -> app.mvFilesToFolder(FOLDER_1, FILE_1));

        assertTrue(Files.notExists(file1));
        assertTrue(Files.exists(destPath));
    }

    @Test
    public void mvFilesToFolder_MultipleFiles_FilesMoved() {
        Path destPath1 = Paths.get(TESTDIR, FOLDER_1, FILE_1);
        assertTrue(Files.notExists(destPath1));

        Path destPath2 = Paths.get(TESTDIR, FOLDER_1, FILE_3);
        assertTrue(Files.notExists(destPath2));

        assertDoesNotThrow(() -> app.mvFilesToFolder(FOLDER_1, FILE_1, FILE_3));

        assertTrue(Files.notExists(file1));
        assertTrue(Files.notExists(file3));
        assertTrue(Files.exists(destPath1));
        assertTrue(Files.exists(destPath2));
    }
}