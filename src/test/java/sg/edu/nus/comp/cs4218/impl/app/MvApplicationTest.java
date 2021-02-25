package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.MvException;

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

class MvApplicationTest {

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";
    private static final String FILE_3 = "file3.txt";

    private static final String FOLDER_1 = "folder1";
    private static final String FOLDER_2 = "folder2";
    private static final String FOLDER_3 = "folder3";

    private final File file1 = new File(FILE_1); // exists
    private final File file2 = new File(FILE_2); // does not exist
    private final File file3 = new File(FILE_3); // exists
    private final File folder1 = new File(FOLDER_1); // exists
    private final File folder2 = new File(FOLDER_2); // does not exist
    private final File folder3 = new File(FOLDER_3); // exists

    private final List<File> files = List.of(file1, file2, file3, folder1, folder2, folder3);

    private MvApplication app;

    private void createFileWithContent(File file, String content) {
        try {
            file.createNewFile();
            BufferedWriter outputStream = new BufferedWriter(new FileWriter(file, true));
            outputStream.append(content);
            outputStream.close();
        } catch (IOException e) {
            fail();
        }
    }

    @BeforeEach
    void setup() {
        app = new MvApplication();

        createFileWithContent(file1, FILE_1);
        createFileWithContent(file3, FILE_3);

        folder1.mkdir();
        folder3.mkdir();
    }

    @AfterEach
    void tearDown() {
        try {
            for (File file : files) {
                if (!file.isDirectory()) {
                    file.delete();
                } else {
                    Files.walk(Paths.get(file.getPath()))
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                }
            }
        } catch (IOException e) {
            fail();
        }
    }


    @Test
    public void mvSrcFileToDestFile_SameDirectoryRenameFile_FileRenamed() {
        assertTrue(file1.exists());
        assertFalse(file2.exists());

        assertDoesNotThrow(() -> app.mvSrcFileToDestFile(FILE_1, FILE_2));

        assertFalse(file1.exists());
        assertTrue(file2.exists());
    }

    @Test
    public void mvSrcFileToDestFile_DifferentDirectorySameFileName_FileRenamed() {
        String destFilePath = FOLDER_1 + File.separator + FILE_1;
        File destFileObj = new File(destFilePath);

        assertTrue(file1.exists());
        assertFalse(destFileObj.exists());

        assertDoesNotThrow(() -> app.mvSrcFileToDestFile(FILE_1, destFilePath));

        assertFalse(file1.exists());
        assertTrue(destFileObj.exists());
    }

    @Test
    public void mvSrcFileToDestFile_DifferentDirectoryDifferentFileName_FileRenamed() {
        String destFilePath = FOLDER_1 + File.separator + FILE_2;
        File destFileObj = new File(destFilePath);

        assertTrue(file1.exists());
        assertFalse(destFileObj.exists());

        assertDoesNotThrow(() -> app.mvSrcFileToDestFile(FILE_1, destFilePath));

        assertFalse(file1.exists());
        assertTrue(destFileObj.exists());
    }

    @Test
    public void mvSrcFileToDestFile_DestFileExists_FileOverwritten() {
        assertTrue(file1.exists());
        assertTrue(file3.exists());

        assertDoesNotThrow(() -> app.mvSrcFileToDestFile(FILE_1, FILE_3));

        assertFalse(file1.exists());
        assertTrue(file3.exists());

        assertDoesNotThrow(() -> {
            String fileContent = new String(Files.readAllBytes(Paths.get(FILE_3)));
            assertEquals(FILE_1, fileContent);
        });
    }

    @Test
    public void mvSrcFileToDestFile_SameDirectoryRenameFolder_FolderRenamed() {
        assertTrue(folder1.exists());
        assertFalse(folder2.exists());

        assertDoesNotThrow(() -> app.mvSrcFileToDestFile(FOLDER_1, FOLDER_2));

        assertFalse(folder1.exists());
        assertTrue(folder2.exists());
    }

    @Test
    public void mvSrcFileToDestFile_DifferentDirectorySameFolderName_FolderRenamed() {
        String destFilePath = FOLDER_3 + File.separator + FOLDER_1;
        File destFileObj = new File(destFilePath);

        assertTrue(folder1.exists());
        assertFalse(destFileObj.exists());

        assertDoesNotThrow(() -> app.mvSrcFileToDestFile(FOLDER_1, destFilePath));

        assertFalse(folder1.exists());
        assertTrue(destFileObj.exists());
    }

    @Test
    public void mvSrcFileToDestFile_DifferentDirectoryDifferentFolderName_FolderRenamed() {
        String destFilePath = FOLDER_3 + File.separator + FOLDER_2;
        File destFileObj = new File(destFilePath);

        assertTrue(folder1.exists());
        assertFalse(destFileObj.exists());

        assertDoesNotThrow(() -> app.mvSrcFileToDestFile(FOLDER_1, destFilePath));

        assertFalse(folder1.exists());
        assertTrue(destFileObj.exists());
    }

    @Test
    public void mvSrcFileToDestFile_SrcFileDoesNotExist_ThrowsException() {
        assertFalse(file2.exists());

        assertThrows(Exception.class, () -> app.mvSrcFileToDestFile(FILE_2, FILE_1));
    }

    @Test
    public void mvSrcFileToDestFile_SrcFolderDoesNotExist_ThrowsException() {
        assertFalse(folder2.exists());

        assertThrows(Exception.class, () -> app.mvSrcFileToDestFile(FOLDER_2, FOLDER_2));
    }

    @Test
    public void mvSrcFileToDestFile_DestFolderExists_ThrowsException() {
        assertTrue(folder3.exists());

        assertThrows(Exception.class, () -> app.mvSrcFileToDestFile(FOLDER_1, FOLDER_3));
    }

    @Test
    public void mvSrcFileToDestFile_FolderToExistingFile_ThrowsException() {
        assertTrue(file1.exists());

        assertThrows(Exception.class, () -> app.mvSrcFileToDestFile(FOLDER_1, FILE_1));
    }

    @Test
    public void mvFilesToFolder_DestFolderDoesNotExist_ThrowsException() {
        assertFalse(folder2.exists());

        assertThrows(Exception.class, () -> app.mvFilesToFolder(FOLDER_2, FILE_1));
    }

    @Test
    public void mvFilesToFolder_DestFolderIsNotDirectory_ThrowsException() {
        assertFalse(file1.isDirectory());

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
    public void mvFilesToFolder_SingleFile_FileMoved() {
        String destFilePath = FOLDER_1 + File.separator + FILE_1;
        File destFile = new File(destFilePath);
        assertFalse(destFile.exists());

        assertDoesNotThrow(() -> app.mvFilesToFolder(FOLDER_1, FILE_1));

        assertFalse(file1.exists());
        assertTrue(destFile.exists());
    }

    @Test
    public void mvFilesToFolder_MultipleFiles_FilesMoved() {
        File destFile1 = new File(FOLDER_1 + File.separator + FILE_1);
        assertFalse(destFile1.exists());

        File destFile2 = new File(FOLDER_1 + File.separator + FILE_3);
        assertFalse(destFile2.exists());

        assertDoesNotThrow(() -> app.mvFilesToFolder(FOLDER_1, FILE_1, FILE_3));

        assertFalse(file1.exists());
        assertFalse(file3.exists());
        assertTrue(destFile1.exists());
        assertTrue(destFile2.exists());
    }
}