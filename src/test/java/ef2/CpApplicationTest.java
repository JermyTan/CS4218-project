package ef2;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.impl.app.CpApplication;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;

@Disabled
public class CpApplicationTest {

    private static final String ORIGINAL_DIR = Environment.currentDirectory;
    private static final String TESTDIR = Environment.currentDirectory + File.separator + RESOURCES_PATH + File.separator + "CpApplicationTest";

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

    private CpApplication app;
    private final InputStream stdin = mock(InputStream.class);
    private final OutputStream stdout = mock(OutputStream.class);

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

    // Check whether path1 and path2 contain the same content
    private void checkContent(Path path1, Path path2) {
        assertTrue(Files.exists(path1));
        assertTrue(Files.exists(path2));
        assertDoesNotThrow(() -> {
            String file1Content = new String(Files.readAllBytes(path1));
            String file2Content = new String(Files.readAllBytes(path2));
            assertEquals(file1Content, file2Content);
        });
    }

    @BeforeEach
    void setup() throws IOException {
        app = spy(new CpApplication());

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
    public void run_CopyFileNonRecursive_FileCopied() {
        // cp file1.txt file2.txt
        assertDoesNotThrow(() -> app.run(new String[]{FILE_1, FILE_2}, stdin, stdout));

        assertDoesNotThrow(() -> verify(app).cpSrcFileToDestFile(false, FILE_1, FILE_2));
        verifyNoInteractions(stdin);
        verifyNoInteractions(stdout);

        checkContent(file1, file2);
    }

    @Test
    public void run_CopyFolderRecursive_FolderCopied() throws IOException {
        // Create a file under folder1
        Files.createFile(folder1.resolve(FILE_1));

        // cp -R folder1 folder2 (Note that folder2 does not exist)
        assertDoesNotThrow(() -> app.run(new String[]{"-R", FOLDER_1, FOLDER_2}, stdin, stdout));

        assertDoesNotThrow(() -> verify(app).cpSrcFileToDestFile(true, FOLDER_1, FOLDER_2));
        verifyNoInteractions(stdin);
        verifyNoInteractions(stdout);

        // Dir copied
        assertTrue(Files.exists(folder2));
        // File copied over as well
        assertTrue(Files.exists(folder2.resolve(FILE_1)));
    }

    @Test
    public void run_CopyFilesNonRecursive_FolderSkipped() {
        assertDoesNotThrow(() -> app.run(new String[]{FOLDER_1, FILE_3, FOLDER_3}, stdin, stdout));

        // cp folder1 file3.txt folder3
        assertDoesNotThrow(() -> verify(app).cpFilesToFolder(false, FOLDER_3, FOLDER_1, FILE_3));
        verifyNoInteractions(stdin);
        verifyNoInteractions(stdout);

        // Dir not copied
        Path destPath1 = Paths.get(TESTDIR, FOLDER_3, FOLDER_1);
        assertTrue(Files.notExists(destPath1));

        // Only file3.txt is copied
        Path destPath2 = Paths.get(TESTDIR, FOLDER_3, FILE_3);
        checkContent(file3, destPath2);
    }

    @Test
    public void run_CopyFilesRecursive_FolderCopied() throws IOException {
        // Create a file under folder1
        Files.createFile(folder1.resolve(FILE_1));

        // cp -r folder1 folder3 (Note that folder 3 is an existing folder)
        assertDoesNotThrow(() -> app.run(new String[]{"-r", FOLDER_1, FOLDER_3}, stdin, stdout));

        assertDoesNotThrow(() -> verify(app).cpFilesToFolder(true, FOLDER_3, FOLDER_1));
        verifyNoInteractions(stdin);
        verifyNoInteractions(stdout);

        // Dir copied into folder3
        Path destPath1 = Paths.get(TESTDIR, FOLDER_3, FOLDER_1);
        assertTrue(Files.exists(destPath1));
        // File copied over as well
        assertTrue(Files.exists(destPath1.resolve(FILE_1)));
    }

    // Assume we do not allow this
    @Test
    public void run_IdenticalFile_ThrowsException() {
        assertThrows(Exception.class,() -> app.run(new String[]{FILE_1, FILE_2}, stdin, stdout));
    }

    // Assume we do not allow this
    @Test
    public void run_IdenticalFolder_ThrowsException() {
        assertThrows(Exception.class,() -> app.run(new String[]{"-r", FOLDER_1, FOLDER_1}, stdin, stdout));
    }

    @Test
    public void cpSrcFileToDestFile_DestFileExists_FileOverwritten() {
        assertTrue(Files.exists(file1));
        assertTrue(Files.exists(file3));

        assertDoesNotThrow(() -> app.cpSrcFileToDestFile(false, FILE_1, FILE_3));

        checkContent(file1, file3);
    }

    @Test
    public void cpSrcFileToDestFile_SrcFileDoesNotExist_ThrowsException() {
        assertTrue(Files.notExists(file2));
        String srcFile = FILE_2;
        String destFile = FILE_1;

        assertThrows(Exception.class, () -> app.cpSrcFileToDestFile(false, srcFile, destFile));
    }

    @Test
    public void cpSrcFileToDestFile_DestFolderDirDoesNotExist_ThrowsException() {
        String srcFile = FILE_1;
        String destFile = Paths.get(FOLDER_2, FILE_1).toString();

        assertThrows(Exception.class, () -> app.cpSrcFileToDestFile(false, srcFile, destFile));
    }

    @Test
    public void cpSrcFileToDestFile_FolderToFile_ThrowsException() {
        assertTrue(Files.exists(file1));
        String srcFile = FOLDER_1;
        String destFile = FILE_1;

        assertThrows(Exception.class, () -> app.cpSrcFileToDestFile(false, srcFile, destFile));
    }

    @Test
    public void cpSrcFileToDestFile_DestFileNotWritable_ThrowsException() {
        String srcFile = FILE_1;
        String destFile = Paths.get(FOLDER_1, FILE_1).toString();
        folder1.toFile().setWritable(false);

        assertThrows(Exception.class, () -> app.cpSrcFileToDestFile(false, srcFile, destFile));
    }

    @Test
    public void cpFilesToFolder_DestFolderDoesNotExist_ThrowsException() {
        assertTrue(Files.notExists(folder2));

        assertThrows(Exception.class, () -> app.cpFilesToFolder(false, FOLDER_2, FILE_1));
    }

    @Test
    public void cpFilesToFolder_DestFolderIsNotDirectory_ThrowsException() {
        assertFalse(Files.isDirectory(file1));

        assertThrows(Exception.class, () -> app.cpFilesToFolder(false, FILE_1, FILE_3));
    }

    @Test
    public void cpFilesToFolder_EmptyFileName_ThrowsException() {
        assertThrows(Exception.class, () -> app.cpFilesToFolder(false, FOLDER_1));
    }

    @Test
    public void cpFilesToFolder_SrcFileDoesNotExist_ThrowsException() {
        assertThrows(Exception.class, () -> app.cpFilesToFolder(false, FOLDER_1, FILE_2));
    }

    @Test
    public void cpFilesToFolder_SrcFolderContainsDestFolder_ThrowsException() {
        // Create dir folder1/folder2/
        try {
            Path destPath = Paths.get(TESTDIR, FOLDER_1, FOLDER_2);
            Files.createDirectory(destPath);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        String srcFile = FOLDER_1;
        String destFile = Paths.get(FOLDER_1, FOLDER_2).toString();

        // Cp folder1/ to folder1/folder2/
        assertThrows(Exception.class, () -> app.cpFilesToFolder(true, destFile, srcFile));
    }
}