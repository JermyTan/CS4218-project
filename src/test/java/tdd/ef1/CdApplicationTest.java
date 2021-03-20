package tdd.ef1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_NOT_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_MISSING_ARG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOO_MANY_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_PARENT_DIR;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.impl.app.CdApplication;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

class CdApplicationTest {

    static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    static final String FOLDER = "folder";
    static final String SUBFOLDER = "folder" + StringUtils.STRING_FILE_SEP + "subfolder";
    static final String BLOCKED_FOLDER = "blocked";
    static final String VALID_FILE = "file.txt";
    @TempDir
    static File tempDir;
    private static CdApplication cdApplication;

    @BeforeAll
    static void setupAll() throws IOException {
        new File(tempDir, FOLDER).mkdir();
        new File(tempDir, SUBFOLDER).mkdir();
        new File(tempDir, VALID_FILE).createNewFile();
        File blockedFolder = new File(tempDir, BLOCKED_FOLDER);
        blockedFolder.mkdir();
        blockedFolder.setExecutable(false);
    }

    @BeforeEach
    void setUp() throws Exception {
        cdApplication = new CdApplication();
        EnvironmentUtil.setCurrentDirectory(tempDir.getAbsolutePath());
    }

    @AfterEach
    void tearDown() throws Exception {
        EnvironmentUtil.setCurrentDirectory(ORIGINAL_DIR);
    }

    // Cd into valid relative path
    @Test
    public void run_CdIntoValidRelativePath_Success() throws CdException {
        String finalPath = tempDir.getAbsolutePath() + StringUtils.STRING_FILE_SEP + FOLDER;
        String[] argList = new String[]{FOLDER};
        cdApplication.run(argList, System.in, System.out);
        String currDirectory = EnvironmentUtil.currentDirectory;
        assertEquals(finalPath, currDirectory);
    }

    // Cd with blank arg
    @Test
    public void run_CdIntoBlankPath_NoChangeToCurrDirectory() throws CdException {
        String[] argList = new String[]{STRING_EMPTY};
        cdApplication.run(argList, System.in, System.out);
        assertEquals(EnvironmentUtil.currentDirectory, tempDir.getAbsolutePath());
    }


    @Test
    public void run_CdIntoValidPathNullStreams_Success() throws CdException {
        String finalPath = tempDir.getAbsolutePath() + StringUtils.STRING_FILE_SEP + FOLDER;
        String[] argList = new String[]{FOLDER};
        cdApplication.run(argList, null, null);
        String currDirectory = EnvironmentUtil.currentDirectory;
        assertEquals(finalPath, currDirectory);
    }

    @Test
    public void run_CdIntoNestedFolder_Success() throws CdException {
        String finalPath = tempDir.getAbsolutePath() + StringUtils.STRING_FILE_SEP + SUBFOLDER;
        String[] argList = new String[]{SUBFOLDER};
        cdApplication.run(argList, System.in, System.out);
        String currDirectory = EnvironmentUtil.currentDirectory;
        assertEquals(finalPath, currDirectory);
    }

    @Test
    public void run_CdOutFromFolder_Success() throws Exception {
        String relativePath = tempDir.getAbsolutePath() + StringUtils.STRING_FILE_SEP + FOLDER;
        EnvironmentUtil.setCurrentDirectory(relativePath);
        String[] argList = new String[]{STRING_PARENT_DIR + STRING_FILE_SEP};
        cdApplication.run(argList, System.in, System.out);
        String currDirectory = EnvironmentUtil.currentDirectory;
        assertEquals(tempDir.getAbsolutePath(), currDirectory);
    }

    @Test
    public void run_CdOutFromNestedFolder_Success() throws Exception {
        String relativePath = tempDir.getAbsolutePath() + StringUtils.STRING_FILE_SEP + SUBFOLDER;
        EnvironmentUtil.setCurrentDirectory(relativePath);
        String[] argList = new String[]{STRING_PARENT_DIR + STRING_FILE_SEP + STRING_PARENT_DIR + STRING_FILE_SEP};
        cdApplication.run(argList, System.in, System.out);
        String currDirectory = EnvironmentUtil.currentDirectory;
        assertEquals(tempDir.getAbsolutePath(), currDirectory);
    }

    // Cd into invalid relative path
    @Test
    public void run_InvalidRelativePath_ThrowsException() {
        String[] argList = new String[]{"invalid"};
        Exception expectedException = assertThrows(CdException.class, () -> {
            cdApplication.run(argList, System.in, System.out);
        });
        assertEquals(
                new CdException(new InvalidDirectoryException("invalid", ERR_FILE_NOT_FOUND).getMessage()).getMessage(),
                expectedException.getMessage());
    }

    // Cd into valid absolute path
    @Test
    public void run_CdIntoValidAbsolutePath_Success() throws CdException {
        String absolutePath = tempDir.getAbsolutePath() + StringUtils.STRING_FILE_SEP + FOLDER;
        String[] argList = new String[]{absolutePath};
        cdApplication.run(argList, System.in, System.out);
        String currDirectory = EnvironmentUtil.currentDirectory;
        assertEquals(absolutePath, currDirectory);
    }

    // Cd into invalid absolute path
    @Test
    public void run_CdIntoInvalidAbsolutePath_ThrowsException() throws CdException {
        String absolutePath = tempDir.getAbsolutePath() + StringUtils.STRING_FILE_SEP + "invalid";
        String[] argList = new String[]{absolutePath};
        Exception expectedException = assertThrows(CdException.class, () -> {
            cdApplication.run(argList, System.in, System.out);
        });
        assertEquals(
                new CdException(new InvalidDirectoryException(absolutePath, ERR_FILE_NOT_FOUND).getMessage()).getMessage(),
                expectedException.getMessage());
    }

    // Cd into non directory
    @Test
    public void run_CdIntoFile_ThrowsException() {
        String[] argList = new String[]{VALID_FILE};
        Exception expectedException = assertThrows(CdException.class, () -> {
            cdApplication.run(argList, System.in, System.out);
        });
        assertEquals(
                new CdException(new InvalidDirectoryException(VALID_FILE, ERR_IS_NOT_DIR).getMessage()).getMessage(),
                expectedException.getMessage());
    }

    // Cd into folder with no permissions
    @Test
    // @DisabledOnOs(WINDOWS)
    public void run_BlockedFolder_ThrowsExeception() {
        String[] argList = new String[]{BLOCKED_FOLDER};
        Exception expectedException = assertThrows(CdException.class, () -> {
            cdApplication.run(argList, System.in, System.out);
        });
        assertEquals(
                new CdException(new InvalidDirectoryException(BLOCKED_FOLDER, ERR_NO_PERM).getMessage()).getMessage(),
                expectedException.getMessage()
        );
    }

    // Cd with too many args
    @Test
    public void run_CdWithManyArgs_ThrowsException() {
        String[] argList = new String[]{FOLDER, SUBFOLDER};
        Exception expectedException = assertThrows(CdException.class, () -> {
            cdApplication.run(argList, System.in, System.out);
        });
        assertEquals(new CdException(ERR_TOO_MANY_ARGS).getMessage(), expectedException.getMessage());
    }

    // Cd with no args
    @Test
    public void run_CdWithNoArgs_ThrowsException() {
        String[] argList = new String[]{};
        Exception expectedException = assertThrows(CdException.class, () -> {
            cdApplication.run(argList, System.in, System.out);
        });
        assertEquals(new CdException(ERR_MISSING_ARG).getMessage(), expectedException.getMessage());
    }

    // Cd with null args
    @Test
    public void run_CdWithNullArgs_ThrowsException() {
        Exception expectedException = assertThrows(CdException.class, () -> {
            cdApplication.run(null, System.in, System.out);
        });
        assertEquals(new CdException(ERR_NULL_ARGS).getMessage(), expectedException.getMessage());
    }

    // Cd with null streams
    @Test
    public void run_CdWithoutArgsNullStreams_ThrowsException() {
        String[] argList = new String[]{};

        Exception expectedException1 = assertThrows(CdException.class, () -> {
            cdApplication.run(argList, null, System.out);
        });
        assertEquals(new CdException(ERR_MISSING_ARG).getMessage(), expectedException1.getMessage());

        Exception expectedException2 = assertThrows(CdException.class, () -> {
            cdApplication.run(argList, System.in, null);
        });
        assertEquals(new CdException(ERR_MISSING_ARG).getMessage(), expectedException2.getMessage());

        Exception expectedException3 = assertThrows(CdException.class, () -> {
            cdApplication.run(argList, null, null);
        });
        assertEquals(new CdException(ERR_MISSING_ARG).getMessage(), expectedException3.getMessage());
    }
}
