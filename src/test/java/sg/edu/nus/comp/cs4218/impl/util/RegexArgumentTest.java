package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.Environment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegexArgumentTest {

    private static final String RESOURCES_PATH = "src/test/resources";
    private static final String ORIGINAL_DIR = Environment.currentDirectory;
    private static final String TESTDIR_PATH = Environment.currentDirectory + File.separator + RESOURCES_PATH + File.separator + "RegexArgumentTest";

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";
    private static final String FILE_3 = "test.md";

    private static final String FOLDER_1 = "folder1";
    private static final String FOLDER_2 = "folder2";
    private static final String FOLDER_3 = "testFolder";

    private final Path file1 = Paths.get(TESTDIR_PATH, FILE_1);
    private final Path file2 = Paths.get(TESTDIR_PATH, FILE_2);
    private final Path file3 = Paths.get(TESTDIR_PATH, FILE_3);

    private final Path folder1 = Paths.get(TESTDIR_PATH, FOLDER_1);
    private final Path folder2 = Paths.get(TESTDIR_PATH, FOLDER_2);
    private final Path folder3 = Paths.get(TESTDIR_PATH, FOLDER_3);

    private final List<Path> paths = List.of(file1, file2, file3, folder1, folder2, folder3);

    private RegexArgument regexArgument;

    private String resolveArg(String arg) {
        return Paths.get(TESTDIR_PATH, arg).toString();
    }

    @BeforeAll
    static void setupBeforeAll() {
        Environment.currentDirectory = TESTDIR_PATH;

        try {
            Files.createDirectories(Paths.get(TESTDIR_PATH));
        } catch (IOException e) {
            fail(e.getMessage());
        }

    }

    @AfterAll
    static void tearDownAfterAll() {
        Environment.currentDirectory = ORIGINAL_DIR;

        try {
            Files.delete(Paths.get(TESTDIR_PATH));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @BeforeEach
    void setup() {
        regexArgument = new RegexArgument();

        try {
            Files.createFile(file1);
            Files.createFile(file2);
            Files.createFile(file3);

            Files.createDirectory(folder1);
            Files.createDirectory(folder2);
            Files.createDirectory(folder3);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        try {
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
        } catch (IOException e) {
            fail(e.getMessage());
        }
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
        assertEquals(resolveArg(FILE_3), globbedFiles.get(0));
    }

    @Test
    public void globFiles_MoreThanOneMatch_ReturnsGlobbedFilesSorted() {
        regexArgument.appendAsterisk();
        regexArgument.merge(".txt");

        List<String> globbedFiles = regexArgument.globFiles();

        assertEquals(2, globbedFiles.size());
        assertEquals(resolveArg(FILE_1), globbedFiles.get(0));
        assertEquals(resolveArg(FILE_2), globbedFiles.get(1));
    }

    @Test
    public void globFiles_EndWithSlash_MatchFoldersOnly() {
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
