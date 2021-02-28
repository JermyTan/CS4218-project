package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.SplitException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Random;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.parser.ArgsParser.ILLEGAL_FLAG_MSG;
import static sg.edu.nus.comp.cs4218.impl.app.SplitApplication.DEFAULT_LINES;
import static sg.edu.nus.comp.cs4218.impl.app.SplitApplication.DEFAULT_PREFIX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FLAG_PREFIX;
import static org.junit.jupiter.api.Assertions.*;

class SplitApplicationTest {

    private static final String LINES_OPTION = CHAR_FLAG_PREFIX + "l";
    private static final String BYTES_OPTION = CHAR_FLAG_PREFIX + "b";
    private static final String DEFAULT_OPTION_ARG = String.valueOf(DEFAULT_LINES);

    private static final String DEFAULT_DIRNAME = Environment.currentDirectory;
    private static final String TEST_DIRNAME = Environment.currentDirectory + File.separator + "SplitApplicationTest";
    private static final String TEST_FILENAME = "test.txt";
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
    private static InputStream testStream;

    private final SplitApplication splitApp = new SplitApplication();

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

    @BeforeAll
    static void setUpBeforeAll() {
        testDir = new File(TEST_DIRNAME);
        testDir.mkdir();
        Environment.currentDirectory = TEST_DIRNAME;
    }

    @AfterAll
    static void tearDownAfterAll() {
        testDir.delete();
        Environment.currentDirectory = DEFAULT_DIRNAME;
    }

    @BeforeEach
    void setUp() throws Exception {
        testFile = new File(TEST_DIRNAME + File.separator + TEST_FILENAME);
        testFile.createNewFile();
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
    void testRun_WhenNullArgs_ShouldThrowException() {
        String[] args = { null };
        Throwable thrown = assertThrows(SplitException.class,
                () -> splitApp.run(args, testStream, null));
        Exception expected = new SplitException(ERR_NULL_ARGS);
        assertEquals(expected.getMessage(), thrown.getMessage());
    }

    @Test
    void testRun_WhenIllegalOptionWrongLetter_ShouldThrowException() {
        String illegalFlag = "r";
        String[] args = { CHAR_FLAG_PREFIX + illegalFlag, DEFAULT_OPTION_ARG };
        Throwable thrown = assertThrows(SplitException.class,
                () -> splitApp.run(args, testStream, null));
        Exception expected = new SplitException(ILLEGAL_FLAG_MSG + illegalFlag);
        assertEquals(expected.getMessage(), thrown.getMessage());
    }

    @Test
    void testRun_WhenIllegalOptionUppercaseOfLegalLetter_ShouldThrowException() {
        String illegalFlag = "B";
        String[] args = { CHAR_FLAG_PREFIX + illegalFlag, LINES_OPTION };
        Throwable thrown = assertThrows(SplitException.class,
                () -> splitApp.run(args, testStream, null));
        Exception expected = new SplitException(ILLEGAL_FLAG_MSG + illegalFlag);
        assertEquals(expected.getMessage(), thrown.getMessage());
    }

    @Test
    void testRun_WhenTooManyLegalOptions_ShouldThrowException() {
        String[] args = { BYTES_OPTION, DEFAULT_OPTION_ARG, LINES_OPTION, DEFAULT_OPTION_ARG };
        Throwable thrown = assertThrows(SplitException.class,
                () -> splitApp.run(args, testStream, null));
        Exception expected = new SplitException(ERR_TOO_MANY_OPTIONS);
        assertEquals(expected.getMessage(), thrown.getMessage());
    }

    @Test
    void testRun_WhenLegalOptionNoArg_ShouldThrowException() {
        String[] args = { LINES_OPTION };
        Throwable thrown = assertThrows(SplitException.class,
                () -> splitApp.run(args, testStream, null));
        Exception expected = new SplitException(ERR_OPTION_REQUIRES_ARGUMENT);
        assertEquals(expected.getMessage(), thrown.getMessage());
    }

    @Test
    void testRun_WhenLegalOptionTooManyArgs_ShouldThrowException() {
        String[] args = { LINES_OPTION, DEFAULT_OPTION_ARG, TEST_FILENAME, DEFAULT_PREFIX, "extra" };
        Throwable thrown = assertThrows(SplitException.class,
                () -> splitApp.run(args, testStream, null));
        Exception expected = new SplitException(ERR_TOO_MANY_ARGS);
        assertEquals(expected.getMessage(), thrown.getMessage());
    }

    @Test
    void testRun_WhenNoOptionTooManyArgs_ShouldThrowException() {
        String[] args = { TEST_FILENAME, DEFAULT_PREFIX, "extra" };
        Throwable thrown = assertThrows(SplitException.class,
                () -> splitApp.run(args, testStream, null));
        Exception expected = new SplitException(ERR_TOO_MANY_ARGS);
        assertEquals(expected.getMessage(), thrown.getMessage());
    }

    @Test
    void testRun_WhenLinesOptionNotANumber_ShouldThrowException() {
        String[] args = { LINES_OPTION, "abc" };
        Throwable thrown = assertThrows(SplitException.class,
                () -> splitApp.run(args, testStream, null));
        Exception expected = new SplitException(ERR_ILLEGAL_LINE_COUNT);
        assertEquals(expected.getMessage(), thrown.getMessage());
    }

    @Test
    void testRun_WhenLinesOptionInvalidNumber_ShouldThrowException() {
        String[] args = { LINES_OPTION, "0" };
        Throwable thrown = assertThrows(SplitException.class,
                () -> splitApp.run(args, testStream, null));
        Exception expected = new SplitException(ERR_ILLEGAL_LINE_COUNT);
        assertEquals(expected.getMessage(), thrown.getMessage());
    }

    @Test
    void testRun_WhenBytesOptionNotANumber_ShouldThrowException() {
        String[] args = { BYTES_OPTION, "abc" };
        Throwable thrown = assertThrows(SplitException.class,
                () -> splitApp.run(args, testStream, null));
        Exception expected = new SplitException(ERR_ILLEGAL_BYTE_COUNT);
        assertEquals(expected.getMessage(), thrown.getMessage());
    }

    @Test
    void testRun_WhenBytesOptionInvalidNumber_ShouldThrowException() {
        String[] args = { BYTES_OPTION, "0" };
        Throwable thrown = assertThrows(SplitException.class,
                () -> splitApp.run(args, testStream, null));
        Exception expected = new SplitException(ERR_ILLEGAL_BYTE_COUNT);
        assertEquals(expected.getMessage(), thrown.getMessage());
    }

    @Test
    void testRun_WhenBytesOptionInvalidAppendage_ShouldThrowException() {
        String[] args = { BYTES_OPTION, "1024a" };
        Throwable thrown = assertThrows(SplitException.class,
                () -> splitApp.run(args, testStream, null));
        Exception expected = new SplitException(ERR_ILLEGAL_BYTE_COUNT);
        assertEquals(expected.getMessage(), thrown.getMessage());
    }

    @Test
    void testRun_WhenFilenameDash_ShouldUseStdin() throws Exception {
        String expectedContent = generateString(1);
        testStream = generateStream(expectedContent);
        String[] args = {LINES_OPTION, "1", "-"};
        assertDoesNotThrow(() -> splitApp.run(args, testStream, null));
        File file = new File(TEST_DIRNAME + File.separator + XAA);
        assertTrue(file.exists());
        String actualContent = new String(Files.readAllBytes(file.toPath()));
        assertEquals(expectedContent, actualContent);

    }

    @Test
    void testRun_When676LinesStdin_ShouldGenerateCorrectFilenames() {
        String testContent = generateString(676);
        testStream = generateStream(testContent);
        String[] args = { LINES_OPTION, "1" };
        assertDoesNotThrow(() -> splitApp.run(args, testStream, null));
        File firstFile = new File(TEST_DIRNAME + File.separator + XAA);
        File lastFile = new File(TEST_DIRNAME + File.separator + XZZ);
        assertTrue(firstFile.exists());
        assertTrue(lastFile.exists());
    }

    @Test
    void testRun_When676LinesFilePrefix_ShouldGenerateCorrectFilenames() throws Exception {
        Files.writeString(testFile.toPath(), generateString(676));
        String[] args = { LINES_OPTION, "1", TEST_FILENAME, "prefix_" };
        assertDoesNotThrow(() -> splitApp.run(args, testStream, null));
        File firstFile = new File(TEST_DIRNAME + File.separator + "prefix_aa");
        File lastFile = new File(TEST_DIRNAME + File.separator + "prefix_zz");
        assertTrue(firstFile.exists());
        assertTrue(lastFile.exists());
    }

    @Test
    void testRun_When1353BytesStdinPrefix_ShouldGenerateCorrectFilenames() {
        byte[] testContent = generateBytes(1353);
        testStream = generateStream(testContent);
        String[] args = { BYTES_OPTION, "1" };
        assertDoesNotThrow(() -> splitApp.run(args, testStream, null));
        File firstFile = new File(TEST_DIRNAME + File.separator + "xzaa");
        File lastFile = new File(TEST_DIRNAME + File.separator + "xzzaa");
        assertTrue(firstFile.exists());
        assertTrue(lastFile.exists());
    }

    @Test
    void testRun_When1LineStdin_ShouldNotSplit() throws Exception {
        String expectedContent = generateString(1);
        testStream = generateStream(expectedContent);
        String[] args = {};
        assertDoesNotThrow(() -> splitApp.run(args, testStream, null));
        File file = new File(TEST_DIRNAME + File.separator + XAA);
        File overflowFile = new File(TEST_DIRNAME + File.separator + XAB);
        assertTrue(file.exists());
        assertFalse(overflowFile.exists());
        String actualContent = new String(Files.readAllBytes(file.toPath()));
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void testRun_When1000LinesFile_ShouldNotSplit() throws Exception {
        String expectedContent = generateString(1000);
        Files.writeString(testFile.toPath(), expectedContent);
        String[] args = { TEST_FILENAME };
        assertDoesNotThrow(() -> splitApp.run(args, testStream, null));
        File file = new File(TEST_DIRNAME + File.separator + XAA);
        File overflowFile = new File(TEST_DIRNAME + File.separator + XAB);
        assertTrue(file.exists());
        assertFalse(overflowFile.exists());
        String actualContent = new String(Files.readAllBytes(file.toPath()));
        assertEquals(expectedContent, actualContent);
    }

    @Test
    void testRun_When1500LinesStdin_ShouldGet2Splits() throws Exception {
        testStream = generateStream(generateString(1500));
        String[] args = {};
        assertDoesNotThrow(() -> splitApp.run(args, testStream, null));
        File firstFile = new File(TEST_DIRNAME + File.separator + XAA);
        File lastFile = new File(TEST_DIRNAME + File.separator + XAB);
        File overflowFile = new File(TEST_DIRNAME + File.separator + XAC);
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
    void testRun_When4000LinesFile_ShouldGet4Splits() throws Exception {
        Files.writeString(testFile.toPath(), generateString(4000));
        String[] args = { TEST_FILENAME };
        assertDoesNotThrow(() -> splitApp.run(args, testStream, null));
        File firstFile = new File(TEST_DIRNAME + File.separator + XAA);
        File lastFile = new File(TEST_DIRNAME + File.separator + XAD);
        File overflowFile = new File(TEST_DIRNAME + File.separator + XAE);
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
    void testRun_When3LinesOption8LinesStdin_ShouldGet3Splits() throws Exception {
        testStream = generateStream(generateString(8));
        String[] args = { LINES_OPTION, "3" };
        assertDoesNotThrow(() -> splitApp.run(args, testStream, null));
        File firstFile = new File(TEST_DIRNAME + File.separator + XAA);
        File lastFile = new File(TEST_DIRNAME + File.separator + XAC);
        File overflowFile = new File(TEST_DIRNAME + File.separator + XAD);
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
    void testRun_When20LinesOption100LinesFile_ShouldGet5Splits() throws Exception {
        Files.writeString(testFile.toPath(), generateString(100));
        String[] args = { LINES_OPTION, "20", TEST_FILENAME };
        assertDoesNotThrow(() -> splitApp.run(args, testStream, null));
        File firstFile = new File(TEST_DIRNAME + File.separator + XAA);
        File lastFile = new File(TEST_DIRNAME + File.separator + XAE);
        File overflowFile = new File(TEST_DIRNAME + File.separator + XAF);
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
    void testRun_When16BytesOption46BytesStdin_ShouldGet3Splits() throws Exception {
        byte[] testContent = generateBytes(46);
        testStream = generateStream(testContent);
        String[] args = { BYTES_OPTION, "16" };
        assertDoesNotThrow(() -> splitApp.run(args, testStream, null));
        File firstFile = new File(TEST_DIRNAME + File.separator + XAA);
        File lastFile = new File(TEST_DIRNAME + File.separator + XAC);
        File overflowFile = new File(TEST_DIRNAME + File.separator + XAD);
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
    void testRun_When8BytesOption16BytesFile_ShouldGet2Splits() throws Exception {
        byte[] testContent = generateBytes(16);
        Files.write(testFile.toPath(), testContent);
        String[] args = { BYTES_OPTION, "8", TEST_FILENAME };
        assertDoesNotThrow(() -> splitApp.run(args, testStream, null));
        File firstFile = new File(TEST_DIRNAME + File.separator + XAA);
        File lastFile = new File(TEST_DIRNAME + File.separator + XAB);
        File overflowFile = new File(TEST_DIRNAME + File.separator + XAC);
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
    void testRun_When16bBytesOption2048BytesStdin_ShouldGet2Splits() throws Exception {
        byte[] testContent = generateBytes(16384);
        testStream = generateStream(testContent);
        String[] args = { BYTES_OPTION, "16b" };
        assertDoesNotThrow(() -> splitApp.run(args, testStream, null));
        File firstFile = new File(TEST_DIRNAME + File.separator + XAA);
        File lastFile = new File(TEST_DIRNAME + File.separator + XAB);
        File overflowFile = new File(TEST_DIRNAME + File.separator + XAC);
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
    void testRun_When2kBytesOption8000BytesFile_ShouldGet4Splits() throws Exception {
        byte[] testContent = generateBytes(8000);
        Files.write(testFile.toPath(), testContent);
        String[] args = { BYTES_OPTION, "2k", TEST_FILENAME };
        assertDoesNotThrow(() -> splitApp.run(args, testStream, null));
        File firstFile = new File(TEST_DIRNAME + File.separator + XAA);
        File lastFile = new File(TEST_DIRNAME + File.separator + XAD);
        File overflowFile = new File(TEST_DIRNAME + File.separator + XAE);
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
    void testRun_When1mBytesOption2000000BytesStdin_ShouldGet2Splits() throws Exception {
        byte[] testContent = generateBytes(2000000);
        testStream = generateStream(testContent);
        String[] args = { BYTES_OPTION, "1m" };
        assertDoesNotThrow(() -> splitApp.run(args, testStream, null));
        File firstFile = new File(TEST_DIRNAME + File.separator + XAA);
        File lastFile = new File(TEST_DIRNAME + File.separator + XAB);
        File overflowFile = new File(TEST_DIRNAME + File.separator + XAC);
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