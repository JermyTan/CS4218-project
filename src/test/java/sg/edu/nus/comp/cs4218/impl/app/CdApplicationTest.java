package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_CD;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;

class CdApplicationTest {

    private static final String ORIGINAL_DIR = Environment.currentDirectory;
    private static final String TEST_DIR = Environment.currentDirectory + File.separator + RESOURCES_PATH + File.separator + "CdApplicationTest";

    private static final String FOLDER_1 = "folder1"; // exists
    private static final String FOLDER_2 = "folder2"; // does not exist
    private static final String FOLDER_3 = "folder3" + File.separator + "folder4";
    private static final String FILE = "file.txt";

    private static final String[] ARGS_1 = {FOLDER_1};
    private static final String[] ARGS_2 = {};
    private static final String[] ARGS_3 = {FOLDER_1, FOLDER_3};

    private CdApplication app;

    @BeforeAll
    static void setupBeforeAll() {
        Environment.currentDirectory = TEST_DIR;
    }

    @AfterAll
    static void tearDownAfterAll() {
        Environment.currentDirectory = ORIGINAL_DIR;
    }

    @BeforeEach
    void setUp() {
        app = new CdApplication();
    }

    @AfterEach
    void tearDown() {
        Environment.currentDirectory = TEST_DIR;
    }

    @Test
    void run_ExistingFolder_ShouldReturn() {
        String oldDir = Environment.currentDirectory;
        assertDoesNotThrow(() -> app.run(ARGS_1, System.in, System.out));
        String newDir = Environment.currentDirectory;

        assertEquals(oldDir + File.separator + FOLDER_1, newDir);
    }

    @Test
    public void run_NullArgs_ShouldThrow() {
        Throwable exception = assertThrows(CdException.class, () -> app.run(null, System.in, System.out));
        assertEquals(String.format(STRING_LABEL_VALUE_PAIR, APP_CD, ERR_NULL_ARGS), exception.getMessage());
    }

    @Test
    void run_MissingArgs_ShouldThrow() {
        Throwable exception = assertThrows(CdException.class, () ->app.run(ARGS_2, System.in, System.out));
        assertEquals(String.format(STRING_LABEL_VALUE_PAIR, APP_CD, ERR_MISSING_ARG), exception.getMessage());
    }

    @Test
    void run_MoreThanOneArgs_ShouldThrow() {
        Throwable exception = assertThrows(CdException.class, () ->app.run(ARGS_3, System.in, System.out));
        assertEquals(String.format(STRING_LABEL_VALUE_PAIR, APP_CD, ERR_TOO_MANY_ARGS), exception.getMessage());
    }

    @Test
    void changeToDirectory_ExistingFolder_ShouldReturn() {
        String oldDir = Environment.currentDirectory;
        assertDoesNotThrow(() -> app.changeToDirectory(FOLDER_1));
        String newDir = Environment.currentDirectory;
        assertEquals(oldDir + File.separator + FOLDER_1, newDir);
    }

    @Test
    void changeToDirectory_MultiLevelFolder_ShouldReturn() {
        String oldDir = Environment.currentDirectory;
        assertDoesNotThrow(() -> app.changeToDirectory(FOLDER_3));
        String newDir = Environment.currentDirectory;
        assertEquals(oldDir + File.separator + FOLDER_3, newDir);
    }

    @Test
    void changeToDirectory_NonExistingFolder_ShouldThrow() {
        Throwable exception = assertThrows(CdException.class, () -> app.changeToDirectory(FOLDER_2));
        assertEquals(String.format("%s: %s: %s", APP_CD, FOLDER_2, ERR_FILE_NOT_FOUND), exception.getMessage());
    }

    @Test
    void changeToDirectory_NotADirectory_ShouldThrow() {
        Throwable exception = assertThrows(CdException.class, () -> app.changeToDirectory(FILE));
        assertEquals(String.format("%s: %s: %s", APP_CD, FILE, ERR_IS_NOT_DIR), exception.getMessage());
    }

    @Test
    void changeToDirectory_NoArgs_ShouldThrow() {
        Throwable exception = assertThrows(CdException.class, () -> app.changeToDirectory(null));
        assertEquals(String.format(STRING_LABEL_VALUE_PAIR, APP_CD, ERR_MISSING_ARG), exception.getMessage());
    }
}