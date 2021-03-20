package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_MISSING_ARG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FLAG_PREFIX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_FLAG;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_SINGLE_WORD;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;

class GrepApplicationTest {

    private static final String INSENSITIVE_LETTER = "i";
    private static final String COUNT_LETTER = "c";
    private static final String FILENAME_LETTER = "H";
    private static final String INSENSITIVE_FLAG = CHAR_FLAG_PREFIX + INSENSITIVE_LETTER;
    private static final String COUNT_FLAG = CHAR_FLAG_PREFIX + COUNT_LETTER;
    private static final String FILENAME_FLAG = CHAR_FLAG_PREFIX + FILENAME_LETTER;

    private static final String DEFAULT_DIRNAME = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + RESOURCES_PATH + STRING_FILE_SEP + "GrepApplicationTest";
    private static final String TEST_FILENAME = "bsd1.txt";
    private static final String TEST_FILENAME_2 = "bsd2.txt";
    private static final String NON_EXISTENT_FILE = "non-existent.txt";
    private static final String TEST_FOLDER = "folder";

    private static final String TEST_LINE_1 = "Copyright (c) The Regents of the University of California.";
    private static final String TEST_LINE_2 = "All rights reserved.";
    private static final String TEST_LINE_3 = "Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:";
    private static final String TEST_LINE_4 = "1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.";
    private static final String TEST_LINE_5 = "2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.";
    private static final String TEST_LINE_6 = "THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND " +
            "ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE " +
            "IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE " +
            "ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE " +
            "FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL " +
            "DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS " +
            "OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) " +
            "HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT " +
            "LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY " +
            "OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF " +
            "SUCH DAMAGE.";
    private static final String TEST_LINE_7 = "the above copyright notice, this list of conditions and the following disclaimer";
    private static final String TEST_STRING = new StringBuilder()
            .append(TEST_LINE_1)
            .append(STRING_NEWLINE)
            .append(TEST_LINE_2)
            .append(STRING_NEWLINE + STRING_NEWLINE)
            .append(TEST_LINE_3)
            .append(STRING_NEWLINE)
            .append(TEST_LINE_4)
            .append(STRING_NEWLINE)
            .append(TEST_LINE_5)
            .append(STRING_NEWLINE + STRING_NEWLINE)
            .append(TEST_LINE_6)
            .append(STRING_NEWLINE)
            .toString();

    private static File testDir;
    private static File testFile;
    private static File testFile2;
    private static Path testFolder;
    private static InputStream testInputStream;
    private static OutputStream testOutputStream;
    private final GrepApplication grepApp = new GrepApplication();
    private OutputStream stderr;

    @BeforeAll
    static void setUpBeforeAll() throws IOException {
        testDir = new File(TEST_DIR);
        testDir.mkdir();
        EnvironmentUtil.currentDirectory = TEST_DIR;

        testFile = new File(TEST_DIR + STRING_FILE_SEP + TEST_FILENAME);
        testFile.createNewFile();
        Files.writeString(testFile.toPath(), TEST_STRING);
        testFile2 = new File(TEST_DIR + STRING_FILE_SEP + TEST_FILENAME_2);
        testFile2.createNewFile();
        Files.writeString(testFile2.toPath(), TEST_STRING);
        testFolder = Paths.get(TEST_DIR, TEST_FOLDER);
        Files.createDirectory(testFolder);
    }

    @AfterAll
    static void tearDownAfterAll() throws IOException {
        testFile.delete();
        testFile2.delete();
        Files.delete(testFolder);

        EnvironmentUtil.currentDirectory = DEFAULT_DIRNAME;
        testDir.delete();
    }

    private void captureErr() {
        stderr = new ByteArrayOutputStream();
        System.setErr(new PrintStream(stderr));
    }

    private String getErrOutput() {
        System.setErr(System.err);
        return stderr.toString();
    }

    @BeforeEach
    void setUp() {
        testInputStream = new ByteArrayInputStream(TEST_STRING.getBytes(StandardCharsets.UTF_8));
        testOutputStream = new ByteArrayOutputStream();
    }

    @AfterEach
    void tearDown() throws Exception {
        testInputStream.close();
        testOutputStream.close();
    }

    @Test
    void run_NullArgs_ThrowsException() {
        String[] args = {null};
        assertThrows(GrepException.class, () -> grepApp.run(args, testInputStream, testOutputStream));
    }

    @Test
    void run_NoStdinInvalidFiles_ThrowsException() {
        captureErr();

        assertDoesNotThrow(() -> {
            grepApp.run(new String[]{STRING_EMPTY, STRING_EMPTY}, null, testOutputStream);
            assertEquals(
                    new GrepException(new InvalidDirectoryException(STRING_EMPTY, ERR_FILE_NOT_FOUND).getMessage()).getMessage() + STRING_NEWLINE,
                    getErrOutput()
            );
        });
    }

    @Test
    void run_NoStdinNoInputFiles_ThrowsException() {
        Throwable exception = assertThrows(GrepException.class, () -> grepApp.run(new String[]{STRING_EMPTY}, null, testOutputStream));
        assertEquals(new GrepException(ERR_NO_INPUT).getMessage(), exception.getMessage());
    }

    @Test
    void run_NoPattern_ThrowsException() {
        Throwable exception = assertThrows(GrepException.class, () -> grepApp.run(new String[]{}, testInputStream, testOutputStream));
        assertEquals(new GrepException(ERR_MISSING_ARG).getMessage(), exception.getMessage());
    }

    @Test
    void run_SingleArgNullInputStream_ThrowsException() {
        String[] args = {};
        assertThrows(GrepException.class, () -> grepApp.run(args, null, testOutputStream));
    }

    @Test
    void run_SingleArgNullOutputStream_ThrowsException() {
        String[] args = {STRING_SINGLE_WORD};
        assertThrows(GrepException.class, () -> grepApp.run(args, testInputStream, null));
    }

    @Test
    void run_IllegalFlagWrongLetter_ThrowsException() {
        String[] args = {"-a"};
        assertThrows(GrepException.class, () -> grepApp.run(args, testInputStream, testOutputStream));
    }

    @Test
    void run_IllegalFlagLegalLetterWrongCase_ThrowsException() {
        String[] args = {"-C"};
        assertThrows(GrepException.class, () -> grepApp.run(args, testInputStream, testOutputStream));
    }

    @Test
    void run_FilenameDash_UseStdin() {
        String[] args = {"Copyright", STRING_STDIN_FLAG};
        assertDoesNotThrow(() -> grepApp.run(args, testInputStream, testOutputStream));
        List<String> result = testOutputStream.toString().lines().collect(Collectors.toList());
        assertEquals(1, result.size());
        assertEquals(TEST_LINE_1, result.get(0));
    }

    @Test
    void grepFromStdin_NullPattern_ThrowsException() {
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromStdin(null, false, false, false, testInputStream)
        );
    }

    @Test
    void grepFromStdin_NullStdin_ThrowsException() {
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromStdin(STRING_SINGLE_WORD, false, false, false, null)
        );
    }

    @Test
    void grepFromStdin_NullFlags_ThrowsException() {
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromStdin(STRING_SINGLE_WORD, null, false, false, testInputStream)
        );
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromStdin(STRING_SINGLE_WORD, false, null, false, testInputStream)
        );
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromStdin(STRING_SINGLE_WORD, false, false, null, testInputStream)
        );
    }

    @Test
    void grepFromStdin_FindWord_ReturnsLine() {
        String output = assertDoesNotThrow(() -> grepApp.grepFromStdin("Copyright", false, false, false, testInputStream));
        List<String> result = output.lines().collect(Collectors.toList());
        assertEquals(1, result.size());
        assertEquals(TEST_LINE_1, result.get(0));
    }

    @Test
    void grepFromStdin_FindRegex_ReturnsLines() {
        String output = assertDoesNotThrow(() -> grepApp.grepFromStdin("\\bdistribution\\b", false, false, false, testInputStream));
        List<String> result = output.lines().collect(Collectors.toList());
        assertEquals(1, result.size());
        assertEquals(TEST_LINE_5, result.get(0));
    }

    @Test
    void grepFromStdin_FindWord_ReturnsLines() {
        String output = assertDoesNotThrow(() -> grepApp.grepFromStdin("Redistribution", false, false, false, testInputStream));
        List<String> result = output.lines().collect(Collectors.toList());
        assertEquals(3, result.size());
        assertEquals(TEST_LINE_3, result.get(0));
        assertEquals(TEST_LINE_4, result.get(1));
        assertEquals(TEST_LINE_5, result.get(2));
    }

    @Test
    void grepFromStdin_FindPhraseWithCountFlag_ReturnsCount() {
        String output = assertDoesNotThrow(() -> grepApp.grepFromStdin(TEST_LINE_7, false, true, false, testInputStream));
        List<String> result = output.lines().collect(Collectors.toList());
        assertEquals(1, result.size());
        assertEquals("2", result.get(0));
    }

    @Test
    void grepFromStdin_FindPhraseWithFilenameFlag_ReturnsFilenameAndLines() {
        String output = assertDoesNotThrow(() -> grepApp.grepFromStdin(TEST_LINE_7, false, false, true, testInputStream));
        List<String> result = output.lines().collect(Collectors.toList());
        assertEquals(2, result.size());
        assertEquals(String.format(STRING_LABEL_VALUE_PAIR, GrepApplication.STDIN_LABEL, TEST_LINE_4), result.get(0));
        assertEquals(String.format(STRING_LABEL_VALUE_PAIR, GrepApplication.STDIN_LABEL, TEST_LINE_5), result.get(1));
    }

    @Test
    void grepFromFiles_EmptyFileNames_ThrowsException() {
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromFiles(STRING_SINGLE_WORD, false, false, false)
        );
    }

    @Test
    void grepFromFiles_NullFileNames_ThrowsException() {
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromFiles(STRING_SINGLE_WORD, false, false, false, (String[]) null)
        );
    }

    @Test
    void grepFromFiles_FileNamesContainNull_ThrowsException() {
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromFiles(STRING_SINGLE_WORD, false, false, false, TEST_FILENAME, null)
        );
    }

    @Test
    public void grepFromFiles_FileDoesNotExist_WritesErrToStderr() {
        captureErr();

        assertDoesNotThrow(() -> {
            grepApp.grepFromFiles(STRING_SINGLE_WORD, false, false, false, NON_EXISTENT_FILE);

            assertEquals(new GrepException(
                            String.format(STRING_LABEL_VALUE_PAIR, NON_EXISTENT_FILE, ERR_FILE_NOT_FOUND)
                    ).getMessage() + STRING_NEWLINE,
                    getErrOutput());
        });
    }

    @Test
    public void grepFromFiles_DirectorySupplied_WritesErrToStderr() {
        captureErr();

        assertDoesNotThrow(() -> {
            grepApp.grepFromFiles(STRING_SINGLE_WORD, false, false, false, TEST_FOLDER);

            assertEquals(new GrepException(
                            String.format(STRING_LABEL_VALUE_PAIR, TEST_FOLDER, ERR_IS_DIR)
                    ).getMessage() + STRING_NEWLINE,
                    getErrOutput());
        });
    }

    @Test
    void grepFromFiles_NullPattern_ThrowsException() {
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromFiles(null, false, false, false, TEST_FILENAME)
        );
    }

    @Test
    void grepFromFiles_NullFlags_ThrowsException() {
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromFiles(STRING_SINGLE_WORD, null, false, false, TEST_FILENAME)
        );
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromFiles(STRING_SINGLE_WORD, false, null, false, TEST_FILENAME)
        );
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromFiles(STRING_SINGLE_WORD, false, false, null, TEST_FILENAME)
        );
    }

    @Test
    void grepFromFiles_OneFileFindPhrase_ReturnsLine() {
        String output = assertDoesNotThrow(
                () -> grepApp.grepFromFiles(
                        "The Regents of the University of California",
                        false,
                        false,
                        false,
                        TEST_FILENAME)
        );
        List<String> result = output.lines().collect(Collectors.toList());
        assertEquals(1, result.size());
        assertEquals(TEST_LINE_1, result.get(0));
    }

    @Test
    void grepFromFiles_OneFileFindPhrase_ReturnsLines() {
        String output = assertDoesNotThrow(
                () -> grepApp.grepFromFiles(
                        TEST_LINE_7,
                        false,
                        false,
                        false,
                        TEST_FILENAME
                )
        );
        List<String> result = output.lines().collect(Collectors.toList());
        assertEquals(2, result.size());
        assertEquals(TEST_LINE_4, result.get(0));
        assertEquals(TEST_LINE_5, result.get(1));
    }

    @Test
    void grepFromFiles_OneFileFindWordWithInsensitiveFlag_ReturnsLines() {
        String output = assertDoesNotThrow(() -> grepApp.grepFromFiles("Regents", true, false, false, TEST_FILENAME));
        List<String> result = output.lines().collect(Collectors.toList());
        assertEquals(2, result.size());
        assertEquals(TEST_LINE_1, result.get(0));
        assertEquals(TEST_LINE_6, result.get(1));
    }

    @Test
    void grepFromFiles_OneFileFindRegexWithFilenameFlag_ReturnsFilenameAndLines() {
        String output = assertDoesNotThrow(() -> grepApp.grepFromFiles("\\bdistribution\\b", false, false, true, TEST_FILENAME));
        List<String> result = output.lines().collect(Collectors.toList());
        assertEquals(1, result.size());
        assertEquals(TEST_FILENAME + ": " + TEST_LINE_5, result.get(0));
    }

    @Test
    void grepFromFiles_MultipleFilesFindPhraseWithFilenameFlag_ReturnsFilenamesAndLines() {
        String output = assertDoesNotThrow(() -> grepApp.grepFromFiles(TEST_LINE_7, false, false, true, TEST_FILENAME, TEST_FILENAME_2));
        List<String> result = output.lines().collect(Collectors.toList());
        assertEquals(4, result.size());
        assertEquals(TEST_FILENAME + ": " + TEST_LINE_4, result.get(0));
        assertEquals(TEST_FILENAME + ": " + TEST_LINE_5, result.get(1));
        assertEquals(TEST_FILENAME_2 + ": " + TEST_LINE_4, result.get(2));
        assertEquals(TEST_FILENAME_2 + ": " + TEST_LINE_5, result.get(3));
    }

    @Test
    void grepFromFileAndStdin_NullStdin_ThrowsException() {
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromFileAndStdin(STRING_SINGLE_WORD, false, false, false, null, TEST_FILENAME)
        );
    }

    @Test
    void grepFromFileAndStdin_EmptyFileNames_ThrowsException() {
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromFileAndStdin(STRING_SINGLE_WORD, false, false, false, testInputStream)
        );
    }

    @Test
    void grepFromFileAndStdin_NullFileNames_ThrowsException() {
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromFileAndStdin(STRING_SINGLE_WORD, false, false, false, testInputStream, (String[]) null)
        );
    }

    @Test
    void grepFromFileAndStdin_FileNamesContainNull_ThrowsException() {
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromFileAndStdin(STRING_SINGLE_WORD, false, false, false, testInputStream, TEST_FILENAME, null)
        );
    }

    @Test
    void grepFromFileAndStdin_NullPattern_ThrowsException() {
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromFileAndStdin(null, false, false, false, testInputStream, TEST_FILENAME)
        );
    }

    @Test
    void grepFromFileAndStdin_NullFlags_ThrowsException() {
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromFileAndStdin(STRING_SINGLE_WORD, null, false, false, testInputStream, TEST_FILENAME)
        );
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromFileAndStdin(STRING_SINGLE_WORD, false, null, false, testInputStream, TEST_FILENAME)
        );
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromFileAndStdin(STRING_SINGLE_WORD, false, false, null, testInputStream, TEST_FILENAME)
        );
    }

    @Test
    void grepFromFileAndStdin_FindPhraseWithAllFlags_ReturnsFilenamesAndCounts() {
        String output = assertDoesNotThrow(() -> grepApp.grepFromFileAndStdin("The Regents", true, true, true, testInputStream, TEST_FILENAME, TEST_FILENAME_2));
        List<String> result = output.lines().collect(Collectors.toList());
        assertEquals(2, result.size());
        assertEquals(TEST_FILENAME + ": 2", result.get(0));
        assertEquals(TEST_FILENAME_2 + ": 2", result.get(1));
    }
}