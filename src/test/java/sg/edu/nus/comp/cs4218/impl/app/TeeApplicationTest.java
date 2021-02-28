package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_FILES;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.TeeException;

class TeeApplicationTest {

    private static final String INPUT_1 = "hello world";
    private static final String INPUT_2 = "hello world" + STRING_NEWLINE + "How are you";

    private static final String OUTPUT_FILE_1 = "output1.txt";
    private static final String OUTPUT_FILE_2 = "output2.txt";
    private static final String OUTPUT_FILE_3 = "output3.txt";
    private static final String INPUT_FILE_1 = "input1.txt";

    private static final String[] ARGS_1 = {"tee", OUTPUT_FILE_1, OUTPUT_FILE_2};
    private static final String[] ARGS_2 = {"tee", "-a", OUTPUT_FILE_1};

    private static final String FOLDER = "folder";

    private static final String FILE_CONTENT_1 = "This is old content.";
    private static final String FILE_CONTENT_2 = "";
    private final ByteArrayOutputStream ERR_OUTPUT = new ByteArrayOutputStream();
    private final ByteArrayOutputStream STD_OUTPUT = new ByteArrayOutputStream();
    private final Path file1 = Paths.get(OUTPUT_FILE_1); // exists
    private final Path file2 = Paths.get(OUTPUT_FILE_2); // exists
    private final Path file3 = Paths.get(OUTPUT_FILE_3); // does not exist
    private final Path folder = Paths.get(FOLDER); // exists
    private final List<Path> paths = List.of(file1, file2, file3, folder);
    private InputStream inputStream;
    private TeeApplication app;

    private void createFileWithContent(Path path, String content) throws IOException {
        Files.createFile(path);
        BufferedWriter outputStream = new BufferedWriter(new FileWriter(path.toFile(), true));//NOPMD
        outputStream.append(content);
        outputStream.close();
    }

    private String readFromFile(String path) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));//NOPMD
            String line = null;
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

    @BeforeEach
    void setUp() {
        app = new TeeApplication();

        try {
            createFileWithContent(file1, FILE_CONTENT_1);
            createFileWithContent(file2, FILE_CONTENT_2);

            Files.createDirectory(folder);

        } catch (IOException e) {
            fail(e.getMessage());
        }

        System.setOut(new PrintStream(STD_OUTPUT));
        System.setErr(new PrintStream(ERR_OUTPUT));
    }

    @AfterEach
    void tearDown() {
        try {
            for (Path path : paths) {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }

        System.setErr(System.err);
        System.setErr(System.out);
    }

    @Test
    public void teeFromStdin_WriteSingleLineToBothStdoutAndFileAppend_ShouldReturn() throws TeeException {
        inputStream = new ByteArrayInputStream(INPUT_1.getBytes());
        String[] filenames = {OUTPUT_FILE_1, OUTPUT_FILE_2};
        String output = app.teeFromStdin(true, inputStream, filenames);
        String fromFile1 = readFromFile(OUTPUT_FILE_1);
        String fromFile2 = readFromFile(OUTPUT_FILE_2);

        assertEquals(INPUT_1, output);
        assertEquals(FILE_CONTENT_1 + INPUT_1, fromFile1);
        assertEquals(INPUT_1, fromFile2);
    }

    @Test
    public void teeFromStdin_WriteMultiLinesToBothStdoutAndFileAppend_ShouldReturn() throws TeeException {
        inputStream = new ByteArrayInputStream(INPUT_2.getBytes());
        String[] filenames = {OUTPUT_FILE_1, OUTPUT_FILE_2};
        String output = app.teeFromStdin(true, inputStream, filenames);
        String fromFile1 = readFromFile(OUTPUT_FILE_1);
        String fromFile2 = readFromFile(OUTPUT_FILE_2);

        assertEquals(INPUT_2, output);
        assertEquals(FILE_CONTENT_1 + INPUT_2, fromFile1);
        assertEquals(INPUT_2, fromFile2);
    }

    @Test
    public void teeFromStdin_WriteToBothStdoutAndFileNoAppend_ShouldTruncateFile() throws TeeException {
        inputStream = new ByteArrayInputStream(INPUT_1.getBytes());
        String[] filenames = {OUTPUT_FILE_1, OUTPUT_FILE_2};
        String output = app.teeFromStdin(false, inputStream, filenames);
        String fromFile1 = readFromFile(OUTPUT_FILE_1);
        String fromFile2 = readFromFile(OUTPUT_FILE_2);

        assertEquals(INPUT_1, output);
        assertEquals(INPUT_1, fromFile1);
        assertEquals(INPUT_1, fromFile2);
    }

    @Test
    public void teeFromStdin_WriteToStdoutOnlyNoAppend_ShouldReturn() throws TeeException {
        inputStream = new ByteArrayInputStream(INPUT_1.getBytes());
        String output = app.teeFromStdin(false, inputStream);

        assertEquals(INPUT_1, output);
    }

    @Test
    public void teeFromStdin_WriteToStdoutOnlyAppend_ShouldReturn() throws TeeException {
        inputStream = new ByteArrayInputStream(INPUT_2.getBytes());
        String output = app.teeFromStdin(true, inputStream);

        assertEquals(INPUT_2, output);
    }

    @Test
    public void teeFromStdin_FileIsDirectory_ShouldWriteToStderr() throws TeeException {
        inputStream = new ByteArrayInputStream(INPUT_1.getBytes());
        String[] filenames = {FOLDER};
        app.teeFromStdin(false, inputStream, filenames);
        assertEquals("tee: " + FOLDER + ": " + ERR_IS_DIR + STRING_NEWLINE, ERR_OUTPUT.toString());//NOPMD
    }

    @Test
    public void teeFromStdin_FileNotFound_ShouldCreateFile() throws TeeException {
        inputStream = new ByteArrayInputStream(INPUT_1.getBytes());
        String[] filenames = {OUTPUT_FILE_3};
        String output = app.teeFromStdin(false, inputStream, filenames);
        String fromFile3 = readFromFile(OUTPUT_FILE_3);

        assertEquals(INPUT_1, fromFile3);
        assertEquals(INPUT_1, output);
    }

    @Test
    public void teeFromStdin_FilenameContainsNullValues_ShouldWriteToStderr() {
        String[] filenames = {OUTPUT_FILE_1, null};
        inputStream = new ByteArrayInputStream(INPUT_1.getBytes());
        Throwable error = assertThrows(TeeException.class, () -> app.teeFromStdin(false, inputStream, filenames));
        assertEquals("tee: " + ERR_INVALID_FILES, error.getMessage());
    }

    @Test
    public void teeFromStdin_NoInputStream_ShouldWriteToStderr() {
        String[] filenames = {OUTPUT_FILE_1};
        Throwable error = assertThrows(TeeException.class, () -> app.teeFromStdin(false, null, filenames));
        assertEquals("tee: " + ERR_NO_ISTREAM, error.getMessage());
    }

    @Test
    public void run_NoInputStream_ShouldThrowException() {
        Throwable error = assertThrows(TeeException.class, () -> app.run(ARGS_1, null, STD_OUTPUT));
        assertEquals("tee: " + ERR_NO_ISTREAM, error.getMessage());
    }

    @Test
    public void run_NoOutputStream_ShouldThrowException() {
        inputStream = new ByteArrayInputStream(INPUT_1.getBytes());
        Throwable error = assertThrows(TeeException.class, () -> app.run(ARGS_1, inputStream, null));
        assertEquals("tee: " + ERR_NO_OSTREAM, error.getMessage());
    }

    @Test
    public void run_ReadFromStdinNoAppend_ShouldReturn() throws TeeException {
        inputStream = new ByteArrayInputStream(INPUT_1.getBytes());
        app.run(ARGS_1, inputStream, STD_OUTPUT);
        String fromFile1 = readFromFile(OUTPUT_FILE_1);
        String fromFile2 = readFromFile(OUTPUT_FILE_2);

        assertEquals(INPUT_1 + STRING_NEWLINE, STD_OUTPUT.toString());
        assertEquals(INPUT_1, fromFile1);
        assertEquals(INPUT_1, fromFile2);
    }

    @Test
    public void run_ReadFromStdinAppend_ShouldReturn() throws TeeException {
        inputStream = new ByteArrayInputStream(INPUT_2.getBytes());
        app.run(ARGS_2, inputStream, STD_OUTPUT);
        String fromFile1 = readFromFile(OUTPUT_FILE_1);

        assertEquals(INPUT_2 + STRING_NEWLINE, STD_OUTPUT.toString());
        assertEquals(FILE_CONTENT_1 + INPUT_2, fromFile1);
    }
}