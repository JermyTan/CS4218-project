package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.Environment;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegexArgumentTest {

    private static final String RESOURCES_PATH = "src/test/resources";
    private static final String ORIGINAL_DIR = Environment.currentDirectory;
    private static final String TESTDIR = Environment.currentDirectory + File.separator + RESOURCES_PATH + File.separator + "RegexArgumentTest";

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";
    private static final String FILE_3 = "test.md";

    private static final String FOLDER_1 = "folder1";
    private static final String FOLDER_2 = "folder2";
    private static final String FOLDER_3 = "testFolder";

    private RegexArgument regexArgument;

    private String resolveArg(String arg) {
        return Paths.get(TESTDIR, arg).toString();
    }

    @BeforeAll
    static void setupBeforeAll() {
        Environment.currentDirectory = TESTDIR;
    }

    @AfterAll
    static void tearDownAfterAll() {
        Environment.currentDirectory = ORIGINAL_DIR;
    }

    @BeforeEach
    void setup() {
        regexArgument = new RegexArgument();
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
        regexArgument.merge(File.separator);

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
        regexArgument.merge(File.separator);

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
        regexArgument.merge(File.separator);

        List<String> globbedFiles = regexArgument.globFiles();

        assertEquals(3, globbedFiles.size());
        assertEquals(resolveArg(FOLDER_1), globbedFiles.get(0));
        assertEquals(resolveArg(FOLDER_2), globbedFiles.get(1));
        assertEquals(resolveArg(FOLDER_3), globbedFiles.get(2));
    }

    @Test
    public void globFiles_AbsolutePath_ReturnsGlobbedFilesSorted() {
        regexArgument.merge(Environment.currentDirectory);
        regexArgument.merge(File.separator);
        regexArgument.merge("file");
        regexArgument.appendAsterisk();

        List<String> globbedFiles = regexArgument.globFiles();

        assertEquals(2, globbedFiles.size());
        assertEquals(resolveArg(FILE_1), globbedFiles.get(0));
        assertEquals(resolveArg(FILE_2), globbedFiles.get(1));
    }
}
