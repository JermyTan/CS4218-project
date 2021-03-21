package ef2;


import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.parser.ArgsParser.ILLEGAL_FLAG_MSG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_FILE_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOO_MANY_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.IOUtils.getLinesFromInputStream;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_FLAG;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_MULTI_WORDS;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_SINGLE_WORD;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_UNICODE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.exception.UniqException;
import sg.edu.nus.comp.cs4218.impl.app.UniqApplication;

@SuppressWarnings("PMD.ExcessiveMethodLength")
public class UniqApplicationTest {
    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = String.join(STRING_FILE_SEP,
            EnvironmentUtil.currentDirectory,
            RESOURCES_PATH,
            "UniqApplicationTest");
    private static final Path TEST_PATH = Path.of(TEST_DIR);

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

    private static final String FOLDER_1 = "folder1";

    private static final String OUTPUT_FILE_1 = "out1.txt";
    private static final String OUTPUT_FILE_2 = "out2.txt";
    private static final String OUTPUT_FILE_3 = "out3.txt"; // does not exist

    private final Path in1 = Path.of(TEST_DIR, INPUT_FILE_1);
    private final Path in2 = Path.of(TEST_DIR, INPUT_FILE_2);
    private final Path in3 = Path.of(TEST_DIR, INPUT_FILE_3);
    private final Path in4 = Path.of(TEST_DIR, INPUT_FILE_4);

    private final Path folder1 = Path.of(TEST_DIR, FOLDER_1);

    private final Path out1 = Path.of(TEST_DIR, OUTPUT_FILE_1);
    private final Path out2 = Path.of(TEST_DIR, OUTPUT_FILE_2);
    private final Path out3 = Path.of(TEST_DIR, OUTPUT_FILE_3);
    private final UniqApplication app = new UniqApplication();
    private OutputStream stdout;
    private InputStream stdin;

    @BeforeAll
    static void setupBeforeAll() throws IOException {
        if (!Files.exists(TEST_PATH)) {
            Files.createDirectory(TEST_PATH);
        }

        EnvironmentUtil.currentDirectory = TEST_DIR;
    }

    @AfterAll
    static void tearDownAfterAll() throws IOException {
        Files.deleteIfExists(TEST_PATH);
        EnvironmentUtil.currentDirectory = ORIGINAL_DIR;
    }

    private void createFileWithContent(Path path, String content) throws IOException {
        Files.writeString(path, content, CREATE, WRITE, TRUNCATE_EXISTING);
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
        createFileWithContent(in1, INPUT_1);
        createFileWithContent(in2, INPUT_2);
        createFileWithContent(in3, INPUT_3);

        Files.createFile(out1);
        Files.createFile(out2);
        Files.createDirectory(folder1);

        stdin = new ByteArrayInputStream(STRING_EMPTY.getBytes());
        stdout = new ByteArrayOutputStream();
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
    public void uniqFromFile_NullFlags_ThrowsException() {
        Throwable exception = assertThrows(
                UniqException.class,
                () -> app.uniqFromFile(null, true, false, INPUT_FILE_1, OUTPUT_FILE_1)
        );
        assertEquals(
                new UniqException(ERR_NULL_ARGS).getMessage(),
                exception.getMessage()
        );
    }

    @Test
    public void uniqFromFile_NoInputFile_ThrowsException() {
        Throwable exception = assertThrows(
                UniqException.class,
                () -> app.uniqFromFile(false, false, false, null, OUTPUT_FILE_1)
        );
        assertEquals(
                new UniqException(ERR_NO_FILE_ARGS).getMessage(),
                exception.getMessage()
        );
    }

    @Test
    public void uniqFromFile_InputFileDoesNotExist_ThrowsException() {
        Throwable exception = assertThrows(
                UniqException.class,
                () -> app.uniqFromFile(false, false, false, INPUT_FILE_4, OUTPUT_FILE_1)
        );
        assertEquals(
                new UniqException(new InvalidDirectoryException(INPUT_FILE_4, ERR_FILE_NOT_FOUND).getMessage()).getMessage(),
                exception.getMessage()
        );
    }

    @Test
    public void uniqFromFile_InputFileIsDirectory_ThrowsException() {
        Throwable exception = assertThrows(
                UniqException.class,
                () -> app.uniqFromFile(false, false, false, FOLDER_1, OUTPUT_FILE_1)
        );
        assertEquals(
                new UniqException(new InvalidDirectoryException(FOLDER_1, ERR_IS_DIR).getMessage()).getMessage(),
                exception.getMessage()
        );
    }

    @Test
    public void uniqFromFile_InputFileNameIsEmpty_ThrowsException() {
        Throwable exception = assertThrows(
                UniqException.class,
                () -> app.uniqFromFile(false, false, false, STRING_EMPTY, OUTPUT_FILE_1)
        );
        assertEquals(
                new UniqException(new InvalidDirectoryException(STRING_EMPTY, ERR_FILE_NOT_FOUND).getMessage()).getMessage(),
                exception.getMessage()
        );
    }

    @Test
    public void uniqFromFile_OutputFileIsDirectory_ThrowsException() {
        Throwable exception = assertThrows(
                UniqException.class,
                () -> app.uniqFromFile(false, false, false, INPUT_FILE_1, FOLDER_1)
        );
        assertEquals(
                new UniqException(new InvalidDirectoryException(FOLDER_1, ERR_IS_DIR).getMessage()).getMessage(),
                exception.getMessage()
        );
    }

    @Test
    public void uniqFromFile_OutputFileNameIsEmpty_ThrowsException() {
        Throwable exception = assertThrows(
                UniqException.class,
                () -> app.uniqFromFile(false, false, false, INPUT_FILE_1, STRING_EMPTY)
        );
        assertEquals(
                new UniqException(new InvalidDirectoryException(STRING_EMPTY, ERR_FILE_NOT_FOUND).getMessage()).getMessage(),
                exception.getMessage()
        );
    }

    @Test
    public void uniqFromFile_OutputFileNotWritable_ThrowsException() {
        out1.toFile().setWritable(false);

        Throwable exception = assertThrows(
                UniqException.class,
                () -> app.uniqFromFile(false, false, false, INPUT_FILE_2, OUTPUT_FILE_1)
        );
        assertEquals(
                new UniqException(new InvalidDirectoryException(OUTPUT_FILE_1, ERR_NO_PERM).getMessage()).getMessage(),
                exception.getMessage()
        );
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

    /* stdout not given as param
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
            assertEquals(expected, stdout.toString());
        });
    }
     */

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

            String result2 = app.uniqFromFile(true, true, false, INPUT_FILE_2, OUTPUT_FILE_2);
            String expected2 = STRING_EMPTY;

            String result3 = app.uniqFromFile(true, true, false, INPUT_FILE_3, OUTPUT_FILE_3);
            String expected3 = String.join(STRING_NEWLINE,
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
    // printing all duplicated lines and repeat counts is meaningless
    public void uniqFromFile_IsCountAndIsAllRepeated_ThrowsException() {
        Throwable exception = assertThrows(
                UniqException.class,
                () -> app.uniqFromFile(true, false, true, INPUT_FILE_1, OUTPUT_FILE_1)
        );
        assertEquals(new UniqException(ERR_INVALID_ARGS).getMessage(), exception.getMessage());
    }

    @Test
    // printing all duplicated lines and repeat counts is meaningless
    public void uniqFromFile_IsCountAndIsRepeatedIsAllRepeated_ThrowsException() {
        Throwable exception = assertThrows(
                UniqException.class,
                () -> app.uniqFromFile(true, true, true, INPUT_FILE_1, OUTPUT_FILE_1)
        );
        assertEquals(new UniqException(ERR_INVALID_ARGS).getMessage(), exception.getMessage());
    }

    @Test
    public void uniqFromStdin_NullFlags_ThrowsException() {
        stdin = new ByteArrayInputStream(INPUT_1.getBytes());

        Throwable exception = assertThrows(
                UniqException.class,
                () -> app.uniqFromStdin(null, true, false, stdin, OUTPUT_FILE_1)
        );
        assertEquals(
                new UniqException(ERR_NULL_ARGS).getMessage(),
                exception.getMessage()
        );
    }

    @Test
    public void uniqFromStdin_NoInputFile_ThrowsException() {
        Throwable exception = assertThrows(
                UniqException.class,
                () -> app.uniqFromStdin(false, false, false, null, OUTPUT_FILE_1)
        );
        assertEquals(
                new UniqException(ERR_NO_ISTREAM).getMessage(),
                exception.getMessage()
        );
    }

    @Test
    public void uniqFromStdin_OutputFileIsDirectory_ThrowsException() {
        stdin = new ByteArrayInputStream(INPUT_1.getBytes());

        Throwable exception = assertThrows(
                UniqException.class,
                () -> app.uniqFromStdin(false, false, false, stdin, FOLDER_1)
        );
        assertEquals(
                new UniqException(new InvalidDirectoryException(FOLDER_1, ERR_IS_DIR).getMessage()).getMessage(),
                exception.getMessage()
        );
    }

    @Test
    public void uniqFromStdin_OutputFileNameIsEmpty_ThrowsException() {
        stdin = new ByteArrayInputStream(INPUT_1.getBytes());

        Throwable exception = assertThrows(
                UniqException.class,
                () -> app.uniqFromStdin(false, false, false, stdin, STRING_EMPTY)
        );
        assertEquals(
                new UniqException(new InvalidDirectoryException(STRING_EMPTY, ERR_FILE_NOT_FOUND).getMessage()).getMessage(),
                exception.getMessage()
        );
    }

    @Test
    public void uniqFromStdin_OutputFileNotWritable_ThrowsException() {
        stdin = new ByteArrayInputStream(INPUT_1.getBytes());
        out1.toFile().setWritable(false);

        Throwable exception = assertThrows(
                UniqException.class,
                () -> app.uniqFromStdin(false, false, false, stdin, OUTPUT_FILE_1)
        );
        assertEquals(
                new UniqException(new InvalidDirectoryException(OUTPUT_FILE_1, ERR_NO_PERM).getMessage()).getMessage(),
                exception.getMessage()
        );
    }

    @Test
    public void uniqFromStdin_NoFlag_RemovesAdjacentDup() {
        assertDoesNotThrow(() -> {
            stdin = new ByteArrayInputStream(INPUT_1.getBytes());
            String result1 = app.uniqFromStdin(false, false, false, stdin, OUTPUT_FILE_1);
            String expected1 = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE);

            stdin = new ByteArrayInputStream(INPUT_2.getBytes());
            String result2 = app.uniqFromStdin(false, false, false, stdin, OUTPUT_FILE_2);
            String expected2 = String.join(STRING_NEWLINE,
                    STRING_SINGLE_WORD,
                    STRING_MULTI_WORDS,
                    STRING_UNICODE);

            stdin = new ByteArrayInputStream(INPUT_3.getBytes());
            String result3 = app.uniqFromStdin(false, false, false, stdin, OUTPUT_FILE_3);
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
    public void uniqFromStdin_StdinEmpty_EmptyString() throws Exception {
        String result1 = app.uniqFromStdin(true, false, false, stdin, OUTPUT_FILE_1);
        String expected1 = STRING_EMPTY;

        assertEquals(expected1, result1);

        assertEquals(expected1, readFromFile(out1));
    }

    /* stdout not given as param
    @Test
    public void uniqFromStdin_NoFlagNoOutputFile_WriteToStdout() {
        assertDoesNotThrow(() -> {
            stdin = new ByteArrayInputStream(INPUT_1.getBytes());
            String result = app.uniqFromStdin(false, false, false, stdin, null);
            String expected = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE);

            assertEquals(expected, result);
            assertEquals(expected, stdout);
        });
    }
     */

    @Test
    public void uniqFromStdin_IsCount_RemovesAdjacentDupWithPrefix() {
        assertDoesNotThrow(() -> {
            stdin = new ByteArrayInputStream(INPUT_1.getBytes());
            String result1 = app.uniqFromStdin(true, false, false, stdin, OUTPUT_FILE_1);
            String expected1 = String.join(STRING_NEWLINE,
                    "2 " + STRING_MULTI_WORDS,
                    "2 " + STRING_SINGLE_WORD,
                    "1 " + STRING_UNICODE,
                    "1 " + STRING_SINGLE_WORD,
                    "1 " + STRING_UNICODE);

            stdin = new ByteArrayInputStream(INPUT_2.getBytes());
            String result2 = app.uniqFromStdin(true, false, false, stdin, OUTPUT_FILE_2);
            String expected2 = String.join(STRING_NEWLINE,
                    "1 " + STRING_SINGLE_WORD,
                    "1 " + STRING_MULTI_WORDS,
                    "1 " + STRING_UNICODE);

            stdin = new ByteArrayInputStream(INPUT_3.getBytes());
            String result3 = app.uniqFromStdin(true, false, false, stdin, OUTPUT_FILE_3);
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
    public void uniqFromStdin_IsRepeated_DisplaysOnlyDup() {
        assertDoesNotThrow(() -> {
            stdin = new ByteArrayInputStream(INPUT_1.getBytes());
            String result1 = app.uniqFromStdin(false, true, false, stdin, OUTPUT_FILE_1);
            String expected1 = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD);

            stdin = new ByteArrayInputStream(INPUT_2.getBytes());
            String result2 = app.uniqFromStdin(false, true, false, stdin, OUTPUT_FILE_2);
            String expected2 = STRING_EMPTY;

            stdin = new ByteArrayInputStream(INPUT_3.getBytes());
            String result3 = app.uniqFromStdin(false, true, false, stdin, OUTPUT_FILE_3);
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
    public void uniqFromStdin_IsRepeatedAndIsAllRepeated_DisplaysAllDup() {
        assertDoesNotThrow(() -> {
            stdin = new ByteArrayInputStream(INPUT_1.getBytes());
            String result1 = app.uniqFromStdin(false, true, true, stdin, OUTPUT_FILE_1);
            String expected1 = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_SINGLE_WORD);

            stdin = new ByteArrayInputStream(INPUT_2.getBytes());
            String result2 = app.uniqFromStdin(false, true, true, stdin, OUTPUT_FILE_2);
            String expected2 = STRING_EMPTY;

            stdin = new ByteArrayInputStream(INPUT_3.getBytes());
            String result3 = app.uniqFromStdin(false, true, true, stdin, OUTPUT_FILE_3);
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
    public void uniqFromStdin_IsAllRepeated_DisplaysAllDup() {
        assertDoesNotThrow(() -> {
            stdin = new ByteArrayInputStream(INPUT_1.getBytes());
            String result1 = app.uniqFromStdin(false, false, true, stdin, OUTPUT_FILE_1);
            String expected1 = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_SINGLE_WORD);

            stdin = new ByteArrayInputStream(INPUT_2.getBytes());
            String result2 = app.uniqFromStdin(false, false, true, stdin, OUTPUT_FILE_2);
            String expected2 = STRING_EMPTY;

            stdin = new ByteArrayInputStream(INPUT_3.getBytes());
            String result3 = app.uniqFromStdin(false, false, true, stdin, OUTPUT_FILE_3);
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
    public void uniqFromStdin_IsCountAndIsRepeated_DisplaysOnlyDupWithPrefix() {
        assertDoesNotThrow(() -> {
            stdin = new ByteArrayInputStream(INPUT_1.getBytes());
            String result1 = app.uniqFromStdin(true, true, false, stdin, OUTPUT_FILE_1);
            String expected1 = String.join(STRING_NEWLINE,
                    "2 " + STRING_MULTI_WORDS,
                    "2 " + STRING_SINGLE_WORD);

            stdin = new ByteArrayInputStream(INPUT_2.getBytes());
            String result2 = app.uniqFromStdin(true, true, false, stdin, OUTPUT_FILE_2);
            String expected2 = STRING_EMPTY;

            stdin = new ByteArrayInputStream(INPUT_3.getBytes());
            String result3 = app.uniqFromStdin(true, true, false, stdin, OUTPUT_FILE_3);
            String expected3 = String.join(STRING_NEWLINE,
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
    // printing all duplicated lines and repeat counts is meaningless
    public void uniqFromStdin_IsCountAndIsAllRepeated_ThrowsException() {
        stdin = new ByteArrayInputStream(INPUT_1.getBytes());
        Throwable exception = assertThrows(
                UniqException.class,
                () -> app.uniqFromStdin(true, false, true, stdin, OUTPUT_FILE_1)
        );
        assertEquals(new UniqException(ERR_INVALID_ARGS).getMessage(), exception.getMessage());
    }

    @Test
    // printing all duplicated lines and repeat counts is meaningless
    public void uniqFromStdin_IsCountAndIsRepeatedIsAllRepeated_ThrowsException() {
        stdin = new ByteArrayInputStream(INPUT_1.getBytes());
        Throwable exception = assertThrows(
                UniqException.class,
                () -> app.uniqFromStdin(true, true, true, stdin, OUTPUT_FILE_1)
        );
        assertEquals(new UniqException(ERR_INVALID_ARGS).getMessage(), exception.getMessage());
    }

    @Test
    public void run_InvalidOption_ThrowsException() {
        Throwable exception = assertThrows(
                Exception.class,
                () -> app.run(new String[]{INPUT_FILE_1, "-r"}, stdin, stdout)
        );
        assertEquals(
                new UniqException(new InvalidArgsException(ILLEGAL_FLAG_MSG + "r").getMessage()).getMessage(),
                exception.getMessage()
        );
    }

    @Test
    public void run_TooManyArgs_ThrowsException() {
        Throwable exception = assertThrows(
                Exception.class,
                () -> app.run(new String[]{INPUT_FILE_1, INPUT_FILE_2, OUTPUT_FILE_1}, stdin, stdout)
        );
        assertEquals(
                new UniqException(new InvalidArgsException(ERR_TOO_MANY_ARGS).getMessage()).getMessage(),
                exception.getMessage()
        );
    }

    @Test
    public void run_ArgsContainNull_ThrowsException() {
        Throwable exception = assertThrows(
                Exception.class,
                () -> app.run(new String[]{INPUT_FILE_1, null}, stdin, stdout)
        );
        assertEquals(
                new UniqException(new InvalidArgsException(ERR_NULL_ARGS).getMessage()).getMessage(),
                exception.getMessage()
        );
    }

    @Test
    public void run_NoInputStreamAndInputFile_ThrowsException() {
        Throwable exception1 = assertThrows(
                Exception.class,
                () -> app.run(null, null, stdout)
        );
        assertEquals(
                new UniqException(ERR_NO_INPUT).getMessage(),
                exception1.getMessage()
        );

        Throwable exception2 = assertThrows(
                Exception.class,
                () -> app.run(new String[]{}, null, stdout)
        );
        assertEquals(
                new UniqException(ERR_NO_INPUT).getMessage(),
                exception2.getMessage()
        );

        Throwable exception3 = assertThrows(
                Exception.class,
                () -> app.run(new String[]{STRING_STDIN_FLAG}, null, stdout)
        );
        assertEquals(
                new UniqException(ERR_NO_INPUT).getMessage(),
                exception3.getMessage()
        );
    }

    @Test
    public void run_NoOutputStreamAndOutputFile_ThrowsException() {
        Throwable exception1 = assertThrows(
                Exception.class,
                () -> app.run(null, stdin, null)
        );
        assertEquals(
                new UniqException(ERR_NO_OSTREAM).getMessage(),
                exception1.getMessage()
        );

        Throwable exception2 = assertThrows(
                Exception.class,
                () -> app.run(new String[]{}, stdin, null)
        );
        assertEquals(
                new UniqException(ERR_NO_OSTREAM).getMessage(),
                exception2.getMessage()
        );

        Throwable exception3 = assertThrows(
                Exception.class,
                () -> app.run(new String[]{INPUT_FILE_1}, stdin, null)
        );
        assertEquals(
                new UniqException(ERR_NO_OSTREAM).getMessage(),
                exception3.getMessage()
        );
    }

    @Test
    public void run_NoFlagReadAndWriteToFile_RemovesAdjacentDup() {
        assertDoesNotThrow(() -> {
            app.run(new String[]{INPUT_FILE_1, OUTPUT_FILE_1}, null, null);
            String expected = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE);

            assertNotEquals(expected, stdout.toString());
            assertEquals(expected, readFromFile(out1));
        });

        assertDoesNotThrow(() -> {
            app.run(new String[]{INPUT_FILE_1, OUTPUT_FILE_1}, null, stdout);
            String expected = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE);

            assertNotEquals(expected, stdout.toString());
            assertEquals(expected, readFromFile(out1));
        });

        assertDoesNotThrow(() -> {
            app.run(new String[]{INPUT_FILE_1, OUTPUT_FILE_1}, stdin, null);
            String expected = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE);

            assertNotEquals(expected, stdout.toString());
            assertEquals(expected, readFromFile(out1));
        });

        assertDoesNotThrow(() -> {
            app.run(new String[]{INPUT_FILE_1, OUTPUT_FILE_1}, stdin, stdout);
            String expected = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE);

            assertNotEquals(expected, stdout.toString());
            assertEquals(expected, readFromFile(out1));
        });
    }

    @Test
    public void run_NoFlagReadFromStdinWriteToFile_RemovesAdjacentDup() {
        assertDoesNotThrow(() -> {
            stdin = new ByteArrayInputStream(INPUT_1.getBytes());
            app.run(new String[]{STRING_STDIN_FLAG, OUTPUT_FILE_1}, stdin, stdout);
            String expected = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE);

            assertNotEquals(expected, stdout.toString());
            assertEquals(expected, readFromFile(out1));
        });

        assertDoesNotThrow(() -> {
            stdin = new ByteArrayInputStream(INPUT_1.getBytes());
            app.run(new String[]{STRING_STDIN_FLAG, OUTPUT_FILE_1}, stdin, null);
            String expected = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE);

            assertNotEquals(expected, stdout.toString());
            assertEquals(expected, readFromFile(out1));
        });
    }

    @Test
    public void run_NoFlagWriteToStdout_RemovesAdjacentDup() {
        assertDoesNotThrow(() -> {
            stdout = new ByteArrayOutputStream();
            app.run(new String[]{INPUT_FILE_1}, stdin, stdout);
            String expected = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE,
                    STRING_EMPTY);

            assertEquals(expected, stdout.toString());
        });

        assertDoesNotThrow(() -> {
            stdin = new ByteArrayInputStream(INPUT_1.getBytes());
            stdout = new ByteArrayOutputStream();
            app.run(new String[]{STRING_STDIN_FLAG}, stdin, stdout);
            String expected = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE,
                    STRING_EMPTY);

            assertEquals(expected, stdout.toString());
        });

        assertDoesNotThrow(() -> {
            stdin = new ByteArrayInputStream(INPUT_1.getBytes());
            stdout = new ByteArrayOutputStream();
            app.run(new String[]{}, stdin, stdout);
            String expected = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE,
                    STRING_EMPTY);

            assertEquals(expected, stdout.toString());
        });

        assertDoesNotThrow(() -> {
            stdin = new ByteArrayInputStream(INPUT_1.getBytes());
            stdout = new ByteArrayOutputStream();
            app.run(null, stdin, stdout);
            String expected = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE,
                    STRING_EMPTY);

            assertEquals(expected, stdout.toString());
        });
    }

    @Test
    public void run_NoFlagAndOutputFilenameIsDash_RemovesAdjacentDup() {
        assertDoesNotThrow(() -> {
            app.run(new String[]{INPUT_FILE_1, STRING_STDIN_FLAG}, stdin, stdout);
            String expected = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE,
                    STRING_SINGLE_WORD,
                    STRING_UNICODE);

            assertNotEquals(expected, stdout.toString());
            assertEquals(expected, readFromFile(Path.of(TEST_DIR, STRING_STDIN_FLAG)));
        });
    }

    @Test
    public void run_IsCountWriteToStdout_RemovesAdjacentDupWithPrefix() {
        assertDoesNotThrow(() -> {
            app.run(new String[]{INPUT_FILE_1, "-c"}, stdin, stdout);
            String expected = String.join(STRING_NEWLINE,
                    "2 " + STRING_MULTI_WORDS,
                    "2 " + STRING_SINGLE_WORD,
                    "1 " + STRING_UNICODE,
                    "1 " + STRING_SINGLE_WORD,
                    "1 " + STRING_UNICODE,
                    STRING_EMPTY);

            assertEquals(expected, stdout.toString());
        });
    }

    @Test
    public void run_IsRepeatedWriteToStdout_DisplaysOnlyDup() {
        assertDoesNotThrow(() -> {
            app.run(new String[]{INPUT_FILE_1, "-d"}, stdin, stdout);
            String expected = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_EMPTY);

            assertEquals(expected, stdout.toString());
        });
    }

    @Test
    public void run_IsAllRepeatedWriteToStdout_DisplaysAllDup() {
        assertDoesNotThrow(() -> {
            app.run(new String[]{INPUT_FILE_1, "-D"}, stdin, stdout);
            String expected = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_SINGLE_WORD,
                    STRING_EMPTY);

            assertEquals(expected, stdout.toString());
        });
    }

    @Test
    public void run_IsRepeatedAndIsCountWriteToStdout_DisplaysOnlyDupWithPrefix() {
        assertDoesNotThrow(() -> {
            app.run(new String[]{INPUT_FILE_1, "-d", "-c"}, stdin, stdout);
            String expected = String.join(STRING_NEWLINE,
                    "2 " + STRING_MULTI_WORDS,
                    "2 " + STRING_SINGLE_WORD,
                    STRING_EMPTY);

            assertEquals(expected, stdout.toString());
        });
    }

    @Test
    public void run_IsRepeatedAndIsAllRepeatedWriteToStdout_DisplaysOnlyDupWithPrefix() {
        assertDoesNotThrow(() -> {
            app.run(new String[]{INPUT_FILE_1, "-d", "-D"}, stdin, stdout);
            String expected = String.join(STRING_NEWLINE,
                    STRING_MULTI_WORDS,
                    STRING_MULTI_WORDS,
                    STRING_SINGLE_WORD,
                    STRING_SINGLE_WORD,
                    STRING_EMPTY);

            assertEquals(expected, stdout.toString());
        });
    }

    @Test
    // printing all duplicated lines and repeat counts is meaningless
    public void run_IsCountAndIsAllRepeated_ThrowsException() {
        Throwable exception = assertThrows(
                UniqException.class,
                () -> app.run(new String[]{INPUT_FILE_1, "-D", "-c"}, stdin, stdout)
        );
        assertEquals(
                new UniqException(new InvalidArgsException(ILLEGAL_FLAG_MSG + "c" + "D").getMessage()).getMessage(),
                exception.getMessage()
        );
    }

    @Test
    // printing all duplicated lines and repeat counts is meaningless
    public void run_IsCountAndIsRepeatedIsAllRepeated_ThrowsException() {
        Throwable exception = assertThrows(
                UniqException.class,
                () -> app.run(new String[]{INPUT_FILE_1, "-D", "-c", "-d"}, stdin, stdout)
        );
        assertEquals(
                new UniqException(new InvalidArgsException(ILLEGAL_FLAG_MSG + "c" + "D").getMessage()).getMessage(),
                exception.getMessage()
        );
    }

    @Test
    public void run_ReadFromStdinBlank_DisplaysBlank() {
        assertDoesNotThrow(() -> {
            app.run(new String[]{}, stdin, stdout);

            assertEquals(STRING_EMPTY, stdout.toString());
        });

        assertDoesNotThrow(() -> {
            app.run(new String[]{STRING_STDIN_FLAG, OUTPUT_FILE_1}, stdin, stdout);

            assertEquals(STRING_EMPTY, stdout.toString());
            assertEquals(STRING_EMPTY, readFromFile(out1));
        });
    }
}
