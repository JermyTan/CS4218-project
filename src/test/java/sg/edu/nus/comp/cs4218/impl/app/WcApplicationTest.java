package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.WcException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FLAG_PREFIX;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;

class WcApplicationTest {

    private static final String BYTES_LETTER = "c";
    private static final String LINES_LETTER = "l";
    private static final String WORDS_LETTER = "w";
    private static final String BYTES_FLAG = CHAR_FLAG_PREFIX + BYTES_LETTER;
    private static final String LINES_FLAG = CHAR_FLAG_PREFIX + LINES_LETTER;
    private static final String WORDS_FLAG = CHAR_FLAG_PREFIX + WORDS_LETTER;
    private static final String REGEX = "\\s+";

    private static final String DEFAULT_DIRNAME = Environment.currentDirectory;
    private static final String TEST_DIRNAME = Environment.currentDirectory + File.separator + RESOURCES_PATH + File.separator + "WcApplicationTest";
    private static final String TEST_FILENAME_1 = "test1.txt";
    private static final String TEST_FILENAME_2 = "test2.txt";
    private static final String TEST_FILENAME_3 = "test3.txt";
    private static final String TEST_STRING = "The quick brown fox jumped over the lazy dog.\n"; // 9 words 46 bytes

    private static File testDir;
    private static InputStream testInputStream;
    private static OutputStream testOutputStream;

    private final WcApplication wcApp = new WcApplication();

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

    private String generateString(int lines) {
        return TEST_STRING.repeat(Math.max(0, lines));
    }

    private InputStream createInputStream(String string) {
        return new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
    }

    private int getWordCount(int lines) {
        return lines * 9;
    }

    private int getByteCount(int lines) {
        return lines * 46;
    }

    @BeforeEach
    void setUp() throws Exception {
        testInputStream = createInputStream(generateString(0));
        testOutputStream = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws Exception {
        for (File file : testDir.listFiles()) {
            file.delete();
        }
        testInputStream.close();
        testOutputStream.close();
    }

    @Test
    void testRun_WhenNullArgs_ShouldThrowException() {
        String[] args = {null};
        assertThrows(WcException.class, () -> wcApp.run(args, testInputStream, testOutputStream));
    }

    @Test
    void testRun_WhenIllegalFlagWrongLetter_ShouldThrowException() {
        String[] args = {"-a"};
        assertThrows(WcException.class,
                () -> wcApp.run(args, testInputStream, testOutputStream));
    }

    @Test
    void testRun_WhenIllegalFlagUppercaseOfLegalLetter_ShouldThrowException() {
        String[] args = {"-C"};
        assertThrows(WcException.class,
                () -> wcApp.run(args, testInputStream, testOutputStream));
    }

    @Test
    void testRun_WhenFilenameDash_ShouldUseStdin() {
        int testLines = 1;
        testInputStream = createInputStream(generateString(testLines));

        String[] args = {"-"};
        assertDoesNotThrow(() -> wcApp.run(args, testInputStream, testOutputStream));
        String[] result = testOutputStream.toString().split(REGEX);

        int lines = Integer.parseInt(result[0]);
        int words = Integer.parseInt(result[1]);
        int bytes = Integer.parseInt(result[2]);
        assertEquals(testLines, lines);
        assertEquals(getWordCount(testLines), words);
        assertEquals(getByteCount(testLines), bytes);
    }

    @Test
    void testRun_WhenStdin_ShouldCountLinesWordsBytes() {
        int testLines = 1;
        testInputStream = createInputStream(generateString(testLines));

        String[] args = {};
        assertDoesNotThrow(() -> wcApp.run(args, testInputStream, testOutputStream));
        String[] result = testOutputStream.toString().split(REGEX);

        int lines = Integer.parseInt(result[0]);
        int words = Integer.parseInt(result[1]);
        int bytes = Integer.parseInt(result[2]);
        assertEquals(testLines, lines);
        assertEquals(getWordCount(testLines), words);
        assertEquals(getByteCount(testLines), bytes);
    }

    @Test
    void testRun_WhenStdinLinesFlag_ShouldCountLines() {
        int testLines = 10;
        testInputStream = createInputStream(generateString(testLines));

        String[] args = {LINES_FLAG};
        assertDoesNotThrow(() -> wcApp.run(args, testInputStream, testOutputStream));
        String[] result = testOutputStream.toString().split(REGEX);

        int lines = Integer.parseInt(result[0]);
        assertEquals(testLines, lines);
    }

    @Test
    void testCountFromStdin_WhenLinesWords_ShouldCountLinesWords() {
        int testLines = 10;
        testInputStream = createInputStream(generateString(testLines));

        String output = assertDoesNotThrow(() -> wcApp.countFromStdin(false, true, true, testInputStream));
        String[] result = output.split(REGEX);

        int lines = Integer.parseInt(result[0]);
        int words = Integer.parseInt(result[1]);
        assertEquals(testLines, lines);
        assertEquals(getWordCount(testLines), words);
    }

    @Test
    void testRun_WhenStdinBytesWordsFlagsTogether_ShouldCountWordsBytes() {
        int testLines = 10;
        testInputStream = createInputStream(generateString(testLines));

        String[] args = {CHAR_FLAG_PREFIX + BYTES_LETTER + WORDS_LETTER};
        assertDoesNotThrow(() -> wcApp.run(args, testInputStream, testOutputStream));
        String[] result = testOutputStream.toString().split(REGEX);

        int words = Integer.parseInt(result[0]);
        int bytes = Integer.parseInt(result[1]);
        assertEquals(getWordCount(testLines), words);
        assertEquals(getByteCount(testLines), bytes);
    }

    @Test
    void testCountFromFiles_WhenOneFileLinesWords_ShouldCountLinesWords() throws Exception {
        int testLines = 10;
        File testFile = new File(TEST_DIRNAME + File.separator + TEST_FILENAME_1);
        testFile.createNewFile();
        Files.writeString(testFile.toPath(), generateString(testLines));

        String output = assertDoesNotThrow(() -> wcApp.countFromFiles(false, true, true, TEST_FILENAME_1));
        String[] result = output.split(REGEX);

        int lines = Integer.parseInt(result[0]);
        int words = Integer.parseInt(result[1]);
        assertEquals(testLines, lines);
        assertEquals(getWordCount(testLines), words);
    }

    @Test
    void testCountFromFiles_WhenMultipleFiles_ShouldCountLinesWordsBytes() throws Exception {
        int testLines1 = 2;
        File testFile1 = new File(TEST_DIRNAME + File.separator + TEST_FILENAME_1);
        testFile1.createNewFile();
        Files.writeString(testFile1.toPath(), generateString(testLines1));

        int testLines2 = 4;
        File testFile2 = new File(TEST_DIRNAME + File.separator + TEST_FILENAME_2);
        testFile2.createNewFile();
        Files.writeString(testFile2.toPath(), generateString(testLines2));

        int testLines3 = 6;
        File testFile3 = new File(TEST_DIRNAME + File.separator + TEST_FILENAME_3);
        testFile3.createNewFile();
        Files.writeString(testFile3.toPath(), generateString(testLines3));

        String output = assertDoesNotThrow(() -> wcApp.countFromFiles(true, true, true, TEST_FILENAME_1, TEST_FILENAME_2, TEST_FILENAME_3));
        String[] result = output.split(REGEX);

        int lines1 = Integer.parseInt(result[0]);
        int words1 = Integer.parseInt(result[1]);
        int bytes1 = Integer.parseInt(result[2]);
        assertEquals(testLines1, lines1);
        assertEquals(getWordCount(testLines1), words1);
        assertEquals(getByteCount(testLines1), bytes1);

        int lines2 = Integer.parseInt(result[4]);
        int words2 = Integer.parseInt(result[5]);
        int bytes2 = Integer.parseInt(result[6]);
        assertEquals(testLines2, lines2);
        assertEquals(getWordCount(testLines2), words2);
        assertEquals(getByteCount(testLines2), bytes2);

        int lines3 = Integer.parseInt(result[8]);
        int words3 = Integer.parseInt(result[9]);
        int bytes3 = Integer.parseInt(result[10]);
        assertEquals(testLines3, lines3);
        assertEquals(getWordCount(testLines3), words3);
        assertEquals(getByteCount(testLines3), bytes3);

        int linesTotal = Integer.parseInt(result[12]);
        int wordsTotal = Integer.parseInt(result[13]);
        int bytesTotal = Integer.parseInt(result[14]);
        assertEquals(testLines1 + testLines2 + testLines3, linesTotal);
        assertEquals(getWordCount(testLines1) + getWordCount(testLines2) + getWordCount(testLines3), wordsTotal);
        assertEquals(getByteCount(testLines1) + getByteCount(testLines2) + getByteCount(testLines3), bytesTotal);
    }

    @Test
    void testRun_WhenMultipleFilesWordsFlag_ShouldCountWords() throws Exception {
        int testLines1 = 2;
        File testFile1 = new File(TEST_DIRNAME + File.separator + TEST_FILENAME_1);
        testFile1.createNewFile();
        Files.writeString(testFile1.toPath(), generateString(testLines1));

        int testLines2 = 4;
        File testFile2 = new File(TEST_DIRNAME + File.separator + TEST_FILENAME_2);
        testFile2.createNewFile();
        Files.writeString(testFile2.toPath(), generateString(testLines2));

        int testLines3 = 6;
        File testFile3 = new File(TEST_DIRNAME + File.separator + TEST_FILENAME_3);
        testFile3.createNewFile();
        Files.writeString(testFile3.toPath(), generateString(testLines3));

        String[] args = {WORDS_FLAG, TEST_FILENAME_1, TEST_FILENAME_2, TEST_FILENAME_3};
        assertDoesNotThrow(() -> wcApp.run(args, testInputStream, testOutputStream));
        String[] result = testOutputStream.toString().split(REGEX);

        int words1 = Integer.parseInt(result[0]);
        assertEquals(getWordCount(testLines1), words1);

        int words2 = Integer.parseInt(result[2]);
        assertEquals(getWordCount(testLines2), words2);

        int words3 = Integer.parseInt(result[4]);
        assertEquals(getWordCount(testLines3), words3);

        int wordsTotal = Integer.parseInt(result[6]);
        assertEquals(getWordCount(testLines1) + getWordCount(testLines2) + getWordCount(testLines3), wordsTotal);
    }

    @Test
    void testCountFromFiles_WhenMultipleFilesWordsFlag_ShouldCountWords() throws Exception {
        int testLines1 = 2;
        File testFile1 = new File(TEST_DIRNAME + File.separator + TEST_FILENAME_1);
        testFile1.createNewFile();
        Files.writeString(testFile1.toPath(), generateString(testLines1));

        int testLines2 = 4;
        File testFile2 = new File(TEST_DIRNAME + File.separator + TEST_FILENAME_2);
        testFile2.createNewFile();
        Files.writeString(testFile2.toPath(), generateString(testLines2));

        int testLines3 = 6;
        File testFile3 = new File(TEST_DIRNAME + File.separator + TEST_FILENAME_3);
        testFile3.createNewFile();
        Files.writeString(testFile3.toPath(), generateString(testLines3));

        String output = assertDoesNotThrow(() -> wcApp.countFromFiles(false, false, true, TEST_FILENAME_1, TEST_FILENAME_2, TEST_FILENAME_3));
        String[] result = output.split(REGEX);

        int words1 = Integer.parseInt(result[0]);
        assertEquals(getWordCount(testLines1), words1);

        int words2 = Integer.parseInt(result[2]);
        assertEquals(getWordCount(testLines2), words2);

        int words3 = Integer.parseInt(result[4]);
        assertEquals(getWordCount(testLines3), words3);

        int wordsTotal = Integer.parseInt(result[6]);
        assertEquals(getWordCount(testLines1) + getWordCount(testLines2) + getWordCount(testLines3), wordsTotal);
    }

    @Test
    void testCountFromFileAndStdin_WhenMultipleFiles_ShouldCountLinesWordsBytes() throws Exception {
        int testLines1 = 2;
        File testFile1 = new File(TEST_DIRNAME + File.separator + TEST_FILENAME_1);
        testFile1.createNewFile();
        Files.writeString(testFile1.toPath(), generateString(testLines1));

        int testLines2 = 4;
        File testFile2 = new File(TEST_DIRNAME + File.separator + TEST_FILENAME_2);
        testFile2.createNewFile();
        Files.writeString(testFile2.toPath(), generateString(testLines2));

        int testLines3 = 6;
        File testFile3 = new File(TEST_DIRNAME + File.separator + TEST_FILENAME_3);
        testFile3.createNewFile();
        Files.writeString(testFile3.toPath(), generateString(testLines3));

        String output = assertDoesNotThrow(() -> wcApp.countFromFileAndStdin(true, true, true, testInputStream, TEST_FILENAME_1, TEST_FILENAME_2, TEST_FILENAME_3));
        String[] result = output.split(REGEX);

        int lines1 = Integer.parseInt(result[0]);
        int words1 = Integer.parseInt(result[1]);
        int bytes1 = Integer.parseInt(result[2]);
        assertEquals(testLines1, lines1);
        assertEquals(getWordCount(testLines1), words1);
        assertEquals(getByteCount(testLines1), bytes1);

        int lines2 = Integer.parseInt(result[4]);
        int words2 = Integer.parseInt(result[5]);
        int bytes2 = Integer.parseInt(result[6]);
        assertEquals(testLines2, lines2);
        assertEquals(getWordCount(testLines2), words2);
        assertEquals(getByteCount(testLines2), bytes2);

        int lines3 = Integer.parseInt(result[8]);
        int words3 = Integer.parseInt(result[9]);
        int bytes3 = Integer.parseInt(result[10]);
        assertEquals(testLines3, lines3);
        assertEquals(getWordCount(testLines3), words3);
        assertEquals(getByteCount(testLines3), bytes3);

        int linesTotal = Integer.parseInt(result[12]);
        int wordsTotal = Integer.parseInt(result[13]);
        int bytesTotal = Integer.parseInt(result[14]);
        assertEquals(testLines1 + testLines2 + testLines3, linesTotal);
        assertEquals(getWordCount(testLines1) + getWordCount(testLines2) + getWordCount(testLines3), wordsTotal);
        assertEquals(getByteCount(testLines1) + getByteCount(testLines2) + getByteCount(testLines3), bytesTotal);
    }
}