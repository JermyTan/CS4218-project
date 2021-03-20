package tdd.bf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_TAB;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_FLAG;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.impl.app.WcApplication;

public class WcApplicationTest {
    public static final String TEMP = "temp-wc";
    public static final Path TEMP_PATH = Paths.get(EnvironmentUtil.currentDirectory, TEMP);
    public static String currPathString;
    public static Deque<Path> files = new ArrayDeque<>();
    private OutputStream stderr;

    private void captureErr() {
        stderr = new ByteArrayOutputStream();
        System.setErr(new PrintStream(stderr));
    }

    private String getErrOutput() {
        System.setErr(System.err);
        return stderr.toString();
    }

    @BeforeEach
    void changeDirectory() throws Exception {
        currPathString = EnvironmentUtil.currentDirectory;
        Files.createDirectory(TEMP_PATH);
        EnvironmentUtil.setCurrentDirectory(TEMP_PATH.toString());
    }

    @AfterEach
    void deleteFiles() throws Exception {
        EnvironmentUtil.setCurrentDirectory(currPathString);
        Files.walk(TEMP_PATH)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    private Path createFile(String name) throws IOException {
        String content = "First line\nSecond line\nThird line\nFourth line\n";
        Path path = TEMP_PATH.resolve(name);
        Files.createFile(path);
        Files.write(path, content.getBytes());
        files.push(path);
        return path;
    }

    private void createDirectory(String folder) throws IOException {
        Path path = TEMP_PATH.resolve(folder);
        Files.createDirectory(path);
        files.push(path);
    }

    private String[] toArgs(String flag, String... files) {
        List<String> args = new ArrayList<>();
        if (!flag.isEmpty()) {
            args.add(STRING_STDIN_FLAG + flag);
        }
        for (String file : files) {
            args.add(Paths.get(file).toString());
        }
        return args.toArray(new String[0]);
    }

    @Test
    void run_SingleFileNoFlags_DisplaysLinesWordsBytesFilename() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileA.txt";
        Path filePath = createFile(fileName);
        long fileSize = Files.size(filePath);
        new WcApplication().run(toArgs(STRING_EMPTY, fileName), System.in, output);
        assertEquals("4\t8\t" + fileSize + CHAR_TAB + fileName + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_SingleFileLinesFlag_DisplaysLinesFilename() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileB.txt";
        createFile(fileName);
        new WcApplication().run(toArgs("l", fileName), System.in, output);
        assertEquals("4\t" + fileName + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_SingleFileWordsFlag_DisplaysWordsFilename() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileC.txt";
        createFile(fileName);
        new WcApplication().run(toArgs("w", fileName), System.in, output);
        assertEquals("8\t" + fileName + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_SingleFileBytesFlag_DisplaysBytesFilename() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileD.txt";
        Path filePath = createFile(fileName);
        long fileSize = Files.size(filePath);
        new WcApplication().run(toArgs("c", fileName), System.in, output);
        assertEquals(String.valueOf(fileSize) + CHAR_TAB + fileName + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_SingleFileAllFlags_DisplaysLinesWordsBytesFilename() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileE.txt";
        Path filePath = createFile(fileName);
        long fileSize = Files.size(filePath);
        new WcApplication().run(toArgs("clw", fileName), System.in, output);
        assertEquals("4\t8\t" + fileSize + CHAR_TAB + fileName + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_SingleFileUnknownFlag_ThrowsException() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileF.txt";
        createFile(fileName);
        assertThrows(WcException.class, () -> new WcApplication().run(toArgs("x", fileName), System.in, output));
    }

    @Test
    void run_SingleInputNoFileSpecified_DisplaysLinesWordsBytes() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String input = "First line\nSecond line\nThird line\nFourth line\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        long fileSize = input.getBytes().length;
        new WcApplication().run(toArgs(STRING_EMPTY), inputStream, output);
        assertEquals("4\t8\t" + fileSize + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_SingleInputDash_DisplaysLinesWordsBytes() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String input = "First line\nSecond line\nThird line\nFourth line\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        long fileSize = input.getBytes().length;
        new WcApplication().run(toArgs(STRING_EMPTY, STRING_STDIN_FLAG), inputStream, output);
        assertEquals("4\t8\t" + fileSize + WcApplication.STDIN_LABEL + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_MultipleFiles_DisplaysLinesWordsBytesFilenameTotal() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileGName = "fileG.txt";
        String fileHName = "fileH.txt";
        Path fileGPath = createFile(fileGName);
        Path fileHPath = createFile(fileHName);
        long fileGSize = Files.size(fileGPath);
        long fileHSize = Files.size(fileHPath);
        new WcApplication().run(toArgs(STRING_EMPTY, fileGName, fileHName), System.in, output);
        assertEquals("4\t8\t" + fileGSize + CHAR_TAB + fileGName + STRING_NEWLINE
                + "4\t8\t" + fileHSize + CHAR_TAB + fileHName + STRING_NEWLINE
                + "8\t16\t" + (fileGSize + fileHSize) + CHAR_TAB + WcApplication.TOTAL_LABEL + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_SingleFileAndSingleInput_DisplaysLinesWordsBytesTotal() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileIName = "fileI.txt";
        Path fileIPath = createFile(fileIName);
        long fileISize = Files.size(fileIPath);
        String input = "First line\nSecond line\nThird line\nFourth line\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        long inputSize = input.getBytes().length;
        new WcApplication().run(toArgs(STRING_EMPTY, fileIName, STRING_STDIN_FLAG), inputStream, output);
        assertEquals("4\t8\t" + fileISize + CHAR_TAB + fileIName + STRING_NEWLINE
                + "4\t8\t" + inputSize + WcApplication.STDIN_LABEL + STRING_NEWLINE
                + "8\t16\t" + (fileISize + inputSize) + CHAR_TAB + WcApplication.TOTAL_LABEL + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_SingleFileAndNonexistentFile_DisplaysLinesWordsBytesFilenameErrorMessageTotal() throws IOException, AbstractApplicationException {
        captureErr();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileJName = "fileJ.txt";
        Path fileJPath = createFile(fileJName);
        long fileJSize = Files.size(fileJPath);
        String nonexistentFileName = "nonexistent_file.txt";
        new WcApplication().run(toArgs(STRING_EMPTY, fileJName, nonexistentFileName), System.in, output);

        String expectedOutput = "4\t8\t" + fileJSize + CHAR_TAB + fileJName + STRING_NEWLINE
                + "4\t8\t" + fileJSize + CHAR_TAB + WcApplication.TOTAL_LABEL + STRING_NEWLINE;
        assertEquals(expectedOutput, output.toString());

        String expectedErr = new WcException(
                new InvalidDirectoryException(
                        nonexistentFileName,
                        ERR_FILE_NOT_FOUND
                ).getMessage()
        ).getMessage();
        assertEquals(expectedErr + STRING_NEWLINE, getErrOutput());
    }

    @Test
    void run_SingleInputAndNonexistentFile_DisplaysLinesWordsBytesDashErrorMessageTotal() throws AbstractApplicationException {
        captureErr();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String input = "First line\nSecond line\nThird line\nFourth line\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        long inputSize = input.getBytes().length;
        String nonexistentFileName = "nonexistent_file.txt";
        new WcApplication().run(toArgs(STRING_EMPTY, STRING_STDIN_FLAG, nonexistentFileName), inputStream, output);

        String expectedOutput = "4\t8\t" + inputSize + WcApplication.STDIN_LABEL + STRING_NEWLINE
                + "4\t8\t" + inputSize + CHAR_TAB + WcApplication.TOTAL_LABEL + STRING_NEWLINE;
        assertEquals(expectedOutput, output.toString());

        String expectedErr = new WcException(
                new InvalidDirectoryException(
                        nonexistentFileName,
                        ERR_FILE_NOT_FOUND
                ).getMessage()
        ).getMessage();
        assertEquals(expectedErr + STRING_NEWLINE, getErrOutput());
    }

    @Test
    void run_FilenameIsNull_ThrowsException() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertThrows(Exception.class, () -> new WcApplication().run(toArgs(STRING_EMPTY, (String[]) null), System.in, output));
    }

    @Test
    void run_OutputStreamIsNull_ThrowsException() throws IOException {
        String fileKName = "fileK.txt";
        createFile(fileKName);
        assertThrows(Exception.class, () -> new WcApplication().run(toArgs(STRING_EMPTY, fileKName), System.in, null));
    }

    @Test
    void run_InputStreamIsNull_ThrowsException() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertThrows(Exception.class, () -> new WcApplication().run(toArgs(STRING_EMPTY), null, output));
    }

    @Test
    void run_FilenameIsDirectory_DisplaysErrorMessage() throws IOException, AbstractApplicationException {
        captureErr();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String folderName = "folder";
        createDirectory(folderName);
        new WcApplication().run(toArgs(STRING_EMPTY, folderName), System.in, output);

        String expectedErr = new WcException(
                new InvalidDirectoryException(
                        folderName,
                        ERR_IS_DIR
                ).getMessage()
        ).getMessage();
        assertEquals(expectedErr + STRING_NEWLINE, getErrOutput());
    }

    @Test
    void run_NonexistentFile_DisplaysErrorMessage() throws AbstractApplicationException {
        captureErr();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String nonexistentFileName = "nonexistent_file.txt";
        new WcApplication().run(toArgs(STRING_EMPTY, nonexistentFileName), System.in, output);


        String expectedErr = new WcException(
                new InvalidDirectoryException(
                        nonexistentFileName,
                        ERR_FILE_NOT_FOUND
                ).getMessage()
        ).getMessage();
        assertEquals(expectedErr + STRING_NEWLINE, getErrOutput());
    }
}
