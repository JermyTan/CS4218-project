package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_CD;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_NOT_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_MISSING_ARG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_FILE_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOO_MANY_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.CdException;

class CdApplicationTest {

    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + RESOURCES_PATH + STRING_FILE_SEP + "CdApplicationTest";

    private static final String FOLDER_1 = "folder1"; // exists
    private static final String FOLDER_2 = "folder2"; // does not exist
    private static final String FOLDER_3 = "folder3" + STRING_FILE_SEP + "folder4";
    private static final String FILE = "file.txt";

    private static final String[] ARGS_1 = {FOLDER_1};
    private static final String[] ARGS_2 = {};
    private static final String[] ARGS_3 = {FOLDER_1, FOLDER_3};

    private CdApplication app;

    @BeforeAll
    static void setupBeforeAll() {
        EnvironmentUtil.currentDirectory = TEST_DIR;
    }

    @AfterAll
    static void tearDownAfterAll() {
        EnvironmentUtil.currentDirectory = ORIGINAL_DIR;
    }

    @BeforeEach
    void setUp() {
        app = new CdApplication();
    }

    @AfterEach
    void tearDown() {
        EnvironmentUtil.currentDirectory = TEST_DIR;
    }

    @Test
    void run_ExistingFolder_ShouldReturn() {
        String oldDir = EnvironmentUtil.currentDirectory;
        assertDoesNotThrow(() -> app.run(ARGS_1, System.in, System.out));
        String newDir = EnvironmentUtil.currentDirectory;

        assertEquals(oldDir + STRING_FILE_SEP + FOLDER_1, newDir);
    }

    @Test
    public void run_NullArgs_ThrowsException() {
        Throwable exception = assertThrows(CdException.class, () -> app.run(null, System.in, System.out));
        assertEquals(String.format(STRING_LABEL_VALUE_PAIR, APP_CD, ERR_NULL_ARGS), exception.getMessage());
    }

    @Test
    void run_MissingArgs_ThrowsException() {
        Throwable exception = assertThrows(CdException.class, () -> app.run(ARGS_2, System.in, System.out));
        assertEquals(String.format(STRING_LABEL_VALUE_PAIR, APP_CD, ERR_MISSING_ARG), exception.getMessage());
    }

    @Test
    void run_MoreThanOneArgs_ThrowsException() {
        Throwable exception = assertThrows(CdException.class, () -> app.run(ARGS_3, System.in, System.out));
        assertEquals(String.format(STRING_LABEL_VALUE_PAIR, APP_CD, ERR_TOO_MANY_ARGS), exception.getMessage());
    }

    @Test
    void changeToDirectory_ExistingFolder_ShouldReturn() {
        String oldDir = EnvironmentUtil.currentDirectory;
        assertDoesNotThrow(() -> app.changeToDirectory(FOLDER_1));
        String newDir = EnvironmentUtil.currentDirectory;
        assertEquals(oldDir + STRING_FILE_SEP + FOLDER_1, newDir);
    }

    @Test
    void changeToDirectory_MultiLevelFolder_ShouldReturn() {
        String oldDir = EnvironmentUtil.currentDirectory;
        assertDoesNotThrow(() -> app.changeToDirectory(FOLDER_3));
        String newDir = EnvironmentUtil.currentDirectory;
        assertEquals(oldDir + STRING_FILE_SEP + FOLDER_3, newDir);
    }

    @Test
    void changeToDirectory_NonExistingFolder_ThrowsException() {
        Throwable exception = assertThrows(CdException.class, () -> app.changeToDirectory(FOLDER_2));
        assertEquals(String.format("%s: %s: %s", APP_CD, FOLDER_2, ERR_FILE_NOT_FOUND), exception.getMessage());
    }

    @Test
    void changeToDirectory_NotADirectory_ThrowsException() {
        Throwable exception = assertThrows(CdException.class, () -> app.changeToDirectory(FILE));
        assertEquals(String.format("%s: %s: %s", APP_CD, FILE, ERR_IS_NOT_DIR), exception.getMessage());
    }

    @Test
    void changeToDirectory_NoArgs_ThrowsException() {
        Throwable exception = assertThrows(CdException.class, () -> app.changeToDirectory(null));
        assertEquals(String.format(STRING_LABEL_VALUE_PAIR, APP_CD, ERR_NO_FILE_ARGS), exception.getMessage());
    }
}