package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.EchoException;
import sg.edu.nus.comp.cs4218.exception.TeeException;
import sg.edu.nus.comp.cs4218.impl.result.TeeResult;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

class TeeApplicationTest {

    private static final String INPUT_1 = "hello world";
    private static final String INPUT_2 = "hello world" + STRING_NEWLINE + "How are you";

    private static final String OUTPUT_FILE_1 = "output1.txt";
    private static final String OUTPUT_FILE_2 = "output2.txt";
    private static final String OUTPUT_FILE_3 = "output3.txt";

    private static final String FOLDER = "folder";

    private static final String FILE_CONTENT_1 = "This is old content.";
    private static final String FILE_CONTENT_2 = "";

    private InputStream inputStream;

    private final Path file1 = Paths.get(OUTPUT_FILE_1); // exists
    private final Path file2 = Paths.get(OUTPUT_FILE_2); // exists
    private final Path file3 = Paths.get(OUTPUT_FILE_3); // does not exist
    private final Path folder = Paths.get(FOLDER); // exists

    private final List<Path> paths = List.of(file1, file2, file3, folder);

    private TeeApplication app;

    private void createFileWithContent(Path path, String content) throws IOException {
        Files.createFile(path);
        BufferedWriter outputStream = new BufferedWriter(new FileWriter(path.toFile(), true));
        outputStream.append(content);
        outputStream.close();
    }

    private String readFromFile(String path) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line = null;
            List<String> result = new ArrayList<>();
            while((line = reader.readLine()) != null) {
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

        } catch (IOException e ) {
            fail(e.getMessage());
        }
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
    }

    @Test
    public void tee_WriteSingleLineToBothStdoutAndFileAppend_Success() throws TeeException {
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
    public void tee_WriteMultiLinesToBothStdoutAndFileAppend_Success() throws TeeException {
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
    public void tee_WriteToBothStdoutAndFileNoAppend_ShouldTruncateFile() throws TeeException {
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
    public void tee_WriteToStdoutOnlyNoAppend_Success() throws TeeException {
        inputStream = new ByteArrayInputStream(INPUT_1.getBytes());
        String output = app.teeFromStdin(false, inputStream);

        assertEquals(INPUT_1, output);
    }

    @Test
    public void tee_WriteToStdoutOnlyAppend_Success() throws TeeException {
        inputStream = new ByteArrayInputStream(INPUT_2.getBytes());
        String output = app.teeFromStdin(true, inputStream);

        assertEquals(INPUT_2, output);
    }

//    @Test
//    public void tee_FileIsDirectory_ShouldThrow() {
//
//    }
//
//    @Test
//    public void tee_FileNotFound_ShouldThrow() {
//
//    }
//
//    @Test
//    public void tee_ContentNull_ShouldThrow() {
//
//    }

    @Test
    public void tee_FilenameContainsNullValues_ShouldThrow() {
        String[] filenames = {OUTPUT_FILE_1, null};
        inputStream = new ByteArrayInputStream(INPUT_1.getBytes());
        Throwable error = assertThrows(TeeException.class, () -> app.teeFromStdin(false, inputStream , filenames));
        assertEquals("tee: " + ERR_INVALID_FILES, error.getMessage());
    }

    @Test
    public void tee_NoInputStream_ShouldThrow() {
        String[] filenames = {OUTPUT_FILE_1};
        Throwable error = assertThrows(TeeException.class, () -> app.teeFromStdin(false, null, filenames));
        assertEquals("tee: " + ERR_NO_ISTREAM, error.getMessage());
    }
}