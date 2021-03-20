package tdd.ef1.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_REGEX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_PARENT_DIR;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.RegexArgument;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

@Disabled
class RegexArgumentTest {

    private static final String X_AND_X = "x_and_x";
    private static final String Y_AND_X = "y_and_x";
    private static final String Z_AND_X = "z_and_x";
    private static final String X_OR_X = "x_or_x";
    private static final String Y_OR_X = "y_or_x";
    private static final String Z_OR_X = "z_or_x";
    private static final String X_AND_Y = "x_and_y";
    private static final String Y_AND_Y = "y_and_y";
    private static final String Z_AND_Y = "z_and_y";
    private static final String X_OR_Y = "x_or_y";
    private static final String Y_OR_Y = "y_or_y";
    private static final String Z_OR_Y = "z_or_y";
    private static final String X_AND_Z = "x_and_z";
    private static final String Y_AND_Z = "y_and_z";
    private static final String Z_AND_Z = "z_and_z";
    private static final String X_OR_Z = "x_or_z";
    private static final String Y_OR_Z = "y_or_z";
    private static final String Z_OR_Z = "z_or_z";
    private static final String FOLDER_NAME = "folder";
    private static final String SUBFOLDER_NAME = "subfolder";
    private static final String[] ASTERISK_FILES = new String[]{
            X_AND_X, Y_AND_X, Z_AND_X,
            X_OR_X, Y_OR_X, Z_OR_X,
            X_AND_Y, Y_AND_Y, Z_AND_Y,
            X_OR_Y, Y_OR_Y, Z_OR_Y,
            X_AND_Z, Y_AND_Z, Z_AND_Z,
            X_OR_Z, Y_OR_Z, Z_OR_Z,
            FOLDER_NAME
    };
    private static final String[] X_ASTERISK_FILES = new String[]{
            X_AND_X, X_OR_X, X_AND_Y, X_OR_Y, X_AND_Z, X_OR_Z,
    };
    private static final String[] ASTERISK_X_FILES = new String[]{
            X_AND_X, Y_AND_X, Z_AND_X, X_OR_X, Y_OR_X, Z_OR_X
    };
    private static final String[] X_ASTERISK_X_FILES = new String[]{
            X_AND_X, X_OR_X
    };
    private static final String[] ASTERISK_AND_ASTERISK_FILES = new String[]{
            X_AND_X, Y_AND_X, Z_AND_X,
            X_AND_Y, Y_AND_Y, Z_AND_Y,
            X_AND_Z, Y_AND_Z, Z_AND_Z,
    };
    @TempDir
    static File tempDir;
    private RegexArgument regexArgument;

    @BeforeAll
    static void setupAll() throws IOException {
        new File(tempDir, FOLDER_NAME).mkdir();
        new File(tempDir, FOLDER_NAME + STRING_FILE_SEP + SUBFOLDER_NAME).mkdir();
        for (String filename : ASTERISK_FILES) {
            new File(tempDir, filename).createNewFile();
            new File(tempDir, FOLDER_NAME + STRING_FILE_SEP + filename).createNewFile();
            new File(tempDir, FOLDER_NAME + STRING_FILE_SEP + SUBFOLDER_NAME + STRING_FILE_SEP + filename).createNewFile();
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        EnvironmentUtil.setCurrentDirectory(tempDir.getAbsolutePath());
        regexArgument = new RegexArgument();
    }

    // *
    @Test
    public void globFiles_asterisk_returnsAll() throws ShellException {
        regexArgument.appendAsterisk();
        List<String> fileList = regexArgument.globFiles();
        assertEquals(ASTERISK_FILES.length, fileList.size());
        for (String filename : ASTERISK_FILES) {
            assertTrue(fileList.contains(filename));
        }
    }

    // x_*
    @Test
    public void globFiles_StringAsterisk_ReturnsAllStartingWithString() throws ShellException {
        regexArgument.append('x');
        regexArgument.append('_');
        regexArgument.appendAsterisk();
        List<String> fileList = regexArgument.globFiles();
        assertEquals(X_ASTERISK_FILES.length, fileList.size());
        for (String filename : X_ASTERISK_FILES) {
            assertTrue(fileList.contains(filename));
        }
    }

    // *_x
    @Test
    public void globFiles_AsteriskString_ReturnsAllEndingWithString() throws ShellException {
        regexArgument.appendAsterisk();
        regexArgument.append('_');
        regexArgument.append('x');
        List<String> fileList = regexArgument.globFiles();
        assertEquals(ASTERISK_X_FILES.length, fileList.size());
        for (String filename : ASTERISK_X_FILES) {
            assertTrue(fileList.contains(filename));
        }
    }

    // x_*_x
    @Test
    public void globFiles_StringAsteriskString_ReturnsAllWrappedBetweenString() throws ShellException {
        regexArgument.append('x');
        regexArgument.append('_');
        regexArgument.appendAsterisk();
        regexArgument.append('_');
        regexArgument.append('x');
        List<String> fileList = regexArgument.globFiles();
        assertEquals(X_ASTERISK_X_FILES.length, fileList.size());
        for (String filename : X_ASTERISK_X_FILES) {
            assertTrue(fileList.contains(filename));
        }
    }

    // *_and_*
    @Test
    public void globFiles_AsteriskStringAsterisk_ReturnsAllContainingString() throws ShellException {
        regexArgument.appendAsterisk();
        for (char c : "_and_".toCharArray()) {
            regexArgument.append(c);
        }
        regexArgument.appendAsterisk();
        List<String> fileList = regexArgument.globFiles();
        assertEquals(ASTERISK_AND_ASTERISK_FILES.length, fileList.size());
        for (String filename : ASTERISK_AND_ASTERISK_FILES) {
            assertTrue(fileList.contains(filename));
        }
    }

    // folder/x_*
    @Test
    public void globFiles_FolderStringAsterisk_ReturnsAll() throws ShellException {
        for (char c : (FOLDER_NAME + STRING_FILE_SEP + "x_").toCharArray()) {
            regexArgument.append(c);
        }
        regexArgument.appendAsterisk();
        List<String> fileList = regexArgument.globFiles();
        assertEquals(X_ASTERISK_FILES.length, fileList.size());
        for (String filename : X_ASTERISK_FILES) {
            assertTrue(fileList.contains(FOLDER_NAME + STRING_FILE_SEP + filename));
        }
    }

    // folder/x_*/
    @Test
    public void globFiles_FolderStringAsteriskWithEndingSlash_ReturnsAll() throws ShellException {
        for (char c : (FOLDER_NAME + STRING_FILE_SEP + "x_").toCharArray()) {
            regexArgument.append(c);
        }
        regexArgument.appendAsterisk();
        regexArgument.append(CHAR_FILE_SEP);
        List<String> fileList = regexArgument.globFiles();
        assertEquals(X_ASTERISK_FILES.length, fileList.size());
        for (String filename : X_ASTERISK_FILES) {
            assertTrue(fileList.contains(FOLDER_NAME + STRING_FILE_SEP + filename));
        }
    }

    // folder/subfolder/*_x
    @Test
    public void globFiles_SubfolderAsteriskString_ReturnsAll() throws ShellException {
        for (char c : (FOLDER_NAME + STRING_FILE_SEP + SUBFOLDER_NAME + STRING_FILE_SEP).toCharArray()) {
            regexArgument.append(c);
        }
        regexArgument.appendAsterisk();
        regexArgument.append('_');
        regexArgument.append('x');
        List<String> fileList = regexArgument.globFiles();
        assertEquals(ASTERISK_X_FILES.length, fileList.size());
        for (String filename : ASTERISK_X_FILES) {
            assertTrue(fileList.contains(FOLDER_NAME + STRING_FILE_SEP + SUBFOLDER_NAME + STRING_FILE_SEP + filename));
        }
    }

    // ../*_x
    @Test
    public void globFiles_UpOneLevelStringAsterisk_ReturnsAll() throws Exception {
        EnvironmentUtil.setCurrentDirectory(tempDir.getAbsolutePath() + STRING_FILE_SEP + FOLDER_NAME);
        for (char c : (STRING_PARENT_DIR + STRING_FILE_SEP).toCharArray()) {
            regexArgument.append(c);
        }
        regexArgument.appendAsterisk();
        regexArgument.append('_');
        regexArgument.append('x');
        List<String> fileList = regexArgument.globFiles();
        assertEquals(ASTERISK_X_FILES.length, fileList.size());
        for (String filename : ASTERISK_X_FILES) {
            assertTrue(fileList.contains(STRING_PARENT_DIR + STRING_FILE_SEP + filename));
        }
    }

    // ../../x_*
    @Test
    public void globFiles_UpTwoLevelStringAsterisk_ReturnsAll() throws Exception {
        EnvironmentUtil.setCurrentDirectory(tempDir.getAbsolutePath() + STRING_FILE_SEP + FOLDER_NAME + StringUtils.STRING_FILE_SEP + SUBFOLDER_NAME);
        for (char c : (STRING_PARENT_DIR + STRING_FILE_SEP + STRING_PARENT_DIR + STRING_FILE_SEP + "x_").toCharArray()) {
            regexArgument.append(c);
        }
        regexArgument.appendAsterisk();
        List<String> fileList = regexArgument.globFiles();
        assertEquals(X_ASTERISK_FILES.length, fileList.size());
        for (String filename : X_ASTERISK_FILES) {
            assertTrue(fileList.contains(STRING_PARENT_DIR + STRING_FILE_SEP + STRING_PARENT_DIR + STRING_FILE_SEP + filename));
        }
    }

    // folder/*/x_and_x
    @Test
    public void globFile_AsteriskInNonTerminal_ThrowsException() {
        for (char c : (FOLDER_NAME + StringUtils.STRING_FILE_SEP).toCharArray()) {
            regexArgument.append(c);
        }
        regexArgument.appendAsterisk();
        for (char c : (StringUtils.STRING_FILE_SEP + X_AND_X).toCharArray()) {
            regexArgument.append(c);
        }
        Exception expectedException = assertThrows(ShellException.class, () -> {
            regexArgument.globFiles();
        });
        assertEquals(new ShellException(ERR_INVALID_REGEX).getMessage(), expectedException.getMessage());
    }
}
