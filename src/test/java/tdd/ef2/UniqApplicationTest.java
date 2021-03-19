package tdd.ef2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.UniqException;
import sg.edu.nus.comp.cs4218.impl.app.UniqApplication;


import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;

public class UniqApplicationTest {
    public static final Path CURR_PATH = Paths.get(EnvironmentUtil.currentDirectory);
    public static Deque<Path> files = new ArrayDeque<>();

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

    private static final String withCountAndAllDuplicateFlagsOutput = "2 Hello World" + STRING_NEWLINE +
            "2 Hello World" + STRING_NEWLINE +
            "2 Alice" + STRING_NEWLINE +
            "2 Alice" + STRING_NEWLINE;

    @AfterEach
    void deleteTemp() throws IOException {
        for (Path file : files) {
            Files.deleteIfExists(file);
        }
    }

    private Path createFile(String name) throws IOException {
        Path path = CURR_PATH.resolve(name);
        Files.createFile(path);
        files.push(path);
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
    void run_NoFilesWithCountAndAllDuplicateFlags_ReadsFromInputAndDisplaysCountOfRepeatedAdjacentLinesRepeatedly() {
        String[] args = {"-c", "-D"};
        InputStream stdin = new ByteArrayInputStream(testInput.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            new UniqApplication().run(args, stdin, outputStream);
            assertEquals(withCountAndAllDuplicateFlagsOutput, outputStream.toString());
        });
    }

    @Test
    void run_NoFilesWithAllFlags_ReadsFromInputAndDisplaysCountOfRepeatedAdjacentLinesRepeatedly() {
        String[] args = {"-cdD"};
        InputStream stdin = new ByteArrayInputStream(testInput.getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            new UniqApplication().run(args, stdin, outputStream);
            assertEquals(withCountAndAllDuplicateFlagsOutput, outputStream.toString());
        });
    }

    @Test
    void run_NoFilesWithUnknownFlag_Throws() {
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
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            new UniqApplication().run(args, stdin, outputStream);
            assertEquals(withoutFlagOutput, outputStream.toString());
        });
    }

    @Test
    void run_EmptyInputFile_ReadsFileAndDisplaysNewline() throws IOException {
        createFile("input_file.txt");
        String[] args = {"input_file.txt"};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            new UniqApplication().run(args, stdin, outputStream);
            assertEquals(STRING_NEWLINE, outputStream.toString());
        });
    }

    @Test
    void run_NonexistentInputFile_Throws() {
        String[] args = {"nonexistent_file.txt"};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
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
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            new UniqApplication().run(args, stdin, outputStream);
            assertEquals(STRING_NEWLINE, outputStream.toString());
            assertArrayEquals(withoutFlagOutput.getBytes(), Files.readAllBytes(outputPath));
        });
    }

    @Test
    void run_InputFileToNonexistentOutputFile_DisplaysNewlineAndCreatesOutputFile() throws IOException {
        Path inputPath = createFile("input_file.txt");
        writeToFile(inputPath, testInput);
        String[] args = {"input_file.txt", "output_file.txt"};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertDoesNotThrow(() -> {
            new UniqApplication().run(args, stdin, outputStream);
            assertEquals(STRING_NEWLINE, outputStream.toString());
            Path outputPath = CURR_PATH.resolve("output_file.txt");
            assertTrue(Files.exists(outputPath));
            assertArrayEquals(withoutFlagOutput.getBytes(), Files.readAllBytes(outputPath));
            Files.deleteIfExists(outputPath);
        });
    }

    @Test
    void run_NonexistentInputFileToOutputFile_Throws() throws IOException {
        Path outputPath = createFile("output_file.txt");
        writeToFile(outputPath, "This is the output file.");
        String[] args = {"input_file.txt", "output_file.txt"};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(UniqException.class, () -> new UniqApplication().run(args, stdin, outputStream));
    }

    @Test
    void run_NonexistentInputFileToNonexistentOutputFile_Throws() {
        String[] args = {"input_file.txt", "output_file.txt"};
        InputStream stdin = new ByteArrayInputStream("".getBytes());
        OutputStream outputStream = new ByteArrayOutputStream();
        assertThrows(UniqException.class, () -> new UniqApplication().run(args, stdin, outputStream));
    }
}
