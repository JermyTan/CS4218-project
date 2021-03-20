package tdd.ef1;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.condition.OS.WINDOWS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_ILLEGAL_BYTE_COUNT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_ILLEGAL_LINE_COUNT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_OPTION_REQUIRES_ARGUMENT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_READING_FILE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOO_MANY_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_FLAG;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.io.TempDir;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.exception.SplitException;
import sg.edu.nus.comp.cs4218.impl.app.SplitApplication;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

@SuppressWarnings({"PMD.LongVariable"})
public class SplitApplicationTest {
    static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final int B_BYTES = 512;
    private static final int K_BYTES = 1024;
    private static final int M_BYTES = 1048576;
    private static final int DEFAULT_LINES = 1000;
    private static final String DEFAULT_PREFIX = "x";
    private static final String OUTPUT_XAA = "xaa";
    private static final String OUTPUT_XAB = "xab";
    private static final String OUTPUT_XAC = "xac";
    private static final String OUTPUT_XAD = "xad";
    private static final String OUTPUT_XAE = "xae";
    private static final String OUTPUT_XAZ = "xaz";
    private static final String OUTPUT_XBA = "xba";
    private static final String OUTPUT_XZZ = "xzz";
    private static final String OUTPUT_XZAA = "xzaa";
    private static final String OUTPUT_XZAB = "xzab";
    private static final String OUTPUT_XZZZ = "xzzz";
    private static final String OUTPUT_XZZAA = "xzzaa";
    private static final String lineOne = "This is the first line" + StringUtils.STRING_NEWLINE;
    private static final String lineTwo = "This is the second line" + StringUtils.STRING_NEWLINE;
    private static final String lineThree = "This is the third line" + StringUtils.STRING_NEWLINE;
    private static final String lineFour = "This is the fourth line" + StringUtils.STRING_NEWLINE;
    private static final String lineFive = "This is the fifth line" + StringUtils.STRING_NEWLINE;
    private static final String lineSix = "This is the sixth line" + StringUtils.STRING_NEWLINE;
    private static final String lineSeven = "This is the seventh line" + StringUtils.STRING_NEWLINE;
    private static final String lineEight = "This is the eight line" + StringUtils.STRING_NEWLINE;
    private static final String lineNine = "This is the ninth line" + StringUtils.STRING_NEWLINE;
    private static final String lineTen = "This is the tenth line" + StringUtils.STRING_NEWLINE;
    private static final String fileContentByLines = lineOne + lineTwo + lineThree + lineFour + lineFive
            + lineSix + lineSeven + lineEight + lineNine + lineTen;
    private static final String byteOne = "Hello e";
    private static final String byteTwo = "verybod";
    private static final String byteThree = "y! Welc";
    private static final String byteFour = "ome to ";
    private static final String byteFive = "CS4218!";
    private static final String byteSix = " Seven ";
    private static final String byteSeven = "Bytes e";
    private static final String byteEight = "ach, en";
    private static final String byteNine = "ds with";
    private static final String byteTen = "6 byte";
    private static final String fileContentByBytes = byteOne + byteTwo + byteThree + byteFour + byteFive
            + byteSix + byteSeven + byteEight + byteNine + byteTen;
    @TempDir
    static File tempDir;
    private static FileWriter fileWriter;
    private static SplitApplication splitApplication;
    private static OutputStream stdout;

    @BeforeAll
    static void setupAll() throws Exception {

        EnvironmentUtil.setCurrentDirectory(tempDir.getAbsolutePath());
        // create nested folder
        new File(tempDir.getAbsolutePath(), "nested").mkdir();

        // create test-lines.txt
        fileWriter = new FileWriter(tempDir.getAbsolutePath() + StringUtils.STRING_FILE_SEP + "test-lines.txt");
        fileWriter.write(fileContentByLines);
        fileWriter.close();

        // create test-bytes.txt
        fileWriter = new FileWriter(tempDir.getAbsolutePath() + StringUtils.STRING_FILE_SEP + "test-bytes.txt");
        fileWriter.write(fileContentByBytes);
        fileWriter.close();

        // create nested/test1.txt
        fileWriter = new FileWriter(tempDir.getAbsolutePath() + StringUtils.STRING_FILE_SEP
                + "nested" + StringUtils.STRING_FILE_SEP + "test1.txt");
        fileWriter.write(fileContentByLines);
        fileWriter.close();

        // create test-unreadable.txt
        fileWriter = new FileWriter(tempDir.getAbsolutePath() + StringUtils.STRING_FILE_SEP + "test-unreadable.txt");
        fileWriter.write(fileContentByLines);
        fileWriter.close();
        File unreadable = new File(tempDir.getAbsolutePath() + StringUtils.STRING_FILE_SEP + "test-unreadable.txt");
        unreadable.setReadable(false);
    }

    @AfterAll
    static void tearDownAll() throws Exception {
        EnvironmentUtil.setCurrentDirectory(ORIGINAL_DIR);
    }

    @BeforeEach
    void setUp() {
        splitApplication = new SplitApplication();
        stdout = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws IOException {
        stdout.flush();
        Files.deleteIfExists(Paths.get(OUTPUT_XAA));
        Files.deleteIfExists(Paths.get(OUTPUT_XAB));
        Files.deleteIfExists(Paths.get(OUTPUT_XAC));
        Files.deleteIfExists(Paths.get(OUTPUT_XAD));
        Files.deleteIfExists(Paths.get(OUTPUT_XAE));
    }


    // [OPTIONS] is empty and [FILES] is - means LINES DEFAULT 1000
    @Test
    public void run_FileIsDash_ReadsStdinAndCreateByLine() throws Exception {
        String[] args = new String[]{STRING_STDIN_FLAG};
        InputStream stdin = new ByteArrayInputStream(fileContentByLines.getBytes(StandardCharsets.UTF_8));
        splitApplication.run(args, stdin, stdout);
        assertArrayEquals(fileContentByLines.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAA)));
    }

    // [OPTIONS] and [FILES] are empty means LINES DEFAULT 1000
    @Test
    public void run_FileIsEmpty_ReadsStdinAndCreateByLine() throws Exception {
        String[] args = new String[]{};
        InputStream stdin = new ByteArrayInputStream(fileContentByLines.getBytes(StandardCharsets.UTF_8));
        splitApplication.run(args, stdin, stdout);
        assertArrayEquals(fileContentByLines.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAA)));
    }

    // Null Stdout
    @Test
    public void run_FileIsEmptyStdoutIsNull_ReadsStdinAndCreateByLine() throws Exception {
        String[] args = new String[]{};
        InputStream stdin = new ByteArrayInputStream(fileContentByLines.getBytes(StandardCharsets.UTF_8));
        splitApplication.run(args, stdin, null);
        assertArrayEquals(fileContentByLines.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAA)));
    }


    // [OPTIONS] -l and [FILES] is single file with absolute path
    @Test
    public void run_SingleFileAbsolutePathByLine_CreateFile() throws Exception {
        String[] args = new String[]{"-l", "10", "nested" + StringUtils.STRING_FILE_SEP + "test1.txt"};
        splitApplication.run(args, System.in, stdout);
        assertArrayEquals(fileContentByLines.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAA)));
    }

    // [OPTIONS] -l and [FILES] is single file with relative path
    @Test
    public void run_SingleFileRelativePathByLine_CreateFile() throws Exception {
        String[] args = new String[]{"-l", "10", "test-lines.txt"};
        splitApplication.run(args, System.in, stdout);
        assertArrayEquals(fileContentByLines.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAA)));
    }

    // STDIN and [OPTIONS] -l and [FILES] is none
    @Test
    public void run_ByLineAndStdin_CreateFile() throws Exception {
        String[] args = new String[]{"-l", "100", STRING_STDIN_FLAG};
        InputStream stdin = new ByteArrayInputStream(fileContentByLines.getBytes(StandardCharsets.UTF_8));
        splitApplication.run(args, stdin, stdout);
        assertArrayEquals(fileContentByLines.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAA)));
    }

    // [OPTIONS] -b and [FILES] is single file
    @Test
    public void run_ByByteSingleFile_CreateFile() throws Exception {
        String[] args = new String[]{"-b", "100m", "test-bytes.txt"};
        splitApplication.run(args, System.in, stdout);
        assertArrayEquals(fileContentByBytes.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAA)));
    }

    // STDIN and [OPTIONS] -b and [FILES] is none
    @Test
    public void run_ByBytesAndStdin_CreateFile() throws Exception {
        String[] args = new String[]{"-b", "100m", STRING_STDIN_FLAG};
        InputStream stdin = new ByteArrayInputStream(fileContentByBytes.getBytes(StandardCharsets.UTF_8));
        splitApplication.run(args, stdin, stdout);
        assertArrayEquals(fileContentByBytes.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAA)));
    }

    // [OPTIONS] -l and [FILES] are empty
    @Test
    public void run_ByLineAndFileIsEmpty_ThrowsException() {
        String[] args = new String[]{"-l"};
        Throwable missingArgs = assertThrows(SplitException.class, () -> splitApplication.run(args, System.in, stdout));
        assertEquals(new SplitException(ERR_OPTION_REQUIRES_ARGUMENT).getMessage(), missingArgs.getMessage());
    }

    // [OPTIONS] -b and [FILES] are empty
    @Test
    public void run_ByByteAndFileIsEmpty_ThrowsException() {
        String[] args = new String[]{"-b"};
        Throwable missingArgs = assertThrows(SplitException.class, () -> splitApplication.run(args, System.in, stdout));
        assertEquals(new SplitException(ERR_OPTION_REQUIRES_ARGUMENT).getMessage(), missingArgs.getMessage());
    }

    // wrong line args indicated
    @Test
    public void run_ByLineWithWrongLineNum_ThrowsException() {
        String[] args = new String[]{"-l", "100b", "test-lines.txt"};
        Throwable wrongArgs = assertThrows(SplitException.class, () -> splitApplication.run(args, System.in, stdout));
        assertEquals(new SplitException(ERR_ILLEGAL_LINE_COUNT).getMessage(), wrongArgs.getMessage());
    }

    // wrong byte args indicated
    @Test
    public void run_ByBytesWithWrongByteStr_ThrowsException() {
        String[] args = new String[]{"-b", "100a", "test-bytes.txt"};
        Throwable wrongArgs = assertThrows(SplitException.class, () -> splitApplication.run(args, System.in, stdout));
        assertEquals(new SplitException(ERR_ILLEGAL_BYTE_COUNT).getMessage(), wrongArgs.getMessage());
    }

    // Too many arguments
    @Test
    public void run_ExtraArgs_ThrowsException() {
        String[] args = new String[]{"-b", "100b", "test-bytes.txt", "x", "extra-args"};
        Throwable extraArgs = assertThrows(SplitException.class, () -> splitApplication.run(args, System.in, stdout));
        assertEquals(new SplitException(ERR_TOO_MANY_ARGS).getMessage(), extraArgs.getMessage());
    }

    // [FILES] is invalid file: prints error
    @Test
    public void run_InvalidFile_ThrowsException() {
        String[] args = new String[]{"-l", "10", "notfound.txt"};
        Throwable invalidFile = assertThrows(SplitException.class, () -> splitApplication.run(args, System.in, stdout));
        String expected = new SplitException(
                new InvalidDirectoryException("notfound.txt", ERR_FILE_NOT_FOUND).getMessage()
        ).getMessage();
        assertEquals(expected, invalidFile.getMessage());
    }

    // [FILES] is a directory: prints error
    @Test
    public void run_FileIsDirectory_ThrowsException() {
        String[] args = new String[]{"-l", "10", "nested"};
        Throwable isDir = assertThrows(SplitException.class, () -> splitApplication.run(args, System.in, stdout));
        String expected = new SplitException(
                new InvalidDirectoryException("nested", ERR_IS_DIR).getMessage()
        ).getMessage();
        assertEquals(expected, isDir.getMessage());
    }

    // [FILES] is a unreadable: prints error
    @Test
    @DisabledOnOs(WINDOWS)
    public void run_FileIsUnreadable_ThrowsException() {
        String[] args = new String[]{"-l", "10", "test-unreadable.txt"};
        Throwable isDir = assertThrows(SplitException.class, () -> splitApplication.run(args, System.in, stdout));
        String expected = new SplitException(
                new InvalidDirectoryException("test-unreadable.txt", ERR_READING_FILE).getMessage()
        ).getMessage();
        assertEquals(expected, isDir.getMessage());
    }

    // Null Args
    @Test
    public void run_ArgsIsNull_ThrowsException() {
        Throwable argsIsNull = assertThrows(SplitException.class, () ->
                splitApplication.run(null, null, null));
        assertEquals(new SplitException(ERR_NO_ISTREAM).getMessage(), argsIsNull.getMessage());
    }

    // Null Stdin
    @Test
    public void run_StdinIsNull_ThrowsException() {
        String[] args = new String[]{};
        Throwable stdinIsNull = assertThrows(SplitException.class, () ->
                splitApplication.run(args, null, null));
        assertEquals(new SplitException(ERR_NO_ISTREAM).getMessage(), stdinIsNull.getMessage());
    }

    @Test
    public void splitFileByLines_TwoLines_CreateFiveFiles() throws Exception {
        splitApplication.splitFileByLines("test-lines.txt", DEFAULT_PREFIX, 2);
        String firstFile = lineOne + lineTwo;
        String secondFile = lineThree + lineFour;
        String thirdFile = lineFive + lineSix;
        String fourthFile = lineSeven + lineEight;
        String fifthFile = lineNine + lineTen;
        assertArrayEquals(firstFile.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAA)));
        assertArrayEquals(secondFile.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAB)));
        assertArrayEquals(thirdFile.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAC)));
        assertArrayEquals(fourthFile.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAD)));
        assertArrayEquals(fifthFile.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAE)));
    }

    @Test
    public void splitFileByLines_SevenLines_CreateTwoFiles() throws Exception {
        splitApplication.splitFileByLines("test-lines.txt", DEFAULT_PREFIX, 7);
        String firstFile = lineOne + lineTwo + lineThree + lineFour + lineFive + lineSix + lineSeven;
        String secondFile = lineEight + lineNine + lineTen;
        assertArrayEquals(firstFile.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAA)));
        assertArrayEquals(secondFile.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAB)));
    }

    @Test
    public void splitStdinByLines_ThreeLines_CreateFourFiles() throws Exception {
        InputStream stdin = new ByteArrayInputStream(fileContentByLines.getBytes(StandardCharsets.UTF_8));
        splitApplication.splitStdinByLines(stdin, DEFAULT_PREFIX, 3);
        String firstFile = lineOne + lineTwo + lineThree;
        String secondFile = lineFour + lineFive + lineSix;
        String thirdFile = lineSeven + lineEight + lineNine;
        String fourthFile = lineTen;
        assertArrayEquals(firstFile.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAA)));
        assertArrayEquals(secondFile.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAB)));
        assertArrayEquals(thirdFile.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAC)));
        assertArrayEquals(fourthFile.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAD)));
    }

    @Test
    public void splitStdinByLines_EightLines_CreateTwoFiles() throws Exception {
        InputStream stdin = new ByteArrayInputStream(fileContentByLines.getBytes(StandardCharsets.UTF_8));
        splitApplication.splitStdinByLines(stdin, DEFAULT_PREFIX, 8);
        String firstFile = lineOne + lineTwo + lineThree + lineFour + lineFive + lineSix + lineSeven + lineEight;
        String secondFile = lineNine + lineTen;
        assertArrayEquals(firstFile.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAA)));
        assertArrayEquals(secondFile.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAB)));
    }

    // Null Stdin
    @Test
    public void splitStdinByLines_StdinIsNull_ThrowsNullIStream() {
        Throwable stdinIsNull = assertThrows(Exception.class, () -> {
            splitApplication.splitStdinByLines(null, DEFAULT_PREFIX, DEFAULT_LINES);
        });
        assertEquals(new SplitException(ERR_NO_ISTREAM).getMessage(), stdinIsNull.getMessage());
    }

    @Test
    public void splitFileByBytes_FourteenBytes_CreateFiveFiles() throws Exception {
        splitApplication.splitFileByBytes("test-bytes.txt", DEFAULT_PREFIX, "14");
        String firstFile = byteOne + byteTwo;
        String secondFile = byteThree + byteFour;
        String thirdFile = byteFive + byteSix;
        String fourthFile = byteSeven + byteEight;
        String fifthFile = byteNine + byteTen;
        assertArrayEquals(firstFile.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAA)));
        assertArrayEquals(secondFile.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAB)));
        assertArrayEquals(thirdFile.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAC)));
        assertArrayEquals(fourthFile.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAD)));
        assertArrayEquals(fifthFile.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAE)));
    }

    @Test
    public void splitStdinByBytes_OneB_CreateOneFile() throws Exception {
        InputStream stdin = new ByteArrayInputStream(fileContentByBytes.getBytes(StandardCharsets.UTF_8));
        splitApplication.splitStdinByBytes(stdin, DEFAULT_PREFIX, "1b");
        assertArrayEquals(fileContentByBytes.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAA)));
    }

    @Test
    public void splitStdinByBytes_FourteenBytes_CreateFiveFiles() throws Exception {
        InputStream stdin = new ByteArrayInputStream(fileContentByBytes.getBytes(StandardCharsets.UTF_8));
        splitApplication.splitStdinByBytes(stdin, DEFAULT_PREFIX, "14");
        String firstFile = byteOne + byteTwo;
        String secondFile = byteThree + byteFour;
        String thirdFile = byteFive + byteSix;
        String fourthFile = byteSeven + byteEight;
        String fifthFile = byteNine + byteTen;
        assertArrayEquals(firstFile.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAA)));
        assertArrayEquals(secondFile.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAB)));
        assertArrayEquals(thirdFile.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAC)));
        assertArrayEquals(fourthFile.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAD)));
        assertArrayEquals(fifthFile.getBytes(StandardCharsets.UTF_8), Files.readAllBytes(IOUtils.resolveAbsoluteFilePath(OUTPUT_XAE)));
    }

    // Null Stdin
    @Test
    public void splitStdinByBytes_StdinIsNull_ThrowsNullIStream() {
        Throwable stdinIsNull = assertThrows(Exception.class, () -> {
            splitApplication.splitStdinByBytes(null, DEFAULT_PREFIX, "100m");
        });
        assertEquals(new SplitException(ERR_NO_ISTREAM).getMessage(), stdinIsNull.getMessage());
    }

//    @Test
//    public void getFileName_FirstFile_ReturnsXAA() {
//        String fileName = splitApplication.getFileName(DEFAULT_PREFIX, DEFAULT_PREFIX);
//        assertEquals(OUTPUT_XAA, fileName);
//    }
//
//    @Test
//    public void getFileName_XAB_ReturnsXAC() {
//        String fileName = splitApplication.getFileName(DEFAULT_PREFIX, OUTPUT_XAB);
//        assertEquals(OUTPUT_XAC, fileName);
//    }
//
//    @Test
//    public void getFileName_XAZ_ReturnsXBA() {
//        String fileName = splitApplication.getFileName(DEFAULT_PREFIX, OUTPUT_XAZ);
//        assertEquals(OUTPUT_XBA, fileName);
//    }
//
//    @Test
//    public void getFileName_XZZ_ReturnsXZAA() {
//        String fileName = splitApplication.getFileName(DEFAULT_PREFIX, OUTPUT_XZZ);
//        assertEquals(OUTPUT_XZAA, fileName);
//    }
//    @Test
//    public void getFileName_XZAA_ReturnsXZAB() {
//        String fileName = splitApplication.getFileName(DEFAULT_PREFIX, OUTPUT_XZAA);
//        assertEquals(OUTPUT_XZAB, fileName);
//    }
//    @Test
//    public void getFileName_XZZZ_ReturnsXZZAA() {
//        String fileName = splitApplication.getFileName(DEFAULT_PREFIX, OUTPUT_XZZZ);
//        assertEquals(OUTPUT_XZZAA, fileName);
//    }
//
//    @Test
//    public void calculateTotalBytesPerFile_100b_ReturnsRequiredBytes() {
//        int finalBytes = splitApplication.calculateTotalBytesPerFile("100b");
//        assertEquals(B_BYTES*100, finalBytes);
//    }
//
//    @Test
//    public void calculateTotalBytesPerFile_200k_ReturnsRequiredBytes() {
//        int finalBytes = splitApplication.calculateTotalBytesPerFile("200k");
//        assertEquals(K_BYTES*200, finalBytes);
//    }
//
//    @Test
//    public void calculateTotalBytesPerFile_300m_ReturnsRequiredBytes() {
//        int finalBytes = splitApplication.calculateTotalBytesPerFile("300m");
//        assertEquals(M_BYTES*300, finalBytes);
//    }
//
//    @Test
//    public void calculateTotalBytesPerFile_400_ReturnsRequiredBytes() {
//        int finalBytes = splitApplication.calculateTotalBytesPerFile("400");
//        assertEquals(400, finalBytes);
//    }
}
