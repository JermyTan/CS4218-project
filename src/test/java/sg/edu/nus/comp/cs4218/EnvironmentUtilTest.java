package sg.edu.nus.comp.cs4218;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_CURR_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_PARENT_DIR;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_FILE_MD;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_FILE_TXT;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_SINGLE_WORD;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

class EnvironmentUtilTest {
    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = String.join(STRING_FILE_SEP,
            EnvironmentUtil.currentDirectory,
            RESOURCES_PATH,
            "EnvironmentUtilTest");
    private static final Path TEST_PATH = Path.of(TEST_DIR);

    private Path sampleDir;

    @BeforeAll
    static void setupBeforeAll() throws IOException {
        if (!Files.exists(TEST_PATH)) {
            Files.createDirectory(TEST_PATH);
        }

        EnvironmentUtil.currentDirectory = TEST_DIR;
    }

    @AfterAll
    static void tearDownAfterAll() throws Exception {
        Files.deleteIfExists(TEST_PATH);
        EnvironmentUtil.currentDirectory = ORIGINAL_DIR;
    }

    @BeforeEach
    void setup() throws Exception {
        Files.createFile(IOUtils.resolveAbsoluteFilePath(STRING_FILE_TXT));
        sampleDir = Files.createDirectory(IOUtils.resolveAbsoluteFilePath(STRING_SINGLE_WORD));
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.list(TEST_PATH).forEach(path -> {
            try {
                if (Files.isDirectory(path)) {
                    Files.walk(path)
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                } else {
                    Files.deleteIfExists(path);
                }
            } catch (Exception e) {
                // do nth
            }
        });
    }

    @Test
    void setCurrentDirectory_relativePath_Success() throws Exception {
        EnvironmentUtil.setCurrentDirectory(STRING_SINGLE_WORD);
        assertEquals(sampleDir.toString(), EnvironmentUtil.currentDirectory);

        EnvironmentUtil.setCurrentDirectory(STRING_PARENT_DIR + STRING_FILE_SEP);
        assertEquals(TEST_DIR, EnvironmentUtil.currentDirectory);

        EnvironmentUtil.setCurrentDirectory(STRING_EMPTY);
        assertEquals(TEST_DIR, EnvironmentUtil.currentDirectory);

        EnvironmentUtil.setCurrentDirectory(STRING_CURR_DIR);
        assertEquals(TEST_DIR, EnvironmentUtil.currentDirectory);

        EnvironmentUtil.setCurrentDirectory(STRING_CURR_DIR + STRING_FILE_SEP);
        assertEquals(TEST_DIR, EnvironmentUtil.currentDirectory);

        EnvironmentUtil.setCurrentDirectory(STRING_CURR_DIR + STRING_FILE_SEP + STRING_SINGLE_WORD + STRING_FILE_SEP + STRING_PARENT_DIR);
        assertEquals(TEST_DIR, EnvironmentUtil.currentDirectory);
    }

    @Test
    void setCurrentDirectory_InvalidDirectory_DoesNothing() throws Exception {
        // not a directory
        EnvironmentUtil.setCurrentDirectory(STRING_FILE_TXT);
        assertEquals(TEST_DIR, EnvironmentUtil.currentDirectory);

        // non-existent directory
        EnvironmentUtil.setCurrentDirectory(STRING_FILE_MD);
        assertEquals(TEST_DIR, EnvironmentUtil.currentDirectory);
    }
}