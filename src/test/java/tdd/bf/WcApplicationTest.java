package tdd.bf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.impl.app.WcApplication;

public class WcApplicationTest {
    public static final String TEMP = "temp-wc";
    public static final Path TEMP_PATH = Paths.get(EnvironmentUtil.currentDirectory, TEMP);
    public static String currPathString;
    public static Deque<Path> files = new ArrayDeque<>();

    @BeforeEach
    void changeDirectory() throws IOException {
        currPathString = EnvironmentUtil.currentDirectory;
        Files.createDirectory(TEMP_PATH);
        EnvironmentUtil.setCurrentDirectory(TEMP_PATH.toString());
    }

    @AfterEach
    void deleteFiles() throws IOException {
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
            args.add("-" + flag);
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
        new WcApplication().run(toArgs("", fileName), System.in, output);
        assertArrayEquals(("       4       8" + String.format("%8s", fileSize) + " " + fileName + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleFileLinesFlag_DisplaysLinesFilename() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileB.txt";
        createFile(fileName);
        new WcApplication().run(toArgs("l", fileName), System.in, output);
        assertArrayEquals(("       4 " + fileName + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleFileWordsFlag_DisplaysWordsFilename() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileC.txt";
        createFile(fileName);
        new WcApplication().run(toArgs("w", fileName), System.in, output);
        assertArrayEquals(("       8 " + fileName + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleFileBytesFlag_DisplaysBytesFilename() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileD.txt";
        Path filePath = createFile(fileName);
        long fileSize = Files.size(filePath);
        new WcApplication().run(toArgs("c", fileName), System.in, output);
        assertArrayEquals((String.format("%8s", fileSize) + " " + fileName + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleFileAllFlags_DisplaysLinesWordsBytesFilename() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileE.txt";
        Path filePath = createFile(fileName);
        long fileSize = Files.size(filePath);
        new WcApplication().run(toArgs("clw", fileName), System.in, output);
        assertArrayEquals(("       4       8" + String.format("%8s", fileSize) + " " + fileName + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleFileUnknownFlag_Throws() throws IOException {
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
        new WcApplication().run(toArgs(""), inputStream, output);
        assertArrayEquals(("       4       8" + String.format("%8s", fileSize) + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleInputDash_DisplaysLinesWordsBytes() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String input = "First line\nSecond line\nThird line\nFourth line\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        long fileSize = input.getBytes().length;
        new WcApplication().run(toArgs("", "-"), inputStream, output);
        assertArrayEquals(("       4       8" + String.format("%8s", fileSize) + " -" + STRING_NEWLINE).getBytes(), output.toByteArray());
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
        new WcApplication().run(toArgs("", fileGName, fileHName), System.in, output);
        assertArrayEquals(("       4       8" + String.format("%8s", fileGSize) + " " + fileGName + STRING_NEWLINE
                + "       4       8" + String.format("%8s", fileHSize) + " " + fileHName + STRING_NEWLINE
                + "       8      16" + String.format("%8s", fileGSize + fileHSize) + " total" + STRING_NEWLINE).getBytes(), output.toByteArray());
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
        new WcApplication().run(toArgs("", fileIName, "-"), inputStream, output);
        assertArrayEquals(("       4       8" + String.format("%8s", fileISize) + " " + fileIName + STRING_NEWLINE
                + "       4       8" + String.format("%8s", inputSize) + " -" + STRING_NEWLINE
                + "       8      16" + String.format("%8s", fileISize + inputSize) + " total" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleFileAndNonexistentFile_DisplaysLinesWordsBytesFilenameErrorMessageTotal() throws IOException, AbstractApplicationException{
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileJName = "fileJ.txt";
        Path fileJPath = createFile(fileJName);
        long fileJSize = Files.size(fileJPath);
        String nonexistentFileName = "nonexistent_file.txt";
        new WcApplication().run(toArgs("", fileJName, nonexistentFileName), System.in, output);
        assertArrayEquals(("       4       8" + String.format("%8s", fileJSize) + " " + fileJName + STRING_NEWLINE
                + "wc: " + ERR_FILE_NOT_FOUND + STRING_NEWLINE
                + "       4       8" + String.format("%8s", fileJSize) + " total" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleInputAndNonexistentFile_DisplaysLinesWordsBytesDashErrorMessageTotal() throws AbstractApplicationException{
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String input = "First line\nSecond line\nThird line\nFourth line\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        long inputSize = input.getBytes().length;
        String nonexistentFileName = "nonexistent_file.txt";
        new WcApplication().run(toArgs("", "-", nonexistentFileName), inputStream, output);
        assertArrayEquals(("       4       8" + String.format("%8s", inputSize) + " -" + STRING_NEWLINE
                + "wc: " + ERR_FILE_NOT_FOUND + STRING_NEWLINE
                + "       4       8" + String.format("%8s", inputSize) + " total" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_FilenameIsNull_Throws() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertThrows(Exception.class, () -> new WcApplication().run(toArgs("", (String[]) null), System.in, output));
    }

    @Test
    void run_OutputStreamIsNull_Throws() throws IOException {
        String fileKName = "fileK.txt";
        createFile(fileKName);
        assertThrows(Exception.class, () -> new WcApplication().run(toArgs("", fileKName), System.in, null));
    }

    @Test
    void run_InputStreamIsNull_Throws() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertThrows(Exception.class, () -> new WcApplication().run(toArgs("", ""), null, output));
    }

    @Test
    void run_FilenameIsDirectory_DisplaysErrorMessage() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String folderName = "folder";
        createDirectory(folderName);
        new WcApplication().run(toArgs("", folderName), System.in, output);
        assertArrayEquals(("wc: " + ERR_IS_DIR + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_NonexistentFile_DisplaysErrorMessage() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String nonexistentFileName = "nonexistent_file.txt";
        new WcApplication().run(toArgs("", nonexistentFileName), System.in, output);
        assertArrayEquals(("wc: " + ERR_FILE_NOT_FOUND + STRING_NEWLINE).getBytes(), output.toByteArray());
    }
}
