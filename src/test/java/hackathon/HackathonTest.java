package hackathon;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.*;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.app.MvApplication.constructRenameErrorMsg;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_DIR_NOT_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;

public class HackathonTest {

    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + RESOURCES_PATH + STRING_FILE_SEP + "HackathonTest";
    private static final Path TEST_PATH = Path.of(TEST_DIR);

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";
    private static final String FOLDER_1 = "folder1";
    private static final String FOLDER_2 = "folder2";

    private final Path file1 = Path.of(TEST_DIR, FILE_1);
    private final Path file2 = Path.of(TEST_DIR, FILE_2);
    private final Path folder1 = Path.of(TEST_DIR, FOLDER_1);
    private final Path folder2 = Path.of(TEST_DIR, FOLDER_2);

    private final List<Path> paths = List.of(file1, file2, folder1, folder2);

    private final OutputStream stdout = new ByteArrayOutputStream();
    private ShellImpl shell;

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
    void setUp() throws IOException {
        Files.createFile(file1);
        Files.createFile(file2);
        Files.createDirectory(folder1);
        Files.createDirectory(folder2);

        shell = new ShellImpl();
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
    @DisplayName("Bug #10 from team 16")
    void parseAndEvaluate_MvFolderToDifferentDirWithConflictingFolder_FolderOverwritten() throws IOException {
        // Create a folder folder2 under folder1
        Path destPath = folder1.resolve(FOLDER_2);
        Files.createDirectory(destPath);

        // Create a file under folder2
        Files.createFile(folder2.resolve(FILE_1));

        assertDoesNotThrow(() -> {
            shell.parseAndEvaluate("mv folder2 folder1", stdout);
        });

        // folder2 is moved to folder1/folder2
        assertTrue(Files.notExists(folder2));
        assertTrue(Files.exists(destPath));

        // folder2/file1.txt is also moved to folder1/folder2
        assertTrue(Files.exists(destPath.resolve(FILE_1)));
    }

    @Test
    @DisplayName("Bug #11 from team 16")
    void parseAndEvaluate_MvMultipleFilesWithInvalidFiles_ValidFilesPrecedingInvalidFilesMoved() {
        Throwable exception = assertThrows(MvException.class, () -> shell.parseAndEvaluate("mv * folder1", stdout));

        assertEquals(new MvException(constructRenameErrorMsg("folder1", "folder1/folder1", ERR_INVALID_ARGS)).getMessage(),
                exception.getMessage());

        // file1.txt moved to folder1
        assertTrue(Files.notExists(file1));
        assertTrue(Files.exists(folder1.resolve(FILE_1)));

        // file2.txt moved to folder1
        assertTrue(Files.notExists(file2));
        assertTrue(Files.exists(folder1.resolve(FILE_2)));

        // folder2 not moved into folder1 (as it is after folder1 after globbing)
        assertTrue(Files.exists(folder2));
        assertTrue(Files.notExists(folder1.resolve(FOLDER_2)));
    }

    @Test
    @DisplayName("Bug #13 from team 16")
    void parseAndEvaluate_RmNonEmptyDirWithEmptyDirFlag_ThrowsException() throws IOException {
        // Create a file under folder1
        Files.createFile(folder1.resolve(FILE_1));

        Throwable exception = assertThrows(RmException.class, () -> shell.parseAndEvaluate("rm -d folder1", stdout));

        assertEquals(new RmException(new InvalidDirectoryException(FOLDER_1, ERR_DIR_NOT_EMPTY).getMessage()).getMessage(),
                exception.getMessage());

        // folder1 not removed
        assertTrue(Files.exists(folder1));
    }

    @Test
    @DisplayName("Bug #14 from team 16")
    void parseAndEvaluate_RmMultipleFilesWithInvalidFiles_ValidFilesPrecedingInvalidFilesRemoved() {
        Throwable exception = assertThrows(RmException.class, () -> shell.parseAndEvaluate("rm file1.txt folder1 file2.txt", stdout));
        assertEquals(new RmException(new InvalidDirectoryException(FOLDER_1, ERR_IS_DIR).getMessage()).getMessage(), exception.getMessage());

        // folder1 remains as it is a directory
        assertTrue(Files.exists(folder1));

        // file1 removed as it is before the invalid file (folder 1)
        assertTrue(Files.notExists(file1));

        // file2 remains as it is after the invalid file (folder 1)
        assertTrue(Files.exists(file2));
    }

    @Test
    @DisplayName("Bug #3 from team 18")
    void parseAndEvaluate_Echo_CommandExecuted() {
        assertDoesNotThrow(() -> {
            shell.parseAndEvaluate("echo abc;", stdout);
        });

        assertEquals("abc" + STRING_NEWLINE, stdout.toString());
    }

    @Test
    @DisplayName("Bug #4 from team 16, bug #6 from team 18")
    void parseAndEvaluate_CpSrcFolderToDestFolderWithoutRecursiveFlag_ThrowsException() {
        assertThrows(CpException.class, () -> {
            shell.parseAndEvaluate("cp folder1 folder2;", stdout);
        });
    }
}
