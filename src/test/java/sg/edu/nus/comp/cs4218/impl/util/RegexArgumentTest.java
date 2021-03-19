package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_BLANK;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_MULTI_WORDS;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_SINGLE_WORD;

import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;

class RegexArgumentTest {

    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + RESOURCES_PATH + STRING_FILE_SEP + "RegexArgumentTest";

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";
    private static final String FILE_3 = "test.md";

    private static final String FOLDER_1 = "folder1";
    private static final String FOLDER_2 = "folder2";
    private static final String FOLDER_3 = "testFolder";

    private RegexArgument regexArgument;

    @BeforeAll
    static void setupBeforeAll() {
        EnvironmentUtil.currentDirectory = TEST_DIR;
    }

    @AfterAll
    static void tearDownAfterAll() {
        EnvironmentUtil.currentDirectory = ORIGINAL_DIR;
    }

    private String resolveArg(String arg) {
        return Paths.get(TEST_DIR, arg).toString();
    }

    @BeforeEach
    void setup() {
        regexArgument = new RegexArgument();
    }

    @AfterEach
    void tearDown() {
        regexArgument = null;
    }

    @Test
    public void globFiles_NoMatchedFileOrFolder_ArgUnchanged() {
        // x*
        regexArgument.append('x');
        regexArgument.appendAsterisk();

        List<String> globbedFiles = regexArgument.globFiles();

        assertEquals(1, globbedFiles.size());
        assertEquals(regexArgument.toString(), globbedFiles.get(0));
    }

    @Test
    public void globFiles_NoMatchedFolder_ArgUnchanged() {
        // file1*/
        regexArgument.merge("file1");
        regexArgument.appendAsterisk();
        regexArgument.merge(STRING_FILE_SEP);

        List<String> globbedFiles = regexArgument.globFiles();

        assertEquals(1, globbedFiles.size());
        assertEquals(regexArgument.toString(), globbedFiles.get(0));
    }

    @Test
    public void globFiles_OneMatch_ReturnsGlobbedFile() {
        // *.md
        regexArgument.appendAsterisk();
        regexArgument.merge(".md");

        List<String> globbedFiles = regexArgument.globFiles();

        assertEquals(1, globbedFiles.size());
        assertEquals(resolveArg(FILE_3), globbedFiles.get(0));
    }

    @Test
    public void globFiles_MoreThanOneMatch_ReturnsGlobbedFilesSorted() {
        // *.txt
        regexArgument.appendAsterisk();
        regexArgument.merge(".txt");

        List<String> globbedFiles = regexArgument.globFiles();

        assertEquals(2, globbedFiles.size());
        assertEquals(resolveArg(FILE_1), globbedFiles.get(0));
        assertEquals(resolveArg(FILE_2), globbedFiles.get(1));
    }

    @Test
    public void globFiles_EndWithSlash_MatchFoldersOnly() {
        // f*/
        regexArgument.merge("f");
        regexArgument.appendAsterisk();
        regexArgument.merge(STRING_FILE_SEP);

        List<String> globbedFiles = regexArgument.globFiles();

        assertEquals(2, globbedFiles.size());
        assertEquals(resolveArg(FOLDER_1), globbedFiles.get(0));
        assertEquals(resolveArg(FOLDER_2), globbedFiles.get(1));
    }

    @Test
    public void globFiles_DoesNotEndWithSlash_MatchFilesAndFolders() {
        // f*
        regexArgument.merge("f");
        regexArgument.appendAsterisk();

        List<String> globbedFiles = regexArgument.globFiles();

        assertEquals(4, globbedFiles.size());
        assertEquals(resolveArg(FILE_1), globbedFiles.get(0));
        assertEquals(resolveArg(FILE_2), globbedFiles.get(1));
        assertEquals(resolveArg(FOLDER_1), globbedFiles.get(2));
        assertEquals(resolveArg(FOLDER_2), globbedFiles.get(3));
    }

    @Test
    public void globFiles_AsteriskSlash_MatchAllFolders() {
        // */
        regexArgument.appendAsterisk();
        regexArgument.merge(STRING_FILE_SEP);

        List<String> globbedFiles = regexArgument.globFiles();

        assertEquals(3, globbedFiles.size());
        assertEquals(resolveArg(FOLDER_1), globbedFiles.get(0));
        assertEquals(resolveArg(FOLDER_2), globbedFiles.get(1));
        assertEquals(resolveArg(FOLDER_3), globbedFiles.get(2));
    }

    @Test
    public void globFiles_AbsolutePath_ReturnsGlobbedFilesSorted() {
        regexArgument.merge(EnvironmentUtil.currentDirectory);
        regexArgument.merge(STRING_FILE_SEP);
        regexArgument.merge("file");
        regexArgument.appendAsterisk();

        List<String> globbedFiles = regexArgument.globFiles();

        assertEquals(2, globbedFiles.size());
        assertEquals(resolveArg(FILE_1), globbedFiles.get(0));
        assertEquals(resolveArg(FILE_2), globbedFiles.get(1));
    }

    @Test
    void globFiles_ConsecutiveAsterisks_SameAsSingleAsterisk() {
        regexArgument.merge("f");
        regexArgument.appendAsterisk();
        regexArgument.appendAsterisk();

        List<String> globbedFiles = regexArgument.globFiles();

        assertEquals(4, globbedFiles.size());
        assertEquals(resolveArg(FILE_1), globbedFiles.get(0));
        assertEquals(resolveArg(FILE_2), globbedFiles.get(1));
        assertEquals(resolveArg(FOLDER_1), globbedFiles.get(2));
        assertEquals(resolveArg(FOLDER_2), globbedFiles.get(3));
    }

    @Test
    void globFiles_MultipleAsterisks_ReturnsGlobbedFilesSorted() {
        regexArgument.appendAsterisk();
        regexArgument.merge("test");
        regexArgument.appendAsterisk();

        List<String> globbedFiles = regexArgument.globFiles();

        assertEquals(2, globbedFiles.size());
        assertEquals(resolveArg(FILE_3), globbedFiles.get(0));
        assertEquals(resolveArg(FOLDER_3), globbedFiles.get(1));
    }

    @Test
    void append_EmptyRegexArg_CharAppendedRegexArg() {
        regexArgument.append('a');
        assertEquals("a", regexArgument.toString());
    }

    @Test
    void append_NonEmptyRegexArg_CharAppendedRegexArg() {
        regexArgument = new RegexArgument(STRING_SINGLE_WORD);
        regexArgument.append('a');
        assertEquals(STRING_SINGLE_WORD + "a", regexArgument.toString());
    }

    @Test
    void appendAsterisk_EmptyRegexArg_AsteriskAppendedRegexArg() {
        regexArgument.appendAsterisk();
        assertEquals("*", regexArgument.toString());
    }

    @Test
    void appendAsterisk_NonEmptyRegexArg_AsteriskAppendedRegexArg() {
        regexArgument = new RegexArgument(STRING_SINGLE_WORD);
        regexArgument.appendAsterisk();
        assertEquals(STRING_SINGLE_WORD + "*", regexArgument.toString());
    }

    @Test
    void merge_WithOtherNonEmptyRegexArg_MergedRegexArg() {
        RegexArgument otherRegexArg = new RegexArgument(STRING_SINGLE_WORD);
        regexArgument.merge(otherRegexArg);
        assertEquals(STRING_SINGLE_WORD, regexArgument.toString());
    }

    @Test
    void merge_WithOtherEmptyRegexArg_OriginalRegexArg() {
        RegexArgument otherRegexArg = new RegexArgument();
        regexArgument.merge(otherRegexArg);
        assertEquals(STRING_EMPTY, regexArgument.toString());
    }

    @Test
    void merge_WithNonEmptyString_MergedRegexArg() {
        regexArgument = new RegexArgument(STRING_SINGLE_WORD);
        regexArgument.merge(STRING_MULTI_WORDS);
        assertEquals(STRING_SINGLE_WORD + STRING_MULTI_WORDS, regexArgument.toString());
    }

    @Test
    void merge_WithEmptyString_OriginalRegexArg() {
        regexArgument = new RegexArgument(STRING_SINGLE_WORD);
        regexArgument.merge(STRING_EMPTY);
        assertEquals(STRING_SINGLE_WORD, regexArgument.toString());
    }

    @Test
    void isEmpty_EmptyRegexArg_ReturnsTrue() {
        assertTrue(regexArgument.isEmpty());
    }

    @Test
    void isEmpty_WhitespacesOnlyRegexArg_ReturnsFalse() {
        regexArgument = new RegexArgument(STRING_BLANK);
        assertFalse(regexArgument.isEmpty());
    }

    @Test
    void isEmpty_NonEmptyRegexArg_ReturnsFalse() {
        regexArgument = new RegexArgument(STRING_SINGLE_WORD);
        assertFalse(regexArgument.isEmpty());
    }

    @Test
    void toString_EmptyRegexArg_ReturnsEmptyString() {
        assertEquals(STRING_EMPTY, regexArgument.toString());
    }

    @Test
    void toString_WhitespacesOnlyRegexArg_ReturnsWhitespacesOnlyString() {
        regexArgument = new RegexArgument(STRING_BLANK);
        assertEquals(STRING_BLANK, regexArgument.toString());
    }

    @Test
    void toString_NonEmptyRegexArg_ReturnsNonEmptyString() {
        regexArgument = new RegexArgument(STRING_MULTI_WORDS);
        assertEquals(STRING_MULTI_WORDS, regexArgument.toString());
    }
}
