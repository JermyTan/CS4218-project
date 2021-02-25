package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

class MvApplicationTest {

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";
    private static final String FILE_3 = "file3.txt";

    private static final String FOLDER_1 = "folder1";
    private static final String FOLDER_2 = "folder2";
    private static final String FOLDER_3 = "folder3";

    private final Path file1 = Paths.get(FILE_1); // exists
    private final Path file2 = Paths.get(FILE_2); // does not exist
    private final Path file3 = Paths.get(FILE_3); // exists
    private final Path folder1 = Paths.get(FOLDER_1); // exists
    private final Path folder2 = Paths.get(FOLDER_2); // does not exist
    private final Path folder3 = Paths.get(FOLDER_3); // exists

    private final List<Path> paths = List.of(file1, file2, file3, folder1, folder2, folder3);

    private MvApplication app;

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
    void setup() {
        app = new MvApplication();

        try {
            createFileWithContent(file1, FILE_1);
            createFileWithContent(file3, FILE_3);

            Files.createDirectory(folder1);
            Files.createDirectory(folder3);
        } catch (IOException e ) {
            fail(e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        try {
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
        } catch (IOException e) {
            fail(e.getMessage());
        }
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
        Path destPath = Paths.get(FOLDER_1, FILE_1);

        assertTrue(Files.exists(file1));
        assertTrue(Files.notExists(destPath));

        assertDoesNotThrow(() -> app.mvSrcFileToDestFile(FILE_1, destPath.toString()));

        assertTrue(Files.notExists(file1));
        assertTrue(Files.exists(destPath));
    }

    @Test
    public void mvSrcFileToDestFile_DifferentDirectoryDifferentFileName_FileRenamed() {
        Path destPath = Paths.get(FOLDER_1, FILE_2);

        assertTrue(Files.exists(file1));
        assertTrue(Files.notExists(destPath));

        assertDoesNotThrow(() -> app.mvSrcFileToDestFile(FILE_1, destPath.toString()));

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
        Path destPath = Paths.get(FOLDER_3, FOLDER_1);

        assertTrue(Files.exists(folder1));
        assertTrue(Files.notExists(destPath));

        assertDoesNotThrow(() -> app.mvSrcFileToDestFile(FOLDER_1, destPath.toString()));

        assertTrue(Files.notExists(folder1));
        assertTrue(Files.exists(destPath));
    }

    @Test
    public void mvSrcFileToDestFile_DifferentDirectoryDifferentFolderName_FolderRenamed() {
        Path destPath = Paths.get(FOLDER_3, FOLDER_2);

        assertTrue(Files.exists(folder1));
        assertTrue(Files.notExists(destPath));

        assertDoesNotThrow(() -> app.mvSrcFileToDestFile(FOLDER_1, destPath.toString()));

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
        Path destPath = Paths.get(FOLDER_2, FILE_1);
        String srcFile = FILE_1;
        String destFile = destPath.toString();

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
        Path destPath = Paths.get(FOLDER_1, FOLDER_2);
        String srcFile = FOLDER_1;
        String destFile = destPath.toString();

        Throwable error = assertThrows(Exception.class, () -> app.mvSrcFileToDestFile(srcFile, destFile));
        assertEquals(constructRenameErrorMsg(srcFile, destFile, ERR_INVALID_ARG), error.getMessage());
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
        Path destPath = Paths.get(FOLDER_1, FOLDER_2);
        String srcFile = FOLDER_1;
        String destFile = destPath.toString();

        try {
            Files.createDirectory(destPath);
        } catch (IOException e ) {
            fail(e.getMessage());
        }

        assertThrows(Exception.class, () -> app.mvFilesToFolder(destFile, srcFile));
    }

    @Test
    public void mvFilesToFolder_SingleFile_FileMoved() {
        Path destPath = Paths.get(FOLDER_1, FILE_1);

        assertTrue(Files.notExists(destPath));

        assertDoesNotThrow(() -> app.mvFilesToFolder(FOLDER_1, FILE_1));

        assertTrue(Files.notExists(file1));
        assertTrue(Files.exists(destPath));
    }

    @Test
    public void mvFilesToFolder_MultipleFiles_FilesMoved() {
        Path destPath1 = Paths.get(FOLDER_1, FILE_1);
        assertTrue(Files.notExists(destPath1));

        Path destPath2 = Paths.get(FOLDER_1, FILE_3);
        assertTrue(Files.notExists(destPath2));

        assertDoesNotThrow(() -> app.mvFilesToFolder(FOLDER_1, FILE_1, FILE_3));

        assertTrue(Files.notExists(file1));
        assertTrue(Files.notExists(file3));
        assertTrue(Files.exists(destPath1));
        assertTrue(Files.exists(destPath2));
    }
}