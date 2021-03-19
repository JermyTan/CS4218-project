package tdd.ef2;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.impl.app.PasteApplication;

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
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class PasteApplicationTest {
    public static final String TEMP = "temp-paste";
    public static final Path TEMP_PATH = Paths.get(EnvironmentUtil.currentDirectory, TEMP);
    public static Deque<Path> files = new ArrayDeque<>();

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
            args.add("-" + flag);
        }
        for (String file : files) {
            if (file.equals("-")) {
                args.add(file);
            } else {
                args.add(Paths.get(TEMP, file).toString());
            }
        }
        return args.toArray(new String[0]);
    }

    @Test
    void run_SingleStdinNullStdout_ThrowsException() throws AbstractApplicationException {
        ByteArrayOutputStream output = null;
        String text = "Test line 1\nTest line 2\nTest line 3";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        assertThrows(PasteException.class, () -> new PasteApplication().run(toArgs(""), inputStream, output));
    }

    @Test
    void run_NullStdinNullFilesNoFlag_ThrowsException() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = null;
        assertThrows(PasteException.class, () -> new PasteApplication().run(toArgs(""), inputStream, output));
    }

    @Test
    void run_NullStdinNullFilesFlag_ThrowsException() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = null;
        assertThrows(PasteException.class, () -> new PasteApplication().run(toArgs("n"), inputStream, output));
    }

    //mergeStdin cases
    @Test
    void run_SingleStdinNoFlag_DisplaysStdinContents() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String text = "Test line 1\nTest line 2\nTest line 3";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        new PasteApplication().run(toArgs(""), inputStream, output);
        assertArrayEquals((text + STRING_NEWLINE).getBytes(), output.toByteArray());
    }


    @Test
    void run_SingleStdinFlag_DisplaysNonParallelStdinContents() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String text = "Test line 1\nTest line 2\nTest line 3";
        String expectedText = "Test line 1\tTest line 2\tTest line 3";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        new PasteApplication().run(toArgs("s"), inputStream, output);
        assertArrayEquals((expectedText + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleStdinDashNoFlag_DisplaysStdinContents() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String text = "Test line 1\nTest line 2\nTest line 3";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        new PasteApplication().run(toArgs("", "-"), inputStream, output);
        assertArrayEquals((text + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleStdinDashFlag_DisplaysNonParallelStdinContents() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String text = "Test line 1\nTest line 2\nTest line 3";
        String expectedText = "Test line 1\tTest line 2\tTest line 3";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        new PasteApplication().run(toArgs("s", "-"), inputStream, output);
        assertArrayEquals((expectedText + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleEmptyStdinNoFlag_DisplaysEmpty() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String text = "";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        new PasteApplication().run(toArgs(""), inputStream, output);
        assertArrayEquals((text + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleEmptyStdinFlag_DisplaysEmpty() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String text = "";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes());
        new PasteApplication().run(toArgs("s"), inputStream, output);
        assertArrayEquals((text + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    //mergeFiles cases
    @Test
    void run_NonexistentFileNoFlag_ThrowsException() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String nonexistentFileName = "nonexistent_file.txt";
        assertThrows(PasteException.class, () -> new PasteApplication().run(toArgs("", nonexistentFileName),
                System.in, output));
    }

    @Test
    void run_DirectoryNoFlag_ThrowsException() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String directoryName = "nonexistent_file.txt";
        Path path = Paths.get(EnvironmentUtil.currentDirectory, directoryName);
        Files.createDirectory(path);
        assertThrows(PasteException.class, () -> new PasteApplication().run(toArgs("", directoryName),
                System.in, output));
        Files.delete(path);
    }

    @Test
    void run_SingleFileNoFlag_DisplaysFileContents() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileA.txt";
        String text = "Test line 1\nTest line 2\nTest line 3";
        Path filePath = createFile(fileName, text);
        File file = new File(filePath.toString());
        new PasteApplication().run(toArgs("", fileName), System.in, output);
        assertArrayEquals((text + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleFileFlag_DisplaysNonParallelFileContents() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileB.txt";
        String text = "Test line 1\nTest line 2\nTest line 3";
        String expectedText = "Test line 1\tTest line 2\tTest line 3";
        createFile(fileName, text);
        new PasteApplication().run(toArgs("s", fileName), System.in, output);
        assertArrayEquals((expectedText + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleEmptyFileNoFlag_DisplaysEmpty() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileC.txt";
        String text = "";
        Path filePath = createFile(fileName, text);
        File file = new File(filePath.toString());
        new PasteApplication().run(toArgs("", fileName), System.in, output);
        assertArrayEquals((text + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleEmptyFileFlag_DisplaysEmpty() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileD.txt";
        String text = "";
        createFile(fileName, text);
        new PasteApplication().run(toArgs("s", fileName), System.in, output);
        assertArrayEquals((text + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleFileUnknownFlag_Throws() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileE.txt";
        String text = "Test line 1\nTest line 2\nTest line 3";
        createFile(fileName, text);
        assertThrows(PasteException.class, () -> new PasteApplication().run(toArgs("a", fileName), System.in, output));
    }

    @Test
    void run_MultipleFilesNoFlag_DisplaysMergedFileContents() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileF.txt";
        String fileName2 = "fileG.txt";
        String text1 = "Test line 1.1\nTest line 1.2\nTest line 1.3";
        String text2 = "Test line 2.1\nTest line 2.2";
        String expectedText = "Test line 1.1\tTest line 2.1\nTest line 1.2\tTest line 2.2\nTest line 1.3";
        Path filePath1 = createFile(fileName1, text1);
        File file1 = new File(filePath1.toString());
        Path filePath2 = createFile(fileName2, text2);
        File file2 = new File(filePath2.toString());
        new PasteApplication().run(toArgs("", fileName1, fileName2), System.in, output);
        assertArrayEquals((expectedText + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_MultipleFilesFlag_DisplaysNonParallelMergedFileContents() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileH.txt";
        String fileName2 = "fileI.txt";
        String text1 = "Test line 1.1\nTest line 1.2\nTest line 1.3";
        String text2 = "Test line 2.1\nTest line 2.2";
        String expectedText = "Test line 1.1\tTest line 1.2\tTest line 1.3\nTest line 2.1\tTest line 2.2";
        Path filePath1 = createFile(fileName1, text1);
        File file1 = new File(filePath1.toString());
        Path filePath2 = createFile(fileName2, text2);
        File file2 = new File(filePath2.toString());
        new PasteApplication().run(toArgs("s", fileName1, fileName2), System.in, output);
        assertArrayEquals((expectedText + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_MultipleEmptyFilesNoFlag_DisplaysEmpty() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileJ.txt";
        String fileName2 = "fileK.txt";
        String text = "";
        Path filePath1 = createFile(fileName1, text);
        File file1 = new File(filePath1.toString());
        Path filePath2 = createFile(fileName2, text);
        File file2 = new File(filePath2.toString());
        new PasteApplication().run(toArgs("", fileName1, fileName2), System.in, output);
        assertArrayEquals((text + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_MultipleEmptyFilesFlag_DisplaysEmpty() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileL.txt";
        String fileName2 = "fileM.txt";
        String text = "";
        Path filePath1 = createFile(fileName1, text);
        File file1 = new File(filePath1.toString());
        Path filePath2 = createFile(fileName2, text);
        File file2 = new File(filePath2.toString());
        new PasteApplication().run(toArgs("s", fileName1, fileName2), System.in, output);
        assertArrayEquals((text + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    //mergeFilesAndStdin cases
    @Test
    void run_SingleStdinNonexistentFileNoFlag_ThrowsException() throws AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String stdinText = "Test line 1.1\nTest line 1.2\nTest line 1.3";
        InputStream inputStream = new ByteArrayInputStream(stdinText.getBytes());
        String nonexistentFileName = "nonexistent_file.txt";
        assertThrows(PasteException.class, () -> new PasteApplication().run(toArgs("", nonexistentFileName),
                inputStream, output));
    }

    @Test
    void run_SingleStdinDirectoryNoFlag_ThrowsException() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String stdinText = "Test line 1.1\nTest line 1.2\nTest line 1.3";
        InputStream inputStream = new ByteArrayInputStream(stdinText.getBytes());
        String directoryName = "nonexistent_file.txt";
        Path path = Paths.get(EnvironmentUtil.currentDirectory, directoryName);
        Files.createDirectory(path);
        assertThrows(PasteException.class, () -> new PasteApplication().run(toArgs("", directoryName),
                inputStream, output));
        Files.delete(path);
    }

    @Test
    void run_SingleStdinDashSingleFileNoFlag_DisplaysMergedStdinFileContents() throws IOException,
            AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String stdinText = "Test line 1.1\nTest line 1.2\nTest line 1.3";
        InputStream inputStream = new ByteArrayInputStream(stdinText.getBytes());
        String fileName = "fileN.txt";
        String fileText = "Test line 2.1\nTest line 2.2";
        Path filePath = createFile(fileName, fileText);
        File file = new File(filePath.toString());
        String expectedText = "Test line 1.1\tTest line 2.1\nTest line 1.2\tTest line 2.2\nTest line 1.3";
        new PasteApplication().run(toArgs("", "-", fileName), inputStream, output);
        assertArrayEquals((expectedText + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void run_SingleFileSingleStdinDashNoFlag_DisplaysNonParallelMergedFileStdinContents() throws IOException,
            AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileText = "Test line 1.1\nTest line 1.2\nTest line 1.3";
        String fileName = "fileO.txt";
        Path filePath = createFile(fileName, fileText);
        File file = new File(filePath.toString());
        String stdinText = "Test line 2.1\nTest line 2.2";
        InputStream inputStream = new ByteArrayInputStream(stdinText.getBytes());
        String expectedText = "Test line 1.1\tTest line 2.1\nTest line 1.2\tTest line 2.2\nTest line 1.3";
        new PasteApplication().run(toArgs("", fileName, "-"), inputStream, output);
        assertArrayEquals((expectedText + STRING_NEWLINE).getBytes(), output.toByteArray());
    }
}
