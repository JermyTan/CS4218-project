package tdd.bf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_FLAG;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.impl.app.GrepApplication;

public class GrepApplicationTest {
    public static final byte[] BYTES_A = ("First line" + STRING_NEWLINE + "Second line" + STRING_NEWLINE + "Third line" + STRING_NEWLINE + "Fourth line" + STRING_NEWLINE).getBytes();
    public static final byte[] BYTES_B = ("Fifth line" + STRING_NEWLINE + "Sixth line" + STRING_NEWLINE + "Seventh line" + STRING_NEWLINE + "Eighth line" + STRING_NEWLINE).getBytes();
    private static final String TEMP = "temp-grep";
    public static final Path TEMP_PATH = Path.of(EnvironmentUtil.currentDirectory, TEMP);
    public static Deque<Path> files = new ArrayDeque<>();
    private OutputStream stderr;

    @BeforeAll
    static void createTemp() throws IOException {
        Files.createDirectory(TEMP_PATH);
    }

    @AfterAll
    static void deleteTemp() throws IOException {
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

    private Path createFile(String name) throws IOException {
        Path path = TEMP_PATH.resolve(name);
        Files.createFile(path);
        files.push(path);
        return path;
    }

    private Path createDirectory(String folder) throws IOException {
        Path path = TEMP_PATH.resolve(folder);
        Files.createDirectory(path);
        files.push(path);
        return path;
    }

    private String[] toArgs(String flags, String pattern, String... files) {
        List<String> args = new ArrayList<>();
        if (!flags.isEmpty()) {
            args.add(STRING_STDIN_FLAG + flags);
        }
        args.add(pattern);
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
        return Path.of(TEMP, name);
    }

    @Test
    void run_SingleFile_PrintsLine() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("a.txt"), BYTES_A);
        new GrepApplication().run(toArgs(STRING_EMPTY, "th", "a.txt"), System.in, output);
        assertEquals("Fourth line" + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_SingleFileRegex_PrintsLines() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("b.txt"), BYTES_A);
        new GrepApplication().run(toArgs(STRING_EMPTY, "[th] l", "b.txt"), System.in, output);
        assertEquals("First line" + STRING_NEWLINE + "Fourth line" + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_MultipleFiles_PrintsLinesWithNames() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("c.txt"), BYTES_A);
        Files.write(createFile("d.txt"), BYTES_B);
        new GrepApplication().run(toArgs(STRING_EMPTY, "Fi", "c.txt", "d.txt"), System.in, output);
        String expected = getRelativePath("c.txt") + ": First line" + STRING_NEWLINE +
                getRelativePath("d.txt") + ": Fifth line" + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }

    @Test
    void run_SingleFilePrintName_PrintsLinesWithName() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("e.txt"), BYTES_A);
        Path path = getRelativePath("e.txt");
        new GrepApplication().run(toArgs("H", "F", "e.txt"), System.in, output);
        String expected = path + ": First line" + STRING_NEWLINE + path + ": Fourth line" + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }

    @Test
    void run_MultipleFilesPrintName_PrintsLinesWithNames() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("f.txt"), BYTES_A);
        Files.write(createFile("g.txt"), BYTES_B);
        new GrepApplication().run(toArgs("H", "Fi", "f.txt", "g.txt"), System.in, output);
        String expected = getRelativePath("f.txt") + ": First line" + STRING_NEWLINE +
                getRelativePath("g.txt") + ": Fifth line" + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }

    @Test
    void run_SingleFileCaseInsensitive_PrintsLines() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("h.txt"), BYTES_A);
        new GrepApplication().run(toArgs("i", "th", "h.txt"), System.in, output);
        String expected = "Third line" + STRING_NEWLINE + "Fourth line" + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }

    @Test
    void run_SingleFilePrintNameCaseInsensitive_PrintsLinesWithName() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("i.txt"), BYTES_A);
        Path path = getRelativePath("i.txt");
        new GrepApplication().run(toArgs("Hi", "th", "i.txt"), System.in, output);
        String expected = path + ": Third line" + STRING_NEWLINE + path + ": Fourth line" + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }

    @Test
    void run_MultipleFilesPrintNameCaseInsensitive_PrintsLines() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("j.txt"), BYTES_A);
        Files.write(createFile("k.txt"), BYTES_B);
        new GrepApplication().run(toArgs("Hi", "fi", "j.txt", "k.txt"), System.in, output);
        String expected = getRelativePath("j.txt") + ": First line" + STRING_NEWLINE +
                getRelativePath("k.txt") + ": Fifth line" + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }

    @Test
    void run_SingleFileCountLines_PrintsCount() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("l.txt"), BYTES_A);
        new GrepApplication().run(toArgs("c", "th", "l.txt"), System.in, output);
        assertEquals("1" + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_MultipleFilesCountLines_PrintsCountWithNames() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("m.txt"), BYTES_A);
        Files.write(createFile("n.txt"), BYTES_B);
        new GrepApplication().run(toArgs("c", "th", "m.txt", "n.txt"), System.in, output);
        String expected = getRelativePath("m.txt") + ": 1" + STRING_NEWLINE +
                getRelativePath("n.txt") + ": 4" + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }

    @Test
    void run_SingleFileCountLinesPrintName_PrintsCountWithName() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("o.txt"), BYTES_A);
        new GrepApplication().run(toArgs("cH", "th", "o.txt"), System.in, output);
        assertEquals(getRelativePath("o.txt") + ": 1" + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_MultipleFilesCountLinesPrintName_PrintsCountWithNames() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("p.txt"), BYTES_A);
        Files.write(createFile("q.txt"), BYTES_B);
        new GrepApplication().run(toArgs("cH", "th", "p.txt", "q.txt"), System.in, output);
        String expected = getRelativePath("p.txt") + ": 1" + STRING_NEWLINE +
                getRelativePath("q.txt") + ": 4" + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }

    @Test
    void run_SingleFileCountLinesCaseInsensitive_PrintsCount() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("r.txt"), BYTES_A);
        new GrepApplication().run(toArgs("ci", "th", "r.txt"), System.in, output);
        assertEquals("2" + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_MultipleFilesCountLinesCaseInsensitive_PrintsCountWithNames() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("s.txt"), BYTES_A);
        Files.write(createFile("t.txt"), BYTES_B);
        new GrepApplication().run(toArgs("ci", "th", "s.txt", "t.txt"), System.in, output);
        String expected = getRelativePath("s.txt") + ": 2" + STRING_NEWLINE +
                getRelativePath("t.txt") + ": 4" + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }

    @Test
    void run_SingleFileCountLinesCaseInsensitivePrintName_PrintsCountWithName() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("u.txt"), BYTES_A);
        new GrepApplication().run(toArgs("ciH", "th", "u.txt"), System.in, output);
        assertEquals(getRelativePath("u.txt") + ": 2" + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_MultipleFilesCountLinesCaseInsensitivePrintName_PrintsCountWithNames() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("v.txt"), BYTES_A);
        Files.write(createFile("w.txt"), BYTES_B);
        new GrepApplication().run(toArgs("ciH", "th", "v.txt", "w.txt"), System.in, output);
        String expected = getRelativePath("v.txt") + ": 2" + STRING_NEWLINE +
                getRelativePath("w.txt") + ": 4" + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }

    @Test
    void run_NoFiles_PrintsLinesFromStdin() throws AbstractApplicationException {
        String inputString = "abc" + STRING_NEWLINE + "def" + STRING_NEWLINE + "ghi" + STRING_NEWLINE;
        ByteArrayInputStream input = new ByteArrayInputStream(inputString.getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new GrepApplication().run(toArgs(STRING_EMPTY, "[ci]"), input, output);
        assertEquals("abc" + STRING_NEWLINE + "ghi" + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_NoFilesCaseInsensitivePrintName_PrintsLinesWithNameFromStdin() throws AbstractApplicationException {
        String inputString = "Abc" + STRING_NEWLINE + "aaf" + STRING_NEWLINE + "bbd" + STRING_NEWLINE;
        ByteArrayInputStream input = new ByteArrayInputStream(inputString.getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new GrepApplication().run(toArgs("Hi", "a"), input, output);
        String expected = String.join(
                STRING_NEWLINE,
                String.format(STRING_LABEL_VALUE_PAIR, GrepApplication.STDIN_LABEL, "Abc"),
                String.format(STRING_LABEL_VALUE_PAIR, GrepApplication.STDIN_LABEL, "aaf"),
                STRING_EMPTY
        );
        assertEquals(expected, output.toString());
    }

    @Test
    void run_NoFilesCountLinesCaseInsensitivePrintName_PrintsCountWithNameFromStdin() throws AbstractApplicationException {
        String inputString = "Abc" + STRING_NEWLINE + "aaf" + STRING_NEWLINE + "bbd" + STRING_NEWLINE;
        ByteArrayInputStream input = new ByteArrayInputStream(inputString.getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new GrepApplication().run(new String[]{"-c", "-Hi", "a"}, input, output);
        String expected = String.format(STRING_LABEL_VALUE_PAIR, GrepApplication.STDIN_LABEL, 2) + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }

    @Test
    void run_Stdin_PrintsLinesFromStdin() throws AbstractApplicationException {
        String inputString = "jkl" + STRING_NEWLINE + "mno" + STRING_NEWLINE + "pqr" + STRING_NEWLINE;
        ByteArrayInputStream input = new ByteArrayInputStream(inputString.getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new GrepApplication().run(toArgs(STRING_EMPTY, "[lo]", STRING_STDIN_FLAG), input, output);
        assertEquals("jkl" + STRING_NEWLINE + "mno" + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_StdinCaseInsensitivePrintName_PrintsLinesWithNameFromStdin() throws AbstractApplicationException {
        String inputString = "Dbc" + STRING_NEWLINE + "daf" + STRING_NEWLINE + "aab" + STRING_NEWLINE;
        ByteArrayInputStream input = new ByteArrayInputStream(inputString.getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new GrepApplication().run(toArgs("Hi", "d", STRING_STDIN_FLAG), input, output);
        String expected = String.join(
                STRING_NEWLINE,
                String.format(STRING_LABEL_VALUE_PAIR, GrepApplication.STDIN_LABEL, "Dbc"),
                String.format(STRING_LABEL_VALUE_PAIR, GrepApplication.STDIN_LABEL, "daf"),
                STRING_EMPTY
        );
        assertEquals(expected, output.toString());
    }

    @Test
    void run_StdinCountLinesCaseInsensitivePrintName_PrintsCountWithNameFromStdin() throws AbstractApplicationException {
        String inputString = "Bbc" + STRING_NEWLINE + "bac" + STRING_NEWLINE + "cde" + STRING_NEWLINE;
        ByteArrayInputStream input = new ByteArrayInputStream(inputString.getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new GrepApplication().run(new String[]{"-c", "-Hi", "b", STRING_STDIN_FLAG}, input, output);
        String expected = String.format(STRING_LABEL_VALUE_PAIR, GrepApplication.STDIN_LABEL, 2) + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }

    @Test
    void run_StdinAndFile_PrintsLines() throws AbstractApplicationException, IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(BYTES_A);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("aa.txt"), BYTES_B);
        new GrepApplication().run(toArgs(STRING_EMPTY, "Fi", STRING_STDIN_FLAG, "aa.txt"), input, output);
        String expected = String.format(STRING_LABEL_VALUE_PAIR, GrepApplication.STDIN_LABEL, "First line") + STRING_NEWLINE +
                getRelativePath("aa.txt") + ": Fifth line" + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }

    @Test
    void run_StdinAndFileCaseInsensitivePrintName_PrintsLinesWithName() throws AbstractApplicationException, IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(BYTES_B);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("ab.txt"), BYTES_A);
        new GrepApplication().run(toArgs("Hi", "fi", STRING_STDIN_FLAG, "ab.txt"), input, output);
        String expected = String.format(STRING_LABEL_VALUE_PAIR, GrepApplication.STDIN_LABEL, "Fifth line") + STRING_NEWLINE +
                getRelativePath("ab.txt") + ": First line" + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }

    @Test
    void run_StdinAndFileCountLinesCaseInsensitivePrintName_PrintsCountWithName() throws AbstractApplicationException, IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(BYTES_B);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("ac.txt"), BYTES_A);
        new GrepApplication().run(toArgs("cHi", "th", "ac.txt", STRING_STDIN_FLAG), input, output);
        String expected = getRelativePath("ac.txt") + ": 2" + STRING_NEWLINE +
                String.format(STRING_LABEL_VALUE_PAIR, GrepApplication.STDIN_LABEL, 4) + STRING_NEWLINE;
        assertEquals(expected, output.toString());
    }

    @Test
    void run_EmptyFile_PrintsNothing() throws AbstractApplicationException, IOException {
        createFile("x.txt");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new GrepApplication().run(toArgs(STRING_EMPTY, "a", "x.txt"), System.in, output);
        assertEquals(STRING_EMPTY, output.toString());
    }

    @Test
    void run_NoMatch_PrintsNothing() throws AbstractApplicationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("y.txt"), BYTES_A);
        new GrepApplication().run(toArgs(STRING_EMPTY, "a", "y.txt"), System.in, output);
        assertEquals(STRING_EMPTY, output.toString());
    }

    @Test
    void run_Directory_OutputsError() throws AbstractApplicationException, IOException {
        captureErr();

        createDirectory("directory");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new GrepApplication().run(toArgs(STRING_EMPTY, "a", "directory"), System.in, output);

        String expectedOutput = STRING_EMPTY;
        assertEquals(expectedOutput, output.toString());

        String expectedErr = new GrepException(
                new InvalidDirectoryException(
                        getRelativePath("directory").toString(),
                        ERR_IS_DIR
                ).getMessage()
        ).getMessage() + STRING_NEWLINE;
        assertEquals(expectedErr, getErrOutput());
    }

    @Test
    void run_NonexistentFile_OutputsError() throws AbstractApplicationException {
        captureErr();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new GrepApplication().run(toArgs(STRING_EMPTY, "a", "not exist"), System.in, output);

        String expectedOutput = STRING_EMPTY;
        assertEquals(expectedOutput, output.toString());

        String expectedErr = new GrepException(
                new InvalidDirectoryException(
                        getRelativePath("not exist").toString(),
                        ERR_FILE_NOT_FOUND
                ).getMessage()
        ).getMessage() + STRING_NEWLINE;
        assertEquals(expectedErr, getErrOutput());
    }

    @Test
    void run_UnknownFlag_ThrowsException() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Files.write(createFile("z.txt"), BYTES_A);
        assertThrows(GrepException.class, () -> new GrepApplication().run(toArgs("x", "l", "z.txt"), System.in, output));
    }

    @Test
    void run_ZeroArguments_ThrowsException() {
        assertThrows(GrepException.class, () -> new GrepApplication().run(new String[]{}, System.in, System.out));
    }

    @Test
    void run_FlagOnly_ThrowsException() {
        assertThrows(GrepException.class, () -> new GrepApplication().run(new String[]{"-c"}, System.in, System.out));
    }
}
