package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.app.SplitApplication.DEFAULT_LINES;
import static sg.edu.nus.comp.cs4218.impl.app.SplitApplication.DEFAULT_PREFIX;
import static sg.edu.nus.comp.cs4218.impl.parser.ArgsParser.ILLEGAL_FLAG_MSG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_ILLEGAL_BYTE_COUNT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_ILLEGAL_LINE_COUNT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_OPTION_REQUIRES_ARGUMENT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOO_MANY_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOO_MANY_OPTIONS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FLAG_PREFIX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_FLAG;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.exception.SplitException;

class SplitApplicationTest {

    private static final String LINES_OPTION = CHAR_FLAG_PREFIX + "l";
    private static final String BYTES_OPTION = CHAR_FLAG_PREFIX + "b";
    private static final String DEFAULT_OPTION_ARG = String.valueOf(DEFAULT_LINES);

    private static final String DEFAULT_DIRNAME = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + RESOURCES_PATH + STRING_FILE_SEP + "SplitApplicationTest";
    private static final String TEST_FILENAME = "test.txt";
    private static final String NON_EXISTENT_FILE = "non-existent.txt";
    private static final String TEST_FOLDER = "folder";
    private static final String TEST_STRING = "The quick brown fox jumped over the lazy dog.\n"; // 46 bytes
    private static final String XAA = "xaa";
    private static final String XAB = "xab";
    private static final String XAC = "xac";
    private static final String XAD = "xad";
    private static final String XAE = "xae";
    private static final String XAF = "xaf";
    private static final String XZZ = "xzz";

    private static File testFile;
    private static File testDir;
    private static Path testFolder;
    private static InputStream testStream;

    private final SplitApplication splitApp = new SplitApplication();

    @BeforeAll
    static void setUpBeforeAll() {
        testDir = new File(TEST_DIR);
        testDir.mkdir();
        EnvironmentUtil.currentDirectory = TEST_DIR;
    }

    @AfterAll
    static void tearDownAfterAll() {
        testDir.delete();
        EnvironmentUtil.currentDirectory = DEFAULT_DIRNAME;
    }

    private String generateString(int lines) {
        return TEST_STRING.repeat(Math.max(0, lines));
    }

    private byte[] generateBytes(int bytes) {
        byte[] arr = new byte[bytes];
        new Random().nextBytes(arr);
        return arr;
    }

    private InputStream generateStream(String string) {
        return new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
    }

    private InputStream generateStream(byte[] arr) {
        return new ByteArrayInputStream(arr);
    }

    @BeforeEach
    void setUp() throws Exception {
        testFile = new File(TEST_DIR + STRING_FILE_SEP + TEST_FILENAME);
        testFile.createNewFile();
        testFolder = Paths.get(TEST_DIR, TEST_FOLDER);
        Files.createDirectory(testFolder);
        testStream = generateStream(generateString(0));
    }

    @AfterEach
    void tearDown() throws Exception {
        for (File file : testDir.listFiles()) {
            file.delete();
        }
        testStream.close();
    }

    @Test
    void run_NullArgs_ThrowsException() {
        String[] args = {null};
        Throwable thrown = assertThrows(SplitException.class,
                () -> splitApp.run(args, testStream, null));
        Exception expected = new SplitException(ERR_NULL_ARGS);
        assertEquals(expected.getMessage(), thrown.getMessage());
    }

    @Test
    void run_IllegalOptionWrongLetter_ThrowsException() {
        String illegalFlag = "r";
        String[] args = {CHAR_FLAG_PREFIX + illegalFlag, DEFAULT_OPTION_ARG};
        Throwable thrown = assertThrows(SplitException.class,
                () -> splitApp.run(args, testStream, null));
        Exception expected = new SplitException(ILLEGAL_FLAG_MSG + illegalFlag);
        assertEquals(expected.getMessage(), thrown.getMessage());
    }

    @Test
    void run_IllegalOptionUppercaseOfLegalLetter_ThrowsException() {
        String illegalFlag = "B";
        String[] args = {CHAR_FLAG_PREFIX + illegalFlag, LINES_OPTION};
        Throwable thrown = assertThrows(SplitException.class,
                () -> splitApp.run(args, testStream, null));
        Exception expected = new SplitException(ILLEGAL_FLAG_MSG + illegalFlag);
        assertEquals(expected.getMessage(), thrown.getMessage());
    }

    @Test
    void run_TooManyLegalOptions_ThrowsException() {
        String[] args = {BYTES_OPTION, DEFAULT_OPTION_ARG, LINES_OPTION, DEFAULT_OPTION_ARG};
        Throwable thrown = assertThrows(SplitException.class,
                () -> splitApp.run(args, testStream, null));
        Exception expected = new SplitException(ERR_TOO_MANY_OPTIONS);
        assertEquals(expected.getMessage(), thrown.getMessage());
    }

    @Test
    void run_LegalOptionNoArg_ThrowsException() {
        String[] args = {LINES_OPTION};
        Throwable thrown = assertThrows(SplitException.class,
                () -> splitApp.run(args, testStream, null));
        Exception expected = new SplitException(ERR_OPTION_REQUIRES_ARGUMENT);
        assertEquals(expected.getMessage(), thrown.getMessage());
    }

    @Test
    void run_LegalOptionTooManyArgs_ThrowsException() {
        String[] args = {LINES_OPTION, DEFAULT_OPTION_ARG, TEST_FILENAME, DEFAULT_PREFIX, "extra"};
        Throwable thrown = assertThrows(SplitException.class,
                () -> splitApp.run(args, testStream, null));
        Exception expected = new SplitException(ERR_TOO_MANY_ARGS);
        assertEquals(expected.getMessage(), thrown.getMessage());
    }

    @Test
    void run_NoOptionTooManyArgs_ThrowsException() {
        String[] args = {TEST_FILENAME, DEFAULT_PREFIX, "extra"};
        Throwable thrown = assertThrows(SplitException.class,
                () -> splitApp.run(args, testStream, null));
        Exception expected = new SplitException(ERR_TOO_MANY_ARGS);
        assertEquals(expected.getMessage(), thrown.getMessage());
    }

    @Test
    void run_LinesOptionNotANumber_ThrowsException() {
        String[] args = {LINES_OPTION, "abc"};
        Throwable thrown = assertThrows(SplitException.class,
                () -> splitApp.run(args, testStream, null));
        Exception expected = new SplitException(ERR_ILLEGAL_LINE_COUNT);
        assertEquals(expected.getMessage(), thrown.getMessage());
    }

    @Test
    void splitStdinByLines_InvalidNumber_ThrowsException() {
        Throwable thrown = assertThrows(SplitException.class,
                () -> splitApp.splitStdinByLines(testStream, null, -100));
        Exception expected = new SplitException(ERR_ILLEGAL_LINE_COUNT);
        assertEquals(expected.getMessage(), thrown.getMessage());
    }

    @Test
    void run_BytesOptionNotANumber_ThrowsException() {
        String[] args = {BYTES_OPTION, "abc"};
        Throwable thrown = assertThrows(SplitException.class,
                () -> splitApp.run(args, testStream, null));
        Exception expected = new SplitException(ERR_ILLEGAL_BYTE_COUNT);
        assertEquals(expected.getMessage(), thrown.getMessage());
    }

    @Test
    void splitStdinByBytes_NotANumber_ThrowsException() {
        Throwable thrown = assertThrows(SplitException.class,
                () -> splitApp.splitStdinByBytes(testStream, null, "def"));
        Exception expected = new SplitException(ERR_ILLEGAL_BYTE_COUNT);
        assertEquals(expected.getMessage(), thrown.getMessage());
    }

    @Test
    void splitStdinByBytes_InvalidNumber_ThrowsException() {
        Throwable thrown = assertThrows(SplitException.class,
                () -> splitApp.splitStdinByBytes(testStream, null, "0"));
        Exception expected = new SplitException(ERR_ILLEGAL_BYTE_COUNT);
        assertEquals(expected.getMessage(), thrown.getMessage());
    }

    @Test
    void splitStdinByBytes_InvalidAppendage_ThrowsException() {
        Throwable thrown = assertThrows(SplitException.class,
                () -> splitApp.splitStdinByBytes(testStream, null, "1024a"));
        Exception expected = new SplitException(ERR_ILLEGAL_BYTE_COUNT);
        assertEquals(expected.getMessage(), thrown.getMessage());
    }

    @Test
    void run_FilenameDash_UseStdin() throws Exception {
        String expectedContent = generateString(1);
        testStream = generateStream(expectedContent);
        String[] args = {LINES_OPTION, "1", STRING_STDIN_FLAG};
        assertDoesNotThrow(() -> splitApp.run(args, testStream, null));
        File file = new File(TEST_DIR + STRING_FILE_SEP + XAA);
        assertTrue(file.exists());
        String actualContent = new String(Files.readAllBytes(file.toPath()));
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void run_676LinesStdin_GenerateCorrectFilenames() {
        String testContent = generateString(676);
        testStream = generateStream(testContent);
        String[] args = {LINES_OPTION, "1"};
        assertDoesNotThrow(() -> splitApp.run(args, testStream, null));
        File firstFile = new File(TEST_DIR + STRING_FILE_SEP + XAA);
        File lastFile = new File(TEST_DIR + STRING_FILE_SEP + XZZ);
        assertTrue(firstFile.exists());
        assertTrue(lastFile.exists());
    }

    @Test
    void run_676LinesFilePrefix_GenerateCorrectFilenames() throws Exception {
        Files.writeString(testFile.toPath(), generateString(676));
        String[] args = {LINES_OPTION, "1", TEST_FILENAME, "prefix_"};
        assertDoesNotThrow(() -> splitApp.run(args, testStream, null));
        File firstFile = new File(TEST_DIR + STRING_FILE_SEP + "prefix_aa");
        File lastFile = new File(TEST_DIR + STRING_FILE_SEP + "prefix_zz");
        assertTrue(firstFile.exists());
        assertTrue(lastFile.exists());
    }

    @Test
    void run_1353BytesStdinPrefix_GenerateCorrectFilenames() {
        byte[] testContent = generateBytes(1353);
        testStream = generateStream(testContent);
        String[] args = {BYTES_OPTION, "1"};
        assertDoesNotThrow(() -> splitApp.run(args, testStream, null));
        File firstFile = new File(TEST_DIR + STRING_FILE_SEP + "xzaa");
        File lastFile = new File(TEST_DIR + STRING_FILE_SEP + "xzzaa");
        assertTrue(firstFile.exists());
        assertTrue(lastFile.exists());
    }

    @Test
    void run_1LineStdin_NoSplit() throws Exception {
        String expectedContent = generateString(1);
        testStream = generateStream(expectedContent);
        String[] args = {};
        assertDoesNotThrow(() -> splitApp.run(args, testStream, null));
        File file = new File(TEST_DIR + STRING_FILE_SEP + XAA);
        File overflowFile = new File(TEST_DIR + STRING_FILE_SEP + XAB);
        assertTrue(file.exists());
        assertFalse(overflowFile.exists());
        String actualContent = new String(Files.readAllBytes(file.toPath()));
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void run_1000LinesFile_NoSplit() throws Exception {
        String expectedContent = generateString(1000);
        Files.writeString(testFile.toPath(), expectedContent);
        String[] args = {TEST_FILENAME};
        assertDoesNotThrow(() -> splitApp.run(args, testStream, null));
        File file = new File(TEST_DIR + STRING_FILE_SEP + XAA);
        File overflowFile = new File(TEST_DIR + STRING_FILE_SEP + XAB);
        assertTrue(file.exists());
        assertFalse(overflowFile.exists());
        String actualContent = new String(Files.readAllBytes(file.toPath()));
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void run_1500LinesStdin_2Splits() throws Exception {
        testStream = generateStream(generateString(1500));
        String[] args = {};
        assertDoesNotThrow(() -> splitApp.run(args, testStream, null));
        File firstFile = new File(TEST_DIR + STRING_FILE_SEP + XAA);
        File lastFile = new File(TEST_DIR + STRING_FILE_SEP + XAB);
        File overflowFile = new File(TEST_DIR + STRING_FILE_SEP + XAC);
        assertTrue(firstFile.exists());
        assertTrue(lastFile.exists());
        assertFalse(overflowFile.exists());
        String expectedFirstContent = generateString(1000);
        String actualFirstContent = new String(Files.readAllBytes(firstFile.toPath()));
        assertEquals(expectedFirstContent, actualFirstContent);
        String expectedLastContent = generateString(500);
        String actualLastContent = new String(Files.readAllBytes(lastFile.toPath()));
        assertEquals(expectedLastContent, actualLastContent);
    }


    @Test
    void run_4000LinesFile_4Splits() throws Exception {
        Files.writeString(testFile.toPath(), generateString(4000));
        String[] args = {TEST_FILENAME};
        assertDoesNotThrow(() -> splitApp.run(args, testStream, null));
        File firstFile = new File(TEST_DIR + STRING_FILE_SEP + XAA);
        File lastFile = new File(TEST_DIR + STRING_FILE_SEP + XAD);
        File overflowFile = new File(TEST_DIR + STRING_FILE_SEP + XAE);
        assertTrue(firstFile.exists());
        assertTrue(lastFile.exists());
        assertFalse(overflowFile.exists());
        String expectedFirstContent = generateString(1000);
        String actualFirstContent = new String(Files.readAllBytes(firstFile.toPath()));
        assertEquals(expectedFirstContent, actualFirstContent);
        String expectedLastContent = generateString(1000);
        String actualLastContent = new String(Files.readAllBytes(lastFile.toPath()));
        assertEquals(expectedLastContent, actualLastContent);
    }

    @Test
    void splitStdinByLines_3LinesOption8LinesStdin_3Splits() throws Exception {
        testStream = generateStream(generateString(8));
        assertDoesNotThrow(() -> splitApp.splitStdinByLines(testStream, null, 3));
        File firstFile = new File(TEST_DIR + STRING_FILE_SEP + XAA);
        File lastFile = new File(TEST_DIR + STRING_FILE_SEP + XAC);
        File overflowFile = new File(TEST_DIR + STRING_FILE_SEP + XAD);
        assertTrue(firstFile.exists());
        assertTrue(lastFile.exists());
        assertFalse(overflowFile.exists());
        String expectedFirstContent = generateString(3);
        String actualFirstContent = new String(Files.readAllBytes(firstFile.toPath()));
        assertEquals(expectedFirstContent, actualFirstContent);
        String expectedLastContent = generateString(2);
        String actualLastContent = new String(Files.readAllBytes(lastFile.toPath()));
        assertEquals(expectedLastContent, actualLastContent);
    }

    @Test
    void splitFileByLines_20LinesOption100LinesFile_5Splits() throws Exception {
        Files.writeString(testFile.toPath(), generateString(100));
        assertDoesNotThrow(() -> splitApp.splitFileByLines(TEST_FILENAME, null, 20));
        File firstFile = new File(TEST_DIR + STRING_FILE_SEP + XAA);
        File lastFile = new File(TEST_DIR + STRING_FILE_SEP + XAE);
        File overflowFile = new File(TEST_DIR + STRING_FILE_SEP + XAF);
        assertTrue(firstFile.exists());
        assertTrue(lastFile.exists());
        assertFalse(overflowFile.exists());
        String expectedFirstContent = generateString(20);
        String actualFirstContent = new String(Files.readAllBytes(firstFile.toPath()));
        assertEquals(expectedFirstContent, actualFirstContent);
        String expectedLastContent = generateString(20);
        String actualLastContent = new String(Files.readAllBytes(lastFile.toPath()));
        assertEquals(expectedLastContent, actualLastContent);
    }

    @Test
    void run_20LinesOption100LinesFile_5Splits() throws Exception {
        Files.writeString(testFile.toPath(), generateString(100));
        String[] args = {LINES_OPTION, "20", TEST_FILENAME};
        assertDoesNotThrow(() -> splitApp.run(args, testStream, null));
        File firstFile = new File(TEST_DIR + STRING_FILE_SEP + XAA);
        File lastFile = new File(TEST_DIR + STRING_FILE_SEP + XAE);
        File overflowFile = new File(TEST_DIR + STRING_FILE_SEP + XAF);
        assertTrue(firstFile.exists());
        assertTrue(lastFile.exists());
        assertFalse(overflowFile.exists());
        String expectedFirstContent = generateString(20);
        String actualFirstContent = new String(Files.readAllBytes(firstFile.toPath()));
        assertEquals(expectedFirstContent, actualFirstContent);
        String expectedLastContent = generateString(20);
        String actualLastContent = new String(Files.readAllBytes(lastFile.toPath()));
        assertEquals(expectedLastContent, actualLastContent);
    }

    @Test
    void splitStdinByBytes_16BytesOption46BytesStdin_3Splits() throws Exception {
        byte[] testContent = generateBytes(46);
        testStream = generateStream(testContent);
        assertDoesNotThrow(() -> splitApp.splitStdinByBytes(testStream, null, "16"));
        File firstFile = new File(TEST_DIR + STRING_FILE_SEP + XAA);
        File lastFile = new File(TEST_DIR + STRING_FILE_SEP + XAC);
        File overflowFile = new File(TEST_DIR + STRING_FILE_SEP + XAD);
        assertTrue(firstFile.exists());
        assertTrue(lastFile.exists());
        assertFalse(overflowFile.exists());
        byte[] expectedFirstContent = Arrays.copyOfRange(testContent, 0, 16);
        byte[] actualFirstContent = Files.readAllBytes(firstFile.toPath());
        assertArrayEquals(expectedFirstContent, actualFirstContent);
        byte[] expectedLastContent = Arrays.copyOfRange(testContent, 32, 46);
        byte[] actualLastContent = Files.readAllBytes(lastFile.toPath());
        assertArrayEquals(expectedLastContent, actualLastContent);
    }

    @Test
    void splitFileByBytes_8BytesOption16BytesFile_2Splits() throws Exception {
        byte[] testContent = generateBytes(16);
        Files.write(testFile.toPath(), testContent);
        assertDoesNotThrow(() -> splitApp.splitFileByBytes(TEST_FILENAME, null, "8"));
        File firstFile = new File(TEST_DIR + STRING_FILE_SEP + XAA);
        File lastFile = new File(TEST_DIR + STRING_FILE_SEP + XAB);
        File overflowFile = new File(TEST_DIR + STRING_FILE_SEP + XAC);
        assertTrue(firstFile.exists());
        assertTrue(lastFile.exists());
        assertFalse(overflowFile.exists());
        byte[] expectedFirstContent = Arrays.copyOfRange(testContent, 0, 8);
        byte[] actualFirstContent = Files.readAllBytes(firstFile.toPath());
        assertArrayEquals(expectedFirstContent, actualFirstContent);
        byte[] expectedLastContent = Arrays.copyOfRange(testContent, 8, 16);
        byte[] actualLastContent = Files.readAllBytes(lastFile.toPath());
        assertArrayEquals(expectedLastContent, actualLastContent);
    }

    @Test
    void splitStdinByBytes_16bBytesOption2048BytesStdin_2Splits() throws Exception {
        byte[] testContent = generateBytes(16384);
        testStream = generateStream(testContent);
        assertDoesNotThrow(() -> splitApp.splitStdinByBytes(testStream, null, "16b"));
        File firstFile = new File(TEST_DIR + STRING_FILE_SEP + XAA);
        File lastFile = new File(TEST_DIR + STRING_FILE_SEP + XAB);
        File overflowFile = new File(TEST_DIR + STRING_FILE_SEP + XAC);
        assertTrue(firstFile.exists());
        assertTrue(lastFile.exists());
        assertFalse(overflowFile.exists());
        byte[] expectedFirstContent = Arrays.copyOfRange(testContent, 0, 8192);
        byte[] actualFirstContent = Files.readAllBytes(firstFile.toPath());
        assertArrayEquals(expectedFirstContent, actualFirstContent);
        byte[] expectedLastContent = Arrays.copyOfRange(testContent, 8192, 16384);
        byte[] actualLastContent = Files.readAllBytes(lastFile.toPath());
        assertArrayEquals(expectedLastContent, actualLastContent);
    }

    @Test
    void splitFileByBytes_2kBytesOption8000BytesFile_4Splits() throws Exception {
        byte[] testContent = generateBytes(8000);
        Files.write(testFile.toPath(), testContent);
        assertDoesNotThrow(() -> splitApp.splitFileByBytes(TEST_FILENAME, null, "2k"));
        File firstFile = new File(TEST_DIR + STRING_FILE_SEP + XAA);
        File lastFile = new File(TEST_DIR + STRING_FILE_SEP + XAD);
        File overflowFile = new File(TEST_DIR + STRING_FILE_SEP + XAE);
        assertTrue(firstFile.exists());
        assertTrue(lastFile.exists());
        assertFalse(overflowFile.exists());
        byte[] expectedFirstContent = Arrays.copyOfRange(testContent, 0, 2048);
        byte[] actualFirstContent = Files.readAllBytes(firstFile.toPath());
        assertArrayEquals(expectedFirstContent, actualFirstContent);
        byte[] expectedLastContent = Arrays.copyOfRange(testContent, 6144, 8000);
        byte[] actualLastContent = Files.readAllBytes(lastFile.toPath());
        assertArrayEquals(expectedLastContent, actualLastContent);
    }

    @Test
    void splitStdinByBytes_1mBytesOption2000000BytesStdin_2Splits() throws Exception {
        byte[] testContent = generateBytes(2000000);
        testStream = generateStream(testContent);
        assertDoesNotThrow(() -> splitApp.splitStdinByBytes(testStream, null, "1m"));
        File firstFile = new File(TEST_DIR + STRING_FILE_SEP + XAA);
        File lastFile = new File(TEST_DIR + STRING_FILE_SEP + XAB);
        File overflowFile = new File(TEST_DIR + STRING_FILE_SEP + XAC);
        assertTrue(firstFile.exists());
        assertTrue(lastFile.exists());
        assertFalse(overflowFile.exists());
        byte[] expectedFirstContent = Arrays.copyOfRange(testContent, 0, 1048576);
        byte[] actualFirstContent = Files.readAllBytes(firstFile.toPath());
        assertArrayEquals(expectedFirstContent, actualFirstContent);
        byte[] expectedLastContent = Arrays.copyOfRange(testContent, 1048576, 2000000);
        byte[] actualLastContent = Files.readAllBytes(lastFile.toPath());
        assertArrayEquals(expectedLastContent, actualLastContent);
    }

    @Test
    public void splitFileByBytes_FileDoesNotExist_ThrowsException() {
        Throwable exception = assertThrows(SplitException.class, () -> {
            splitApp.splitFileByBytes(NON_EXISTENT_FILE, null, "16");
        });
        assertEquals(
                new SplitException(new InvalidDirectoryException(NON_EXISTENT_FILE, ERR_FILE_NOT_FOUND).getMessage()).getMessage(),
                exception.getMessage()
        );
    }

    @Test
    public void splitFileByBytes_DirectorySupplied_ThrowsException() {
        Throwable exception = assertThrows(SplitException.class, () -> {
            splitApp.splitFileByBytes(TEST_FOLDER, null, "16");
        });
        assertEquals(
                new SplitException(new InvalidDirectoryException(TEST_FOLDER, ERR_IS_DIR).getMessage()).getMessage(),
                exception.getMessage());
    }

    @Test
    void run_1mBytesOption2000000BytesStdin_2Splits() throws Exception {
        byte[] testContent = generateBytes(2000000);
        testStream = generateStream(testContent);
        String[] args = {BYTES_OPTION, "1m"};
        assertDoesNotThrow(() -> splitApp.run(args, testStream, null));
        File firstFile = new File(TEST_DIR + STRING_FILE_SEP + XAA);
        File lastFile = new File(TEST_DIR + STRING_FILE_SEP + XAB);
        File overflowFile = new File(TEST_DIR + STRING_FILE_SEP + XAC);
        assertTrue(firstFile.exists());
        assertTrue(lastFile.exists());
        assertFalse(overflowFile.exists());
        byte[] expectedFirstContent = Arrays.copyOfRange(testContent, 0, 1048576);
        byte[] actualFirstContent = Files.readAllBytes(firstFile.toPath());
        assertArrayEquals(expectedFirstContent, actualFirstContent);
        byte[] expectedLastContent = Arrays.copyOfRange(testContent, 1048576, 2000000);
        byte[] actualLastContent = Files.readAllBytes(lastFile.toPath());
        assertArrayEquals(expectedLastContent, actualLastContent);
    }
}