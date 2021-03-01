package ef2;


import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.impl.app.UniqApplication;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;

@Disabled
public class UniqApplicationTest {

    private static final String ORIGINAL_DIR = Environment.currentDirectory;
    private static final String TESTDIR = Environment.currentDirectory + File.separator + RESOURCES_PATH + File.separator + "UniqApplicationTest";

    private static final String INPUT_1 = "Hello World" + STRING_NEWLINE + "Hello World"
                                        + "Alice" + STRING_NEWLINE + "Alice" + STRING_NEWLINE
                                        + "Bob" + STRING_NEWLINE + "Alice" + STRING_NEWLINE + "Bob";
    private static final String INPUT_2 = "AAA" + STRING_NEWLINE + "BBB" + STRING_NEWLINE + "CCC";
    private static final String INPUT_3 = "AAA" + STRING_NEWLINE + "BBB" + STRING_NEWLINE + "BBB"
                                        + STRING_NEWLINE + "AAA" + STRING_NEWLINE + "AAA";

    private static final String INPUT_EMPTY = "";

    private static final String INPUT_FILE_1 = "in1.txt";
    private static final String INPUT_FILE_2 = "in2.txt";
    private static final String INPUT_FILE_3 = "in3.txt";
    private static final String INPUT_FILE_4 = "in4.txt"; // does not exist

    private static final String OUTPUT_FILE_1 = "out1.txt";
    private static final String OUTPUT_FILE_2 = "out2.txt";
    private static final String OUTPUT_FILE_3 = "out3.txt"; // does not exist

    private final Path in1 = Paths.get(TESTDIR, INPUT_FILE_1);
    private final Path in2 = Paths.get(TESTDIR, INPUT_FILE_2);
    private final Path in3 = Paths.get(TESTDIR, INPUT_FILE_3);
    private final Path in4 = Paths.get(TESTDIR, INPUT_FILE_4);

    private final Path out1 = Paths.get(TESTDIR, OUTPUT_FILE_1);
    private final Path out2 = Paths.get(TESTDIR, OUTPUT_FILE_2);
    private final Path out3 = Paths.get(TESTDIR, OUTPUT_FILE_3);


    private final List<Path> paths = List.of(in1, in2, in3, in4, out1, out2, out3);
    private InputStream inputStream;
    private UniqApplication app;

    private final ByteArrayOutputStream STD_OUTPUT = new ByteArrayOutputStream();

    private void createFileWithContent(Path path, String content) throws IOException {
        Files.createFile(path);
        BufferedWriter outputStream = new BufferedWriter(new FileWriter(path.toFile(), true));//NOPMD
        outputStream.append(content);
        outputStream.close();
    }

    private String readFromFile(String path) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));//NOPMD
            String line;
            List<String> result = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
            reader.close();
            return String.join(STRING_NEWLINE, result);
        } catch (Exception e) {
            return e.getMessage();
        }
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
    void setup() throws IOException {
        app = spy(new UniqApplication());

        Files.createFile(out1);
        Files.createFile(out2);

        createFileWithContent(in1, INPUT_1);
        createFileWithContent(in2, INPUT_2);
        createFileWithContent(in3, INPUT_3);

        System.setOut(new PrintStream(STD_OUTPUT));
    }

    @AfterEach
    void tearDown() throws IOException {
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
        System.setErr(System.out);
    }

    @Test
    public void uniqFromFile_FileDoesNotExit_ThrowsException() {
        assertThrows(Exception.class, () -> app.uniqFromFile(false, false, false, INPUT_FILE_4, OUTPUT_FILE_1));
    }

    @Test
    public void uniqFromFile_FileNotWritable_ThrowsException() {
        out1.toFile().setWritable(false);

        assertThrows(Exception.class, () -> app.uniqFromFile(false, false, false, INPUT_FILE_2, OUTPUT_FILE_1));
    }

    @Test
    public void uniqFromFile_NoFlag_RemovesAdjacentDup() throws AbstractApplicationException {
        String result1 = app.uniqFromFile(false, false, false, INPUT_FILE_1, OUTPUT_FILE_1);
        String expected1 = "Hello World" + STRING_NEWLINE + "Alice" + STRING_NEWLINE + "Bob" + STRING_NEWLINE + "Alice"
                        + STRING_NEWLINE + "Bob" + STRING_NEWLINE;

        String result2 = app.uniqFromFile(false, false, false, INPUT_FILE_2, OUTPUT_FILE_2);
        String expected2 = "AAA" + STRING_NEWLINE + "BBB" + STRING_NEWLINE + "CCC" + STRING_NEWLINE;

        String result3 = app.uniqFromFile(false, false, false, INPUT_FILE_3, OUTPUT_FILE_3);
        String expected3 = "AAA" + STRING_NEWLINE + "BBB" + STRING_NEWLINE + "AAA" + STRING_NEWLINE;

        assertEquals(expected1, result1);
        assertEquals(expected2, result2);
        assertEquals(expected3, result3);

        assertEquals(expected1, readFromFile(OUTPUT_FILE_1));
        assertEquals(expected2, readFromFile(OUTPUT_FILE_2));
        assertEquals(expected3, readFromFile(OUTPUT_FILE_3));
    }

    @Test
    public void uniqFromFile_NoFlagNoOutputFile_WriteToStdout() throws AbstractApplicationException {
        String result1 = app.uniqFromFile(false, false, false, INPUT_FILE_1, null);
        String expected1 = "Hello World" + STRING_NEWLINE + "Alice" + STRING_NEWLINE + "Bob" + STRING_NEWLINE + "Alice"
                + STRING_NEWLINE + "Bob" + STRING_NEWLINE;

        assertEquals(expected1, result1);
        assertEquals(expected1, STD_OUTPUT.toString());

    }

    @Test
    public void uniqFromFile_IsCount_RemovesAdjacentDupWithPrefix() throws AbstractApplicationException {
        String result1 = app.uniqFromFile(true, false, false, INPUT_FILE_1, OUTPUT_FILE_1);
        String expected1 = "2 Hello World" + STRING_NEWLINE + "2 Alice" + STRING_NEWLINE + "1 Bob" + STRING_NEWLINE + "1 Alice"
                + STRING_NEWLINE + "1 Bob" + STRING_NEWLINE;

        String result2 = app.uniqFromFile(true, false, false, INPUT_FILE_2, OUTPUT_FILE_2);
        String expected2 = "1 AAA" + STRING_NEWLINE + "1 BBB" + STRING_NEWLINE + "1 CCC" + STRING_NEWLINE;

        String result3 = app.uniqFromFile(true, false, false, INPUT_FILE_3, OUTPUT_FILE_3);
        String expected3 = "1 AAA" + STRING_NEWLINE + "2 BBB" + STRING_NEWLINE + "2 AAA" + STRING_NEWLINE;

        assertEquals(expected1, result1);
        assertEquals(expected2, result2);
        assertEquals(expected3, result3);

        assertEquals(expected1, readFromFile(OUTPUT_FILE_1));
        assertEquals(expected2, readFromFile(OUTPUT_FILE_2));
        assertEquals(expected3, readFromFile(OUTPUT_FILE_3));
    }

    @Test
    public void uniqFromFile_IsRepeated_DisplaysOnlyDup() throws AbstractApplicationException {
        String result1 = app.uniqFromFile(false, true, false, INPUT_FILE_1, OUTPUT_FILE_1);
        String expected1 = "Hello World" + STRING_NEWLINE + "Alice" + STRING_NEWLINE;

        String result2 = app.uniqFromFile(false, true, false, INPUT_FILE_2, OUTPUT_FILE_2);

        String result3 = app.uniqFromFile(false, true, false, INPUT_FILE_3, OUTPUT_FILE_3);
        String expected3 = "BBB" + STRING_NEWLINE + "AAA" + STRING_NEWLINE;

        assertEquals(expected1, result1);
        assertEquals(STRING_NEWLINE, result2);
        assertEquals(expected3, result3);

        assertEquals(expected1, readFromFile(OUTPUT_FILE_1));
        assertEquals(STRING_NEWLINE, readFromFile(OUTPUT_FILE_2));
        assertEquals(expected3, readFromFile(OUTPUT_FILE_3));
    }

    @Test
    public void uniqFromFile_IsRepeatedAndIsAllRepeated_DisplaysAllDup() throws AbstractApplicationException {
        String result1 = app.uniqFromFile(false, true, true, INPUT_FILE_1, OUTPUT_FILE_1);
        String expected1 = "Hello World" + STRING_NEWLINE + "Hello World" + STRING_NEWLINE + "Alice" + STRING_NEWLINE
                + "Alice" + STRING_NEWLINE;
        String result2 = app.uniqFromFile(false, true, true, INPUT_FILE_2, OUTPUT_FILE_2);
        String result3 = app.uniqFromFile(false, true, true, INPUT_FILE_3, OUTPUT_FILE_3);
        String expected3 = "BBB" + STRING_NEWLINE + "BBB" + STRING_NEWLINE + "AAA" + STRING_NEWLINE
                + "AAA" + STRING_NEWLINE;

        assertEquals(expected1, result1);
        assertEquals(STRING_NEWLINE, result2);
        assertEquals(expected3, result3);

        assertEquals(expected1, readFromFile(OUTPUT_FILE_1));
        assertEquals(STRING_NEWLINE, readFromFile(OUTPUT_FILE_2));
        assertEquals(expected3, readFromFile(OUTPUT_FILE_3));

    }

    @Test
    public void uniqFromFile_IsAllRepeated_DisplaysAllDup() throws AbstractApplicationException {

        String expected1 = "Hello World" + STRING_NEWLINE + "Hello World" + STRING_NEWLINE + "Alice" + STRING_NEWLINE
                        + "Alice" + STRING_NEWLINE;
        String expected3 = "BBB" + STRING_NEWLINE + "BBB" + STRING_NEWLINE + "AAA" + STRING_NEWLINE
                        + "AAA" + STRING_NEWLINE;

        String result1 = app.uniqFromFile(false, false, true, INPUT_FILE_1, OUTPUT_FILE_1);
        String result2 = app.uniqFromFile(false, false, true, INPUT_FILE_2, OUTPUT_FILE_2);
        String result3 = app.uniqFromFile(false, false, true, INPUT_FILE_3, OUTPUT_FILE_3);

        assertEquals(expected1, result1);
        assertEquals(STRING_NEWLINE, result2);
        assertEquals(expected3, result3);

        assertEquals(expected1, readFromFile(OUTPUT_FILE_1));
        assertEquals(STRING_NEWLINE, readFromFile(OUTPUT_FILE_2));
        assertEquals(expected3, readFromFile(OUTPUT_FILE_3));
    }

    @Test
    public void uniqFromFile_IsCountAndIsRepeated_DisplaysOnlyDupWithPrefix() throws AbstractApplicationException {
        String result1 = app.uniqFromFile(true, true, false, INPUT_FILE_1, OUTPUT_FILE_1);
        String expected1 = "2 Hello World" + STRING_NEWLINE + "2 Alice" + STRING_NEWLINE;

        String result2 = app.uniqFromFile(true, true, false, INPUT_FILE_2, OUTPUT_FILE_1);

        String result3 = app.uniqFromFile(true, true, false, INPUT_FILE_3, OUTPUT_FILE_1);
        String expected3 = "2 BBB" + STRING_NEWLINE + "2 AAA" + STRING_NEWLINE;

        assertEquals(expected1, result1);
        assertEquals(STRING_NEWLINE, result2);
        assertEquals(expected3, result3);
    }

    @Test
    // printing all duplicated lines and repeat counts is meaningless
    public void uniqFromFile_IsCountAndIsAllRepeated_ShouldThrowException() {
        assertThrows(Exception.class, () -> app.uniqFromFile(true, false, true, INPUT_FILE_1, OUTPUT_FILE_1));
    }

    @Test
    // printing all duplicated lines and repeat counts is meaningless
    public void uniqFromFile_IsCountAndIsRepeatedIsAllRepeated_ShouldThrowException() {
        assertThrows(Exception.class, () -> app.uniqFromFile(true, true, true, INPUT_FILE_1, OUTPUT_FILE_1));
    }

    @Test
    public void uniqFromStdin_NoInputStream_ShouldThrowException() {
        assertThrows(Exception.class, () -> app.uniqFromStdin(true, false, true, null, OUTPUT_FILE_1));
    }

    @Test
    public void uniqFromStdin_NoFlag_RemovesAdjacentDup() throws AbstractApplicationException {
        inputStream = new ByteArrayInputStream(INPUT_1.getBytes());
        String result1 = app.uniqFromStdin(false, false, false, inputStream, OUTPUT_FILE_1);
        String expected1 = "Hello World" + STRING_NEWLINE + "Alice" + STRING_NEWLINE + "Bob" + STRING_NEWLINE + "Alice"
                + STRING_NEWLINE + "Bob" + STRING_NEWLINE;

        inputStream = new ByteArrayInputStream(INPUT_2.getBytes());
        String result2 = app.uniqFromStdin(false, false, false, inputStream, OUTPUT_FILE_2);
        String expected2 = "AAA" + STRING_NEWLINE + "BBB" + STRING_NEWLINE + "CCC" + STRING_NEWLINE;

        inputStream = new ByteArrayInputStream(INPUT_3.getBytes());
        String result3 = app.uniqFromStdin(false, false, false, inputStream, OUTPUT_FILE_3);
        String expected3 = "AAA" + STRING_NEWLINE + "BBB" + STRING_NEWLINE + "AAA" + STRING_NEWLINE;

        assertEquals(expected1, result1);
        assertEquals(expected2, result2);
        assertEquals(expected3, result3);

        assertEquals(expected1, readFromFile(OUTPUT_FILE_1));
        assertEquals(expected2, readFromFile(OUTPUT_FILE_2));
        assertEquals(expected3, readFromFile(OUTPUT_FILE_3));
    }

    @Test
    public void uniqFromStdin_NoOutputFile_ShouldWriteToStdout() throws AbstractApplicationException {
        inputStream = new ByteArrayInputStream(INPUT_1.getBytes());
        String result = app.uniqFromStdin(true, false, true, inputStream, null);
        String expected1 = "Hello World" + STRING_NEWLINE + "Alice" + STRING_NEWLINE + "Bob" + STRING_NEWLINE + "Alice"
                + STRING_NEWLINE + "Bob" + STRING_NEWLINE;

        assertEquals(expected1, STD_OUTPUT.toString());
    }

    @Test
    public void run_InvalidOption_ThrowsException() {
        assertThrows(Exception.class, () -> app.run(new String[]{INPUT_FILE_1, "-r"}, System.in, System.out));
    }

    @Test
    public void run_NoInputStream_ThrowsException() {
        assertThrows(Exception.class, () -> app.run(new String[]{}, null, System.out));
    }

    @Test
    public void run_NoOutputStream_ThrowsException() {
        assertThrows(Exception.class, () -> app.run(new String[]{}, System.in, null));
    }

    @Test
    public void run_NoFlagReadAndWriteToFile_RemovesAdjacentDup() throws AbstractApplicationException {
        app.run(new String[]{INPUT_FILE_1, OUTPUT_FILE_1}, System.in, System.out);
        String expected = "Hello World" + STRING_NEWLINE + "Alice" + STRING_NEWLINE + "Bob" + STRING_NEWLINE + "Alice"
                + STRING_NEWLINE + "Bob" + STRING_NEWLINE;

        assertNotEquals(expected, STD_OUTPUT.toString());
        assertEquals(expected, readFromFile(OUTPUT_FILE_1));
    }

    @Test
    public void run_NoFlagWriteToStdout_RemovesAdjacentDup() throws AbstractApplicationException {
        app.run(new String[]{INPUT_FILE_1}, System.in, System.out);
        String expected = "Hello World" + STRING_NEWLINE + "Alice" + STRING_NEWLINE + "Bob" + STRING_NEWLINE + "Alice"
                + STRING_NEWLINE + "Bob" + STRING_NEWLINE;

        assertEquals(expected, STD_OUTPUT.toString());
    }

    @Test
    public void run_NoFlagAndOutputFilenameIsDash_RemovesAdjacentDup() throws AbstractApplicationException {
        app.run(new String[]{INPUT_FILE_1, "-"}, System.in, System.out);
        String expected = "Hello World" + STRING_NEWLINE + "Alice" + STRING_NEWLINE + "Bob" + STRING_NEWLINE + "Alice"
                + STRING_NEWLINE + "Bob" + STRING_NEWLINE;

        assertEquals(expected, STD_OUTPUT.toString());
    }

    @Test
    public void run_IsCountWriteToStdout_RemovesAdjacentDupWithPrefix() throws AbstractApplicationException {
        app.run(new String[]{INPUT_FILE_1, "-c"}, System.in, System.out);
        String expected = "2 Hello World" + STRING_NEWLINE + "2 Alice" + STRING_NEWLINE + "1 Bob" + STRING_NEWLINE + "1 Alice"
                + STRING_NEWLINE + "1 Bob" + STRING_NEWLINE;

        assertEquals(expected, STD_OUTPUT.toString());
    }

    @Test
    public void run_IsRepeatedWriteToStdout_DisplaysOnlyDup() throws AbstractApplicationException {
        app.run(new String[]{INPUT_FILE_1, "-d"}, System.in, System.out);
        String expected = "Hello World" + STRING_NEWLINE + "Alice" + STRING_NEWLINE;

        assertEquals(expected, STD_OUTPUT.toString());
    }

    @Test
    public void run_IsAllRepeatedWriteToStdout_DisplaysAllDup() throws AbstractApplicationException {
        app.run(new String[]{INPUT_FILE_1, "-D"}, System.in, System.out);
        String expected = "Hello World" + STRING_NEWLINE + "Hello World" + STRING_NEWLINE + "Alice" + STRING_NEWLINE
                + "Alice" + STRING_NEWLINE;

        assertEquals(expected, STD_OUTPUT.toString());
    }

    @Test
    public void run_IsRepeatedAndIsCountWriteToStdout_DisplaysOnlyDupWithPrefix() throws AbstractApplicationException {
        app.run(new String[]{INPUT_FILE_1, "-d", "-c"}, System.in, System.out);
        String expected = "2 Hello World" + STRING_NEWLINE + "2 Alice" + STRING_NEWLINE;

        assertEquals(expected, STD_OUTPUT.toString());
    }

    @Test
    public void run_IsRepeatedAndIsAllRepeatedWriteToStdout_DisplaysOnlyDupWithPrefix() throws AbstractApplicationException {
        app.run(new String[]{INPUT_FILE_1, "-d", "-D"}, System.in, System.out);
        String expected = "Hello World" + STRING_NEWLINE + "Hello World" + STRING_NEWLINE + "Alice" + STRING_NEWLINE
                + "Alice" + STRING_NEWLINE;

        assertEquals(expected, STD_OUTPUT.toString());
    }

    @Test
    public void run_ReadFromStdinBlank_DisplaysBlank() throws AbstractApplicationException {
        inputStream = new ByteArrayInputStream(INPUT_EMPTY.getBytes());
        app.run(new String[]{}, inputStream, System.out);

        assertEquals(STRING_NEWLINE, STD_OUTPUT.toString());
    }

}
