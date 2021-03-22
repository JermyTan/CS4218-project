package tdd.ef2;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.UniqException;
import sg.edu.nus.comp.cs4218.impl.app.UniqApplication;

public class UniqApplicationTest {
    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = String.join(STRING_FILE_SEP,
            EnvironmentUtil.currentDirectory,
            RESOURCES_PATH,
            "UniqApplicationTest");
    private static final Path TEST_PATH = Path.of(TEST_DIR);

    private static final String testInput = "Hello World" + STRING_NEWLINE +
            "Hello World" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE +
            "Bob" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE +
            "Bob" + STRING_NEWLINE;
    private static final String withoutFlagOutput = "Hello World" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE +
            "Bob" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE +
            "Bob" + STRING_NEWLINE;
    private static final String withCountFlagOutput = "2 Hello World" + STRING_NEWLINE +
            "2 Alice" + STRING_NEWLINE +
            "1 Bob" + STRING_NEWLINE +
            "1 Alice" + STRING_NEWLINE +
            "1 Bob" + STRING_NEWLINE;
    private static final String withDuplicateFlagOutput = "Hello World" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE;
    private static final String withAllDuplicateFlagOutput = "Hello World" + STRING_NEWLINE +
            "Hello World" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE +
            "Alice" + STRING_NEWLINE;
    private static final String withCountAndDuplicateFlagsOutput = "2 Hello World" + STRING_NEWLINE +
            "2 Alice" + STRING_NEWLINE;

    @BeforeAll
    static void setupBeforeAll() throws IOException {
        if (!Files.exists(TEST_PATH)) {
            Files.createDirectory(TEST_PATH);
        }

        EnvironmentUtil.currentDirectory = TEST_DIR;
    }

    @AfterAll
    static void tearDownAfterAll() throws IOException {
        EnvironmentUtil.currentDirectory = ORIGINAL_DIR;
    }

    @AfterEach
    void deleteTemp() throws IOException {
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

    private Path createFile(String name) throws IOException {
        Path path = TEST_PATH.resolve(name);
        Files.createFile(path);
        return path;
    }

    private void writeToFile(Path path, String content) throws IOException {
        Files.write(path, content.getBytes());
    }

    @Test
    void run_NoFilesWithoutFlag_ReadsFromInputAndDisplaysAdjacentLines() {
        String[] args = {};
        InputStream stdin = new ByteArrayInputStream(testInput.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            new UniqApplication().run(args, stdin, outputStream);
            assertEquals(withoutFlagOutput, outputStream.toString());
        });
    }

    @Test
    void run_NoFilesWithCountFlag_ReadsFromInputAndDisplaysCountOfAdjacentLines() {
        String[] args = {"-c"};
        InputStream stdin = new ByteArrayInputStream(testInput.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            new UniqApplication().run(args, stdin, outputStream);
            assertEquals(withCountFlagOutput, outputStream.toString());
        });
    }

    @Test
    void run_NoFilesWithDuplicateFlag_ReadsFromInputAndDisplaysRepeatedAdjacentLinesOnlyOnce() {
        String[] args = {"-d"};
        InputStream stdin = new ByteArrayInputStream(testInput.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            new UniqApplication().run(args, stdin, outputStream);
            assertEquals(withDuplicateFlagOutput, outputStream.toString());
        });
    }

    @Test
    void run_NoFilesWithAllDuplicateFlag_ReadsFromInputAndDisplaysRepeatedAdjacentLinesRepeatedly() {
        String[] args = {"-D"};
        InputStream stdin = new ByteArrayInputStream(testInput.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            new UniqApplication().run(args, stdin, outputStream);
            assertEquals(withAllDuplicateFlagOutput, outputStream.toString());
        });
    }

    @Test
    void run_NoFilesWithDuplicateAndAllDuplicateFlags_ReadsFromInputAndDisplaysRepeatedAdjacentLinesRepeatedly() {
        String[] args = {"-d", "-D"};
        InputStream stdin = new ByteArrayInputStream(testInput.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            new UniqApplication().run(args, stdin, outputStream);
            assertEquals(withAllDuplicateFlagOutput, outputStream.toString());
        });
    }

    @Test
    void run_NoFilesWithCountAndDuplicateFlags_ReadsFromInputAndDisplaysCountOfRepeatedAdjacentLinesOnlyOnce() {
        String[] args = {"-c", "-d"};
        InputStream stdin = new ByteArrayInputStream(testInput.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            new UniqApplication().run(args, stdin, outputStream);
            assertEquals(withCountAndDuplicateFlagsOutput, outputStream.toString());
        });
    }

    @Test
    void run_NoFilesWithUnknownFlag_ThrowsException() {
        String[] args = {"-x"};
        InputStream stdin = new ByteArrayInputStream(testInput.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(UniqException.class, () -> new UniqApplication().run(args, stdin, outputStream));
    }

    @Test
    void run_NonemptyInputFile_ReadsFileAndDisplaysAdjacentLines() throws IOException {
        Path inputPath = createFile("input_file.txt");
        writeToFile(inputPath, testInput);
        String[] args = {"input_file.txt"};
        InputStream stdin = new ByteArrayInputStream(STRING_EMPTY.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            new UniqApplication().run(args, stdin, outputStream);
            assertEquals(withoutFlagOutput, outputStream.toString());
        });
    }

    @Test
    void run_EmptyInputFile_ReadsFileAndDisplaysEmptyLine() throws IOException {
        createFile("input_file.txt");
        String[] args = {"input_file.txt"};
        InputStream stdin = new ByteArrayInputStream(STRING_EMPTY.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            new UniqApplication().run(args, stdin, outputStream);
            assertEquals(STRING_EMPTY, outputStream.toString());
        });
    }

    @Test
    void run_NonexistentInputFile_ThrowsException() {
        String[] args = {"nonexistent_file.txt"};
        InputStream stdin = new ByteArrayInputStream(STRING_EMPTY.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(UniqException.class, () -> new UniqApplication().run(args, stdin, outputStream));
    }

    @Test
    void run_InputFileToOutputFile_DisplaysNewlineAndOverwritesOutputFile() throws IOException {
        Path inputPath = createFile("input_file.txt");
        Path outputPath = createFile("output_file.txt");
        writeToFile(inputPath, testInput);
        writeToFile(outputPath, "This is the output file.");
        String[] args = {"input_file.txt", "output_file.txt"};
        InputStream stdin = new ByteArrayInputStream(STRING_EMPTY.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            new UniqApplication().run(args, stdin, outputStream);
            assertEquals(STRING_EMPTY, outputStream.toString());
            assertArrayEquals(withoutFlagOutput.getBytes(), Files.readAllBytes(outputPath));
        });
    }

    @Test
    void run_InputFileToNonexistentOutputFile_DisplaysEmptyLineAndCreatesOutputFile() throws IOException {
        Path inputPath = createFile("input_file.txt");
        writeToFile(inputPath, testInput);
        String[] args = {"input_file.txt", "output_file.txt"};
        InputStream stdin = new ByteArrayInputStream(STRING_EMPTY.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            new UniqApplication().run(args, stdin, outputStream);
            assertEquals(STRING_EMPTY, outputStream.toString());
            Path outputPath = TEST_PATH.resolve("output_file.txt");
            assertTrue(Files.exists(outputPath));
            assertArrayEquals(withoutFlagOutput.getBytes(), Files.readAllBytes(outputPath));
            Files.deleteIfExists(outputPath);
        });
    }

    @Test
    void run_NonexistentInputFileToOutputFile_ThrowsException() throws IOException {
        Path outputPath = createFile("output_file.txt");
        writeToFile(outputPath, "This is the output file.");
        String[] args = {"input_file.txt", "output_file.txt"};
        InputStream stdin = new ByteArrayInputStream(STRING_EMPTY.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(UniqException.class, () -> new UniqApplication().run(args, stdin, outputStream));
    }

    @Test
    void run_NonexistentInputFileToNonexistentOutputFile_ThrowsException() {
        String[] args = {"input_file.txt", "output_file.txt"};
        InputStream stdin = new ByteArrayInputStream(STRING_EMPTY.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(UniqException.class, () -> new UniqApplication().run(args, stdin, outputStream));
    }
}
