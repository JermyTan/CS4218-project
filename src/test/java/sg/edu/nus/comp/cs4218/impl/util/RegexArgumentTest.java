package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegexArgumentTest {

    private static final String RESOURCES_PATH = "src/test/resources/";
    private static final File RESOURCES_DIRECTORY = new File(RESOURCES_PATH);

    private final File file1 = new File(RESOURCES_DIRECTORY, "file1.txt");
    private final File file2 = new File(RESOURCES_DIRECTORY, "file2.txt");
    private final File file3 = new File(RESOURCES_DIRECTORY, "test.md");

    private final File folder1 = new File(RESOURCES_DIRECTORY, "folder1");
    private final File folder2 = new File(RESOURCES_DIRECTORY, "folder2");
    private final File folder3 = new File(RESOURCES_DIRECTORY, "testFolder");

    private RegexArgument regexArgument;

    private String resolveArg(String arg) {
        return new File(RESOURCES_DIRECTORY, arg).getAbsolutePath();
    }

    @BeforeEach
    void setup() throws IOException {
        file1.createNewFile();
        file2.createNewFile();
        file3.createNewFile();
        folder1.mkdir();
        folder2.mkdir();
        folder3.mkdir();

        regexArgument = new RegexArgument();
        regexArgument.merge(RESOURCES_PATH);
    }

    @AfterEach
    void tearDown() {
        file1.delete();
        file2.delete();
        file3.delete();
        folder1.delete();
        folder2.delete();
        folder3.delete();
    }

    @Test
    public void globFiles_NoMatchedFile_ArgUnchanged() {
        regexArgument.append('x');
        regexArgument.appendAsterisk();

        List<String> globbedFiles = regexArgument.globFiles();

        assertEquals(1, globbedFiles.size());
        assertEquals(regexArgument.toString(), globbedFiles.get(0));
    }

    @Test
    public void globFiles_NoMatchedFolder_ArgUnchanged() {
        regexArgument.merge("folder3");
        regexArgument.appendAsterisk();
        regexArgument.merge(File.separator);

        List<String> globbedFiles = regexArgument.globFiles();

        assertEquals(1, globbedFiles.size());
        assertEquals(regexArgument.toString(), globbedFiles.get(0));
    }

    @Test
    public void globFiles_OneMatch_ReturnsGlobbedFile() {
        regexArgument.appendAsterisk();
        regexArgument.merge(".md");

        List<String> globbedFiles = regexArgument.globFiles();

        assertEquals(1, globbedFiles.size());
        assertEquals(resolveArg("test.md"), globbedFiles.get(0));
    }

    @Test
    public void globFiles_MoreThanOneMatch_ReturnsGlobbedFilesSorted() {
        regexArgument.appendAsterisk();
        regexArgument.merge(".txt");

        List<String> globbedFiles = regexArgument.globFiles();

        assertEquals(2, globbedFiles.size());
        assertEquals(resolveArg("file1.txt"), globbedFiles.get(0));
        assertEquals(resolveArg("file2.txt"), globbedFiles.get(1));
    }

    @Test
    public void globFiles_EndWithSlash_MatchFoldersOnly() {
        regexArgument.merge("f");
        regexArgument.appendAsterisk();
        regexArgument.merge(File.separator);

        List<String> globbedFiles = regexArgument.globFiles();

        assertEquals(2, globbedFiles.size());
        assertEquals(resolveArg("folder1"), globbedFiles.get(0));
        assertEquals(resolveArg("folder2"), globbedFiles.get(1));
    }

    @Test
    public void globFiles_DoesNotEndWithSlash_MatchFilesAndFolders() {
        regexArgument.merge("f");
        regexArgument.appendAsterisk();

        List<String> globbedFiles = regexArgument.globFiles();

        assertEquals(4, globbedFiles.size());
        assertEquals(resolveArg("file1.txt"), globbedFiles.get(0));
        assertEquals(resolveArg("file2.txt"), globbedFiles.get(1));
        assertEquals(resolveArg("folder1"), globbedFiles.get(2));
        assertEquals(resolveArg("folder2"), globbedFiles.get(3));
    }

    @Test
    public void globFiles_AsteriskSlash_MatchAllFolders() {
        regexArgument.appendAsterisk();
        regexArgument.merge(File.separator);

        List<String> globbedFiles = regexArgument.globFiles();

        assertEquals(3, globbedFiles.size());
        assertEquals(resolveArg("folder1"), globbedFiles.get(0));
        assertEquals(resolveArg("folder2"), globbedFiles.get(1));
        assertEquals(resolveArg("testFolder"), globbedFiles.get(2));
    }

    @Test
    public void globFiles_AbsolutePath_ReturnsGlobbedFilesSorted() {
        regexArgument = new RegexArgument();
        regexArgument.merge(System.getProperty("user.dir"));
        regexArgument.merge(File.separator);
        regexArgument.merge(RESOURCES_PATH);
        regexArgument.merge("file");
        regexArgument.appendAsterisk();

        List<String> globbedFiles = regexArgument.globFiles();

        assertEquals(2, globbedFiles.size());
        assertEquals(resolveArg("file1.txt"), globbedFiles.get(0));
        assertEquals(resolveArg("file2.txt"), globbedFiles.get(1));
    }

    @Test
    public void globFiles_MatchFoldersUnderCwd_ReturnsGlobbedFilesSorted() {
        // Find folders under current working directory
        File file = new File(System.getProperty("user.dir"));
        List<String> directories = new LinkedList<>();
        for (File node : file.listFiles()) {
            if (!node.isHidden() && node.isDirectory()) {
                directories.add(node.getPath());
            }
        }
        Collections.sort(directories);

        regexArgument = new RegexArgument();
        regexArgument.appendAsterisk();
        regexArgument.merge(File.separator);

        List<String> globbedFiles = regexArgument.globFiles();

        assertEquals(directories.size(), globbedFiles.size());
        for (int i = 0; i < globbedFiles.size(); i++) {
            assertEquals(directories.get(i), globbedFiles.get(i));
        }
    }
}
