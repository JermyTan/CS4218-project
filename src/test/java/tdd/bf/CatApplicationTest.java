package tdd.bf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_FLAG;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.impl.app.CatApplication;

public class CatApplicationTest {
    public static final String TEMP = "temp-cat";
    public static final Path TEMP_PATH = Paths.get(EnvironmentUtil.currentDirectory, TEMP);
    public static Deque<Path> files = new ArrayDeque<>();
    private OutputStream stderr;

    @BeforeAll
    static void createTemp() throws IOException {
        Files.createDirectory(TEMP_PATH);
    }

    @AfterAll
    static void deleteFiles() throws IOException {
        for (Path file : files) {
            Files.deleteIfExists(file);
        }
        Files.delete(TEMP_PATH);
    }

    private void captureErr() {
        stderr = new ByteArrayOutputStream();
        System.setErr(new PrintStream(stderr));
    }

    private String getErrOutput() {
        System.setErr(System.err);
        return stderr.toString();
    }

    private Path createFile(String name, String text) throws IOException {
        Path path = TEMP_PATH.resolve(name);
        Files.createFile(path);
        Files.write(path, text.getBytes());
        files.push(path);
        return path;
    }

    private String[] toArgs(String flag, String... files) {
        List<String> args = new ArrayList<>();
        if (!flag.isEmpty()) {
            args.add(STRING_STDIN_FLAG + flag);
        }
        for (String file : files) {
            if (file.equals(STRING_STDIN_FLAG)) {
                args.add(file);
            } else {
                args.add(getRelativePath(file).toString());
            }
        }
        return args.toArray(new String[0]);
    }

    private Path getRelativePath(String name) {
        return Paths.get(TEMP, name);
    }

    @Test
    void run_SingleStdinNullStdout_ThrowsException() throws AbstractApplicationException {
        ByteArrayOutputStream output = null;
        String text = String.join(STRING_NEWLINE,"Test line 1", "Test line 2", "Test line 3");
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        assertThrows(CatException.class, () -> new CatApplication().run(toArgs(STRING_EMPTY), inputStream, output));
    }

    @Test
    void run_NullStdinNullFilesNoFlag_ThrowsException() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = null;
        assertThrows(CatException.class, () -> new CatApplication().run(toArgs(STRING_EMPTY), inputStream, output));
    }

    @Test
    void run_NullStdinNullFilesFlag_ThrowsException() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = null;
        assertThrows(CatException.class, () -> new CatApplication().run(toArgs("n"), inputStream, output));
    }

    //catStdin cases
    @Test
    void run_SingleStdinNoFlag_DisplaysStdinContents() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String text = String.join(STRING_NEWLINE,"Test line 1", "Test line 2", "Test line 3");
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        new CatApplication().run(toArgs(STRING_EMPTY), inputStream, output);
        assertEquals(text + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_SingleStdinFlag_DisplaysNumberedStdinContents() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String text = String.join(STRING_NEWLINE,"Test line 1", "Test line 2", "Test line 3");
        String expectedText = String.join(STRING_NEWLINE,"1 Test line 1", "2 Test line 2", "3 Test line 3", STRING_EMPTY);
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        new CatApplication().run(toArgs("n"), inputStream, output);
        assertEquals(expectedText, output.toString());
    }

    @Test
    void run_SingleStdinDashNoFlag_DisplaysStdinContents() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String text = String.join(STRING_NEWLINE,"Test line 1", "Test line 2", "Test line 3");
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        new CatApplication().run(toArgs(STRING_EMPTY, STRING_STDIN_FLAG), inputStream, output);
        assertEquals(text + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_SingleStdinDashFlag_DisplaysNumberedStdinContents() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String text = String.join(STRING_NEWLINE,"Test line 1", "Test line 2", "Test line 3");
        String expectedText = String.join(STRING_NEWLINE,"1 Test line 1", "2 Test line 2", "3 Test line 3", STRING_EMPTY);
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        new CatApplication().run(toArgs("n", STRING_STDIN_FLAG), inputStream, output);
        assertEquals(expectedText, output.toString());
    }

    @Test
    void run_SingleEmptyStdinNoFlag_DisplaysEmpty() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String text = STRING_EMPTY;
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        new CatApplication().run(toArgs(STRING_EMPTY), inputStream, output);
        assertEquals(text, output.toString());
    }

    @Test
    void run_SingleEmptyStdinFlag_DisplaysEmpty() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String text = STRING_EMPTY;
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        new CatApplication().run(toArgs("n"), inputStream, output);
        assertEquals(text, output.toString());
    }

    //catFiles cases
    @Test
    void run_NonexistentFileNoFlag_OutputsError() throws AbstractApplicationException {
        captureErr();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String nonexistentFileName = "nonexistent_file.txt";

        new CatApplication().run(toArgs(STRING_EMPTY, nonexistentFileName), System.in, output);

        String expectedOutput = STRING_EMPTY;
        assertEquals(expectedOutput, output.toString());

        String expectedErr = new CatException(
                new InvalidDirectoryException(
                        getRelativePath(nonexistentFileName).toString(),
                        ERR_FILE_NOT_FOUND
                ).getMessage()
        ).getMessage() + STRING_NEWLINE;
        assertEquals(expectedErr, getErrOutput());
    }

    @Test
    void run_DirectoryNoFlag_OutputsError() throws AbstractApplicationException, IOException {
        captureErr();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String directoryName = "nonexistent_file.txt";
        Path path = Paths.get(EnvironmentUtil.currentDirectory, directoryName);

        if (Files.notExists(path)) {
            Files.createDirectory(path);
        }

        new CatApplication().run(toArgs(STRING_EMPTY, directoryName), System.in, output);

        Files.delete(path);

        String expectedOutput = STRING_EMPTY;
        assertEquals(expectedOutput, output.toString());

        String expectedErr = new CatException(
                new InvalidDirectoryException(
                        getRelativePath(directoryName).toString(),
                        ERR_FILE_NOT_FOUND
                ).getMessage()
        ).getMessage() + STRING_NEWLINE;
        assertEquals(expectedErr, getErrOutput());
    }

    @Test
    void run_SingleFileNoFlag_DisplaysFileContents() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileA.txt";
        String text = String.join(STRING_NEWLINE,"Test line 1", "Test line 2", "Test line 3");
        createFile(fileName, text);
        new CatApplication().run(toArgs(STRING_EMPTY, fileName), System.in, output);
        assertEquals(text + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_SingleFileFlag_DisplaysNumberedFileContents() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileB.txt";
        String text = String.join(STRING_NEWLINE,"Test line 1", "Test line 2", "Test line 3");
        String expectedText = String.join(STRING_NEWLINE,"1 Test line 1", "2 Test line 2", "3 Test line 3", STRING_EMPTY);
        createFile(fileName, text);
        new CatApplication().run(toArgs("n", fileName), System.in, output);
        assertEquals(expectedText, output.toString());
    }

    @Test
    void run_SingleEmptyFileNoFlag_DisplaysEmpty() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileC.txt";
        String text = STRING_EMPTY;
        createFile(fileName, text);
        new CatApplication().run(toArgs(STRING_EMPTY, fileName), System.in, output);
        assertEquals(text, output.toString());
    }

    @Test
    void run_SingleEmptyFileFlag_DisplaysEmpty() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileD.txt";
        String text = STRING_EMPTY;
        createFile(fileName, text);
        new CatApplication().run(toArgs("n", fileName), System.in, output);
        assertEquals(text, output.toString());
    }

    @Test
    void run_SingleFileUnknownFlag_ThrowsException() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileE.txt";
        String text = String.join(STRING_NEWLINE,"Test line 1", "Test line 2", "Test line 3");
        createFile(fileName, text);
        assertThrows(CatException.class, () -> new CatApplication().run(toArgs("a", fileName), System.in, output));
    }

    @Test
    void run_MultipleFilesNoFlag_DisplaysCatFileContents() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileF.txt";
        String fileName2 = "fileG.txt";
        String text1 = String.join(STRING_NEWLINE,"Test line 1.1", "Test line 1.2", "Test line 1.3");
        String text2 = String.join(STRING_NEWLINE,"Test line 2.1", "Test line 2.2");

        createFile(fileName1, text1);
        createFile(fileName2, text2);

        String expectedText = String.join(STRING_NEWLINE,text1, text2, STRING_EMPTY);

        new CatApplication().run(toArgs(STRING_EMPTY, fileName1, fileName2), System.in, output);
        assertEquals(expectedText, output.toString());
    }

    @Test
    void run_MultipleFilesFlag_DisplaysNumberedCatFileContents() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileH.txt";
        String fileName2 = "fileI.txt";
        String text1 = String.join(STRING_NEWLINE,"Test line 1.1", "Test line 1.2", "Test line 1.3");
        String text2 = String.join(STRING_NEWLINE,"Test line 2.1", "Test line 2.2");

        createFile(fileName1, text1);
        createFile(fileName2, text2);

        String expectedText = String.join(
                STRING_NEWLINE,
                "1 Test line 1.1",
                "2 Test line 1.2",
                "3 Test line 1.3",
                "1 Test line 2.1",
                "2 Test line 2.2",
                STRING_EMPTY
        );

        new CatApplication().run(toArgs("n", fileName1, fileName2), System.in, output);
        assertEquals(expectedText, output.toString());
    }

    @Test
    void run_MultipleEmptyFilesNoFlag_DisplaysEmpty() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileJ.txt";
        String fileName2 = "fileK.txt";
        String text = STRING_EMPTY;

        createFile(fileName1, text);
        createFile(fileName2, text);

        new CatApplication().run(toArgs(STRING_EMPTY, fileName1, fileName2), System.in, output);
        assertEquals(text, output.toString());
    }

    @Test
    void run_MultipleEmptyFilesFlag_DisplaysEmpty() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileL.txt";
        String fileName2 = "fileM.txt";
        String text = STRING_EMPTY;

        createFile(fileName1, text);
        createFile(fileName2, text);

        new CatApplication().run(toArgs("n", fileName1, fileName2), System.in, output);
        assertEquals(text, output.toString());
    }

    //catFilesAndStdin cases
    @Test
    void run_SingleStdinNonexistentFileNoFlag_OutputsError() throws AbstractApplicationException {
        captureErr();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String stdinText = String.join(STRING_NEWLINE,"Test line 1.1", "Test line 1.2", "Test line 1.3");
        InputStream inputStream = new ByteArrayInputStream(stdinText.getBytes());
        String nonexistentFileName = "nonexistent_file.txt";
        new CatApplication().run(toArgs(STRING_EMPTY, nonexistentFileName), inputStream, output);

        String expectedOutput = STRING_EMPTY;
        assertEquals(expectedOutput, output.toString());

        String expectedErr = new CatException(
                new InvalidDirectoryException(
                        getRelativePath(nonexistentFileName).toString(),
                        ERR_FILE_NOT_FOUND
                ).getMessage()
        ).getMessage() + STRING_NEWLINE;
        assertEquals(expectedErr, getErrOutput());
    }

    @Test
    void run_SingleStdinDirectoryNoFlag_OutputsError() throws AbstractApplicationException, IOException {
        captureErr();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String stdinText = String.join(STRING_NEWLINE,"Test line 1.1", "Test line 1.2", "Test line 1.3");
        InputStream inputStream = new ByteArrayInputStream(stdinText.getBytes());
        String directoryName = "nonexistent_file.txt";
        Path path = Paths.get(EnvironmentUtil.currentDirectory, directoryName);
        if (Files.notExists(path)) {
            Files.createDirectory(path);
        }

        new CatApplication().run(toArgs(STRING_EMPTY, directoryName), inputStream, output);

        Files.delete(path);

        String expectedOutput = STRING_EMPTY;
        assertEquals(expectedOutput, output.toString());

        String expectedErr = new CatException(
                new InvalidDirectoryException(
                        getRelativePath(directoryName).toString(),
                        ERR_FILE_NOT_FOUND
                ).getMessage()
        ).getMessage() + STRING_NEWLINE;
        assertEquals(expectedErr, getErrOutput());
    }

    @Test
    void run_SingleStdinDashSingleFileNoFlag_DisplaysCatStdinFileContents() throws IOException,
            AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String stdinText = String.join(STRING_NEWLINE,"Test line 1.1", "Test line 1.2", "Test line 1.3");
        InputStream inputStream = new ByteArrayInputStream(stdinText.getBytes());
        String fileName = "fileN.txt";
        String fileText = String.join(STRING_NEWLINE,"Test line 2.1", "Test line 2.2");

        createFile(fileName, fileText);

        String expectedText = String.join(STRING_NEWLINE, stdinText, fileText, STRING_EMPTY);

        new CatApplication().run(toArgs(STRING_EMPTY, STRING_STDIN_FLAG, fileName), inputStream, output);
        assertEquals(expectedText, output.toString());
    }

    @Test
    void run_SingleFileSingleStdinDashNoFlag_DisplaysCatFileStdinContents() throws IOException,
            AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileText = String.join(STRING_NEWLINE,"Test line 1.1", "Test line 1.2", "Test line 1.3");
        String fileName = "fileO.txt";
        createFile(fileName, fileText);

        String stdinText = String.join(STRING_NEWLINE,"Test line 2.1", "Test line 2.2");
        InputStream inputStream = new ByteArrayInputStream(stdinText.getBytes());

        String expectedText = String.join(STRING_NEWLINE, fileText, stdinText, STRING_EMPTY);

        new CatApplication().run(toArgs(STRING_EMPTY, fileName, STRING_STDIN_FLAG), inputStream, output);
        assertEquals(expectedText , output.toString());
    }
}

