package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

class CdApplicationTest {

    private static final String FOLDER_1 = "folder1"; // exists
    private static final String FOLDER_2 = "folder2"; // does not exist
    private static final String FOLDER_3 = "folder3" + File.separator + "folder4";
    private static final String FILE = "file.txt";

    private static final String[] ARGS_1 = {FOLDER_1};
    private static final String[] ARGS_2 = {};
    private static final String[] ARGS_3 = {FOLDER_1, FOLDER_3};

    private final Path folder1 = Paths.get(FOLDER_1);
    private final Path folder2 = Paths.get(FOLDER_2);
    private final Path folder3 = Paths.get(FOLDER_3);
    private final Path file = Paths.get(FILE);

    private final List<Path> paths = List.of(folder1, folder2, folder3, file);
    private final String defaultDir = Environment.currentDirectory;

    private CdApplication app;

    @BeforeEach
    void setUp() {
        app = new CdApplication();

        try {

            Files.createDirectory(folder1);
            Files.createDirectories(folder3);
            Files.createFile(file);

        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        Environment.currentDirectory = defaultDir;

        try {
            for (Path path : paths) {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void run_ExistingFolder_ShouldReturn() throws CdException {
        String oldDir = Environment.currentDirectory;
        app.run(ARGS_1, System.in, System.out);
        String newDir = Environment.currentDirectory;

        assertEquals(oldDir + File.separator + FOLDER_1, newDir);
    }

    @Test
    void run_MoreThanOneArgs_ShouldThrow() {
        Throwable error = assertThrows(CdException.class, () ->app.run(ARGS_3, System.in, System.out));

        assertEquals("cd: " + ERR_TOO_MANY_ARGS, error.getMessage());
    }

    @Test
    void run_MissingArgs_ShouldThrow() {
        Throwable error = assertThrows(CdException.class, () ->app.run(ARGS_2, System.in, System.out));

        assertEquals("cd: " + ERR_MISSING_ARG, error.getMessage());
    }

    @Test
    void changeToDirectory_ExistingFolder_ShouldReturn() throws CdException {
        String oldDir = Environment.currentDirectory;
        app.changeToDirectory(FOLDER_1);
        String newDir = Environment.currentDirectory;
        assertEquals(oldDir + File.separator + FOLDER_1, newDir);
    }

    @Test
    void changeToDirectory_MultiLevelFolder_ShouldReturn() throws CdException {
        String oldDir = Environment.currentDirectory;
        app.changeToDirectory(FOLDER_3);
        String newDir = Environment.currentDirectory;
        assertEquals(oldDir + File.separator + FOLDER_3, newDir);
    }

    @Test
    void changeToDirectory_NonExistingFolder_ShouldThrow() {
        Throwable error = assertThrows(CdException.class, () -> app.changeToDirectory(FOLDER_2));
        assertEquals("cd: " + FOLDER_2 + ": " + ERR_FILE_NOT_FOUND, error.getMessage());
    }

    @Test
    void changeToDirectory_NotADirectory_ShouldThrow() {
        Throwable error = assertThrows(CdException.class, () -> app.changeToDirectory(FILE));
        assertEquals("cd: " + FILE + ": " + ERR_IS_NOT_DIR, error.getMessage());
    }

    @Test
    void changeToDirectory_NoArgs_ShouldThrow() {
        Throwable error = assertThrows(CdException.class, () -> app.changeToDirectory(null));
        assertEquals("cd: " + ERR_MISSING_ARG, error.getMessage());
    }
}