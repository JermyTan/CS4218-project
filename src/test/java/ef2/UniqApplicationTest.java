package ef2;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static sg.edu.nus.comp.cs4218.impl.util.IOUtils.getLinesFromInputStream;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_FLAG;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_MULTI_WORDS;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_SINGLE_WORD;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_UNICODE;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.impl.app.UniqApplication;

@Disabled
public class UniqApplicationTest {
    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = String.join(STRING_FILE_SEP,
            EnvironmentUtil.currentDirectory,
            RESOURCES_PATH,
            "UniqApplicationTest");

    private static final String INPUT_1 = String.join(STRING_NEWLINE,
            STRING_MULTI_WORDS,
            STRING_MULTI_WORDS,
            STRING_SINGLE_WORD,
            STRING_SINGLE_WORD,
            STRING_UNICODE,
            STRING_SINGLE_WORD,
            STRING_UNICODE);
    private static final String INPUT_2 = String.join(STRING_NEWLINE,
            STRING_SINGLE_WORD,
            STRING_MULTI_WORDS,
            STRING_UNICODE);
    private static final String INPUT_3 = String.join(STRING_NEWLINE,
            STRING_SINGLE_WORD,
            STRING_MULTI_WORDS,
            STRING_MULTI_WORDS,
            STRING_SINGLE_WORD,
            STRING_SINGLE_WORD);

    private static final String INPUT_FILE_1 = "in1.txt";
    private static final String INPUT_FILE_2 = "in2.txt";
    private static final String INPUT_FILE_3 = "in3.txt";
    private static final String INPUT_FILE_4 = "in4.txt"; // does not exist

    private static final String OUTPUT_FILE_1 = "out1.txt";
    private static final String OUTPUT_FILE_2 = "out2.txt";
    private static final String OUTPUT_FILE_3 = "out3.txt"; // does not exist

    private final Path in1 = Paths.get(TEST_DIR, INPUT_FILE_1);
    private final Path in2 = Paths.get(TEST_DIR, INPUT_FILE_2);
    private final Path in3 = Paths.get(TEST_DIR, INPUT_FILE_3);
    private final Path in4 = Paths.get(TEST_DIR, INPUT_FILE_4);

    private final Path out1 = Paths.get(TEST_DIR, OUTPUT_FILE_1);
    private final Path out2 = Paths.get(TEST_DIR, OUTPUT_FILE_2);
    private final Path out3 = Paths.get(TEST_DIR, OUTPUT_FILE_3);


    private final List<Path> paths = List.of(in1, in2, in3, in4, out1, out2, out3);
    private final ByteArrayOutputStream STD_OUTPUT = new ByteArrayOutputStream();
    private InputStream inputStream;
    private UniqApplication app;

    @BeforeAll
    static void setupBeforeAll() {
        EnvironmentUtil.currentDirectory = TEST_DIR;
    }

    @AfterAll
    static void tearDownAfterAll() {
        EnvironmentUtil.currentDirectory = ORIGINAL_DIR;
    }

    private void createFileWithContent(Path path, String content) throws IOException {
        Files.createFile(path);
        BufferedWriter outputStream = new BufferedWriter(new FileWriter(path.toFile(), true));//NOPMD
        outputStream.append(content);
        outputStream.close();
    }

    private String readFromFile(Path path) {
        try {
            List<String> result = getLinesFromInputStream(Files.newInputStream(path));
            return String.join(STRING_NEWLINE, result);
        } catch (Exception e) {
            return e.getMessage();
        }
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
    public void uniqFromFile_NoFlag_RemovesAdjacentDup() {
        assertDoesNotThrow(() -> {
            String result1 = app.uniqFromFile(false, false, false, INPUT_FILE_1, OUTPUT_FILE_1);
            String expected1 = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE);

            String result2 = app.uniqFromFile(false, false, false, INPUT_FILE_2, OUTPUT_FILE_2);
            String expected2 = String.join(STRING_NEWLINE,
                    STRING_SINGLE_WORD,
                    STRING_MULTI_WORDS,
                    STRING_UNICODE);

            String result3 = app.uniqFromFile(false, false, false, INPUT_FILE_3, OUTPUT_FILE_3);
            String expected3 = String.join(STRING_NEWLINE,
                    STRING_SINGLE_WORD,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD);

            assertEquals(expected1, result1);
            assertEquals(expected2, result2);
            assertEquals(expected3, result3);

            assertEquals(expected1, readFromFile(out1));
            assertEquals(expected2, readFromFile(out2));
            assertEquals(expected3, readFromFile(out3));
        });
    }

    @Test
    public void uniqFromFile_NoFlagNoOutputFile_WriteToStdout() {
        assertDoesNotThrow(() -> {
            String result = app.uniqFromFile(false, false, false, INPUT_FILE_1, null);
            String expected = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE);

            assertEquals(expected, result);
            assertEquals(expected, STD_OUTPUT.toString());
        });
    }

    @Test
    public void uniqFromFile_IsCount_RemovesAdjacentDupWithPrefix() {
        assertDoesNotThrow(() -> {
            String result1 = app.uniqFromFile(true, false, false, INPUT_FILE_1, OUTPUT_FILE_1);
            String expected1 = String.join(STRING_NEWLINE,
                    "2 " + STRING_MULTI_WORDS,
                    "2 " + STRING_SINGLE_WORD,
                    "1 " + STRING_UNICODE,
                    "1 " + STRING_SINGLE_WORD,
                    "1 " + STRING_UNICODE);

            String result2 = app.uniqFromFile(true, false, false, INPUT_FILE_2, OUTPUT_FILE_2);
            String expected2 = String.join(STRING_NEWLINE,
                    "1 " + STRING_SINGLE_WORD,
                    "1 " + STRING_MULTI_WORDS,
                    "1 " + STRING_UNICODE);

            String result3 = app.uniqFromFile(true, false, false, INPUT_FILE_3, OUTPUT_FILE_3);
            String expected3 = String.join(STRING_NEWLINE,
                    "1 " + STRING_SINGLE_WORD,
                    "2 " + STRING_MULTI_WORDS,
                    "2 " + STRING_SINGLE_WORD);

            assertEquals(expected1, result1);
            assertEquals(expected2, result2);
            assertEquals(expected3, result3);

            assertEquals(expected1, readFromFile(out1));
            assertEquals(expected2, readFromFile(out2));
            assertEquals(expected3, readFromFile(out3));
        });
    }

    @Test
    public void uniqFromFile_IsRepeated_DisplaysOnlyDup() {
        assertDoesNotThrow(() -> {
            String result1 = app.uniqFromFile(false, true, false, INPUT_FILE_1, OUTPUT_FILE_1);
            String expected1 = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD);

            String result2 = app.uniqFromFile(false, true, false, INPUT_FILE_2, OUTPUT_FILE_2);
            String expected2 = STRING_EMPTY;

            String result3 = app.uniqFromFile(false, true, false, INPUT_FILE_3, OUTPUT_FILE_3);
            String expected3 = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD);

            assertEquals(expected1, result1);
            assertEquals(expected2, result2);
            assertEquals(expected3, result3);

            assertEquals(expected1, readFromFile(out1));
            assertEquals(expected2, readFromFile(out2));
            assertEquals(expected3, readFromFile(out3));
        });
    }

    @Test
    public void uniqFromFile_IsRepeatedAndIsAllRepeated_DisplaysAllDup() {
        assertDoesNotThrow(() -> {
            String result1 = app.uniqFromFile(false, true, true, INPUT_FILE_1, OUTPUT_FILE_1);
            String expected1 = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_SINGLE_WORD);

            String result2 = app.uniqFromFile(false, true, true, INPUT_FILE_2, OUTPUT_FILE_2);
            String expected2 = STRING_EMPTY;

            String result3 = app.uniqFromFile(false, true, true, INPUT_FILE_3, OUTPUT_FILE_3);
            String expected3 = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_SINGLE_WORD);

            assertEquals(expected1, result1);
            assertEquals(expected2, result2);
            assertEquals(expected3, result3);

            assertEquals(expected1, readFromFile(out1));
            assertEquals(expected2, readFromFile(out2));
            assertEquals(expected3, readFromFile(out3));
        });
    }

    @Test
    public void uniqFromFile_IsAllRepeated_DisplaysAllDup() {
        assertDoesNotThrow(() -> {
            String result1 = app.uniqFromFile(false, false, true, INPUT_FILE_1, OUTPUT_FILE_1);
            String expected1 = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_SINGLE_WORD);

            String result2 = app.uniqFromFile(false, false, true, INPUT_FILE_2, OUTPUT_FILE_2);
            String expected2 = STRING_EMPTY;

            String result3 = app.uniqFromFile(false, false, true, INPUT_FILE_3, OUTPUT_FILE_3);
            String expected3 = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_SINGLE_WORD);


            assertEquals(expected1, result1);
            assertEquals(expected2, result2);
            assertEquals(expected3, result3);

            assertEquals(expected1, readFromFile(out1));
            assertEquals(expected2, readFromFile(out2));
            assertEquals(expected3, readFromFile(out3));
        });
    }

    @Test
    public void uniqFromFile_IsCountAndIsRepeated_DisplaysOnlyDupWithPrefix() {
        assertDoesNotThrow(() -> {
            String result1 = app.uniqFromFile(true, true, false, INPUT_FILE_1, OUTPUT_FILE_1);
            String expected1 = String.join(STRING_NEWLINE,
                    "2 " + STRING_MULTI_WORDS,
                    "2 " + STRING_SINGLE_WORD);

            String result2 = app.uniqFromFile(true, true, false, INPUT_FILE_2, OUTPUT_FILE_1);
            String expected2 = STRING_EMPTY;

            String result3 = app.uniqFromFile(true, true, false, INPUT_FILE_3, OUTPUT_FILE_1);
            String expected3 = String.join(STRING_NEWLINE,
                    "2 " + STRING_MULTI_WORDS,
                    "2 " + STRING_SINGLE_WORD);

            assertEquals(expected1, result1);
            assertEquals(expected2, result2);
            assertEquals(expected3, result3);
        });
    }

    @Test
    // printing all duplicated lines and repeat counts is meaningless
    public void uniqFromFile_IsCountAndIsAllRepeated_ThrowsExceptionException() {
        assertThrows(Exception.class, () -> app.uniqFromFile(true, false, true, INPUT_FILE_1, OUTPUT_FILE_1));
    }

    @Test
    // printing all duplicated lines and repeat counts is meaningless
    public void uniqFromFile_IsCountAndIsRepeatedIsAllRepeated_ThrowsExceptionException() {
        assertThrows(Exception.class, () -> app.uniqFromFile(true, true, true, INPUT_FILE_1, OUTPUT_FILE_1));
    }

    @Test
    public void uniqFromStdin_NoInputStream_ThrowsExceptionException() {
        assertThrows(Exception.class, () -> app.uniqFromStdin(true, false, true, null, OUTPUT_FILE_1));
    }

    @Test
    public void uniqFromStdin_NoFlag_RemovesAdjacentDup() {
        assertDoesNotThrow(() -> {
            inputStream = new ByteArrayInputStream(INPUT_1.getBytes());
            String result1 = app.uniqFromStdin(false, false, false, inputStream, OUTPUT_FILE_1);
            String expected1 = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE);

            inputStream = new ByteArrayInputStream(INPUT_2.getBytes());
            String result2 = app.uniqFromStdin(false, false, false, inputStream, OUTPUT_FILE_2);
            String expected2 = String.join(STRING_NEWLINE,
                    STRING_SINGLE_WORD,
                    STRING_MULTI_WORDS,
                    STRING_UNICODE);

            inputStream = new ByteArrayInputStream(INPUT_3.getBytes());
            String result3 = app.uniqFromStdin(false, false, false, inputStream, OUTPUT_FILE_3);
            String expected3 = String.join(STRING_NEWLINE,
                    STRING_SINGLE_WORD,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD);

            assertEquals(expected1, result1);
            assertEquals(expected2, result2);
            assertEquals(expected3, result3);

            assertEquals(expected1, readFromFile(out1));
            assertEquals(expected2, readFromFile(out2));
            assertEquals(expected3, readFromFile(out3));
        });
    }

    @Test
    public void uniqFromStdin_NoFlagNoOutputFile_ShouldWriteToStdout() {
        assertDoesNotThrow(() -> {
            inputStream = new ByteArrayInputStream(INPUT_1.getBytes());
            String result = app.uniqFromStdin(false, false, false, inputStream, null);
            String expected = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE);

            assertEquals(expected, result);
            assertEquals(expected, STD_OUTPUT.toString());
        });
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
    public void run_NoFlagReadAndWriteToFile_RemovesAdjacentDup() {
        assertDoesNotThrow(() -> {
            app.run(new String[]{INPUT_FILE_1, OUTPUT_FILE_1}, System.in, System.out);
            String expected = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE);

            assertNotEquals(expected, STD_OUTPUT.toString());
            assertEquals(expected, readFromFile(out1));
        });
    }

    @Test
    public void run_NoFlagWriteToStdout_RemovesAdjacentDup() {
        assertDoesNotThrow(() -> {
            app.run(new String[]{INPUT_FILE_1}, System.in, System.out);
            String expected = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE);

            assertEquals(expected, STD_OUTPUT.toString());
        });
    }

    @Test
    public void run_NoFlagAndOutputFilenameIsDash_RemovesAdjacentDup() {
        assertDoesNotThrow(() -> {
            app.run(new String[]{INPUT_FILE_1, STRING_STDIN_FLAG}, System.in, System.out);
            String expected = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE);

            assertEquals(expected, STD_OUTPUT.toString());
        });
    }

    @Test
    public void run_IsCountWriteToStdout_RemovesAdjacentDupWithPrefix() {
        assertDoesNotThrow(() -> {
            app.run(new String[]{INPUT_FILE_1, "-c"}, System.in, System.out);
            String expected = String.join(STRING_NEWLINE,
                    "2 " + STRING_MULTI_WORDS,
                    "2 " + STRING_SINGLE_WORD,
                    "1 " + STRING_UNICODE,
                    "1 " + STRING_SINGLE_WORD,
                    "1 " + STRING_UNICODE);

            assertEquals(expected, STD_OUTPUT.toString());
        });
    }

    @Test
    public void run_IsRepeatedWriteToStdout_DisplaysOnlyDup() {
        assertDoesNotThrow(() -> {
            app.run(new String[]{INPUT_FILE_1, "-d"}, System.in, System.out);
            String expected = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD);

            assertEquals(expected, STD_OUTPUT.toString());
        });
    }

    @Test
    public void run_IsAllRepeatedWriteToStdout_DisplaysAllDup() {
        assertDoesNotThrow(() -> {
            app.run(new String[]{INPUT_FILE_1, "-D"}, System.in, System.out);
            String expected = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_SINGLE_WORD);

            assertEquals(expected, STD_OUTPUT.toString());
        });
    }

    @Test
    public void run_IsRepeatedAndIsCountWriteToStdout_DisplaysOnlyDupWithPrefix() {
        assertDoesNotThrow(() -> {
            app.run(new String[]{INPUT_FILE_1, "-d", "-c"}, System.in, System.out);
            String expected = String.join(STRING_NEWLINE,
                    "2 " + STRING_MULTI_WORDS,
                    "2 " + STRING_SINGLE_WORD);

            assertEquals(expected, STD_OUTPUT.toString());
        });
    }

    @Test
    public void run_IsRepeatedAndIsAllRepeatedWriteToStdout_DisplaysOnlyDupWithPrefix() {
        assertDoesNotThrow(() -> {
            app.run(new String[]{INPUT_FILE_1, "-d", "-D"}, System.in, System.out);
            String expected = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_SINGLE_WORD);

            assertEquals(expected, STD_OUTPUT.toString());
        });
    }

    @Test
    public void run_ReadFromStdinBlank_DisplaysBlank() {
        assertDoesNotThrow(() -> {
            inputStream = new ByteArrayInputStream(STRING_EMPTY.getBytes());
            app.run(new String[]{}, inputStream, System.out);

            assertEquals(STRING_EMPTY, STD_OUTPUT.toString());
        });
    }

}
