package tdd.ef1;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.parser.ArgsParser.ILLEGAL_FLAG_MSG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.exception.TeeException;
import sg.edu.nus.comp.cs4218.impl.app.TeeApplication;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

class TeeApplicationTest {
    static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    static final String STDIN_STRING = "Hello world!" + STRING_NEWLINE
            + "Welcome to CS4218!" + STRING_NEWLINE;
    static final String TEE_ERR_FORMAT = "tee: %s: %s" + STRING_NEWLINE;
    @TempDir
    static File tempDir;
    private static TeeApplication teeApplication;
    private static InputStream stdin;
    private static OutputStream stdout;
    private static PrintStream standardOut;
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
    void setUp() throws Exception {
        teeApplication = new TeeApplication();
        stdin = new ByteArrayInputStream(STDIN_STRING.getBytes(StandardCharsets.UTF_8));
        standardOut = System.out;
        stdout = new ByteArrayOutputStream();
        System.setOut(new PrintStream(stdout));
        EnvironmentUtil.setCurrentDirectory(tempDir.getAbsolutePath());
        File existing = new File(tempDir, "existing.txt");
        FileWriter fileWriter = new FileWriter(existing, false);
        File existing2 = new File(tempDir, "existing2.txt");
        FileWriter fileWriter2 = new FileWriter(existing2, false);
        try {
            fileWriter.write("Hello World" + STRING_NEWLINE);
            fileWriter2.write("Hello CS4218" + STRING_NEWLINE);
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            fileWriter.close();
            fileWriter2.close();
        }
        File unwritable = new File(tempDir, "unwritable.txt");
        unwritable.createNewFile();
        unwritable.setReadOnly();
    }

    @AfterEach
    public void tearDown() throws Exception {
        System.setOut(standardOut);
        for (File file : tempDir.listFiles()) {
            file.delete();
        }
        EnvironmentUtil.setCurrentDirectory(ORIGINAL_DIR);
    }

    // tee with single file: write to stdout and file
    @Test
    public void run_SingleFileNoAppend_WritesToFileAndStdout() throws AbstractApplicationException, IOException {
        String[] argList = new String[]{"tee1.txt"};
        teeApplication.run(argList, stdin, System.out);
        assertEquals(STDIN_STRING, stdout.toString());
        File outputFile = new File(tempDir, "tee1.txt");
        assertTrue(outputFile.exists());
        String fileContents = Files.readString(Path.of(outputFile.getAbsolutePath()), StandardCharsets.UTF_8);
        assertEquals(STDIN_STRING, fileContents);
    }

    // tee with absolute path: write to stdout and file
    @Test
    public void run_SingleFileAbsolutePath_WritesToFileAndStdout() throws AbstractApplicationException, IOException {
        String[] argList = new String[]{tempDir.getAbsolutePath() + StringUtils.STRING_FILE_SEP + "tee1.txt"};
        teeApplication.run(argList, stdin, System.out);
        assertEquals(STDIN_STRING, stdout.toString());
        File outputFile = new File(tempDir, "tee1.txt");
        assertTrue(outputFile.exists());
        String fileContents = Files.readString(Path.of(outputFile.getAbsolutePath()), StandardCharsets.UTF_8);
        assertEquals(STDIN_STRING, fileContents);
    }

    // tee with multiple files: write to stdout and files
    @Test
    public void run_MultipleFileNoAppend_WritesToFileAndStdout() throws AbstractApplicationException, IOException {
        String[] argList = new String[]{"tee1.txt", "tee2.txt"};
        teeApplication.run(argList, stdin, System.out);
        assertEquals(STDIN_STRING, stdout.toString());
        File outputFile = new File(tempDir, "tee1.txt");
        assertTrue(outputFile.exists());
        String fileContents = Files.readString(Path.of(outputFile.getAbsolutePath()), StandardCharsets.UTF_8);
        assertEquals(STDIN_STRING, fileContents);
        File outputFile2 = new File(tempDir, "tee2.txt");
        assertTrue(outputFile2.exists());
        String fileContents2 = Files.readString(Path.of(outputFile2.getAbsolutePath()), StandardCharsets.UTF_8);
        assertEquals(STDIN_STRING, fileContents2);
    }

    // tee: write to stdout
    @Test
    public void run_NoFile_WritesToStdout() throws AbstractApplicationException {
        String[] argList = new String[]{};
        teeApplication.run(argList, stdin, System.out);
        assertEquals(STDIN_STRING, stdout.toString());
    }

    // tee with unwritable file
    @Test
    public void run_UnwritableFile_WritesNoPermToStderr() throws AbstractApplicationException {
        captureErr();

        String[] argList = new String[]{"unwritable.txt"};
        teeApplication.run(argList, stdin, System.out);
        assertEquals(STDIN_STRING, stdout.toString());

        assertEquals(
                new TeeException(new InvalidDirectoryException("unwritable.txt", ERR_NO_PERM).getMessage()).getMessage() + STRING_NEWLINE,
                getErrOutput()
        );
    }

    // tee with mixture of valid and invalid cases
    @Test
    public void run_WritableAndUnWritableFile_WritesNoPermToStderrAndWritesOutputToStdoutAndFile() throws Exception {
        captureErr();

        String[] argList = new String[]{"unwritable.txt", "tee1.txt"};
        teeApplication.run(argList, stdin, System.out);

        assertEquals(STDIN_STRING, stdout.toString());

        assertEquals(
                new TeeException(new InvalidDirectoryException("unwritable.txt", ERR_NO_PERM).getMessage()).getMessage() + STRING_NEWLINE,
                getErrOutput()
        );

        File outputFile = new File(tempDir, "tee1.txt");
        assertTrue(outputFile.exists());
        String fileContents = Files.readString(Path.of(outputFile.getAbsolutePath()), StandardCharsets.UTF_8);
        assertEquals(STDIN_STRING, fileContents);
    }

    // tee -a with single file: write to stdout and append to file
    @Test
    public void run_SingleFileAppend_WritesToFileAndStdout() throws AbstractApplicationException, IOException {
        String[] argList = new String[]{"-a", "existing.txt"};
        teeApplication.run(argList, stdin, System.out);
        assertEquals(STDIN_STRING, stdout.toString());
        File outputFile = new File(tempDir, "existing.txt");
        assertTrue(outputFile.exists());
        String fileContents = Files.readString(Path.of(outputFile.getAbsolutePath()), StandardCharsets.UTF_8);
        assertEquals("Hello World" + STRING_NEWLINE + STDIN_STRING, fileContents);
    }

    // tee -a with multiple files: write to stdout and append to file
    @Test
    public void run_MultipleFileAppend_WritesToFileAndStdout() throws AbstractApplicationException, IOException {
        String[] argList = new String[]{"-a", "existing.txt", "existing2.txt"};
        teeApplication.run(argList, stdin, System.out);
        assertEquals(STDIN_STRING, stdout.toString());
        File outputFile = new File(tempDir, "existing.txt");
        assertTrue(outputFile.exists());
        String fileContents = Files.readString(Path.of(outputFile.getAbsolutePath()), StandardCharsets.UTF_8);
        assertEquals("Hello World" + STRING_NEWLINE + STDIN_STRING, fileContents);

        File outputFile2 = new File(tempDir, "existing2.txt");
        assertTrue(outputFile2.exists());
        String fileContents2 = Files.readString(Path.of(outputFile2.getAbsolutePath()), StandardCharsets.UTF_8);
        assertEquals("Hello CS4218" + STRING_NEWLINE + STDIN_STRING, fileContents2);
    }

    // tee -a: write to stdout
    @Test
    public void run_NoFileWithFlag_WritesToStdout() throws AbstractApplicationException {
        String[] argList = new String[]{"-a"};
        teeApplication.run(argList, stdin, System.out);
        assertEquals(STDIN_STRING, stdout.toString());
    }


    // tee with invalid flag: throws exception
    @Test
    public void run_InvalidArgs_ThrowsTeeException() {
        String[] argList = new String[]{"-d", "tee1.txt"};
        Exception expectedException = assertThrows(TeeException.class, () -> {
            teeApplication.run(argList, stdin, stdout);
        });
        assertEquals(new TeeException(ILLEGAL_FLAG_MSG + "d").getMessage(), expectedException.getMessage());
    }

    // null streams: throws exception
    @Test
    public void run_StdinIsNull_ThrowsTeeException() {
        Exception expectedException = assertThrows(TeeException.class, () -> {
            teeApplication.run(new String[0], null, stdout);
        });
        assertEquals(new TeeException(ERR_NO_ISTREAM).getMessage(), expectedException.getMessage());
    }

    @Test
    public void run_StdoutIsNull_ThrowsTeeException() {
        Exception expectedException = assertThrows(TeeException.class, () -> {
            teeApplication.run(new String[0], System.in, null);
        });
        assertEquals(new TeeException(ERR_NO_OSTREAM).getMessage(), expectedException.getMessage());
    }

}
