package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FLAG_PREFIX;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.GrepException;

class GrepApplicationTest {

    private static final String INSENSITIVE_LETTER = "i";
    private static final String COUNT_LETTER = "c";
    private static final String FILENAME_LETTER = "H";
    private static final String INSENSITIVE_FLAG = CHAR_FLAG_PREFIX + INSENSITIVE_LETTER;
    private static final String COUNT_FLAG = CHAR_FLAG_PREFIX + COUNT_LETTER;
    private static final String FILENAME_FLAG = CHAR_FLAG_PREFIX + FILENAME_LETTER;

    private static final String DEFAULT_DIRNAME = Environment.currentDirectory;
    private static final String TEST_DIR = Environment.currentDirectory + File.separator + RESOURCES_PATH + File.separator + "WcApplicationTest";
    private static final String TEST_FILENAME = "bsd1.txt";
    private static final String TEST_FILENAME_2 = "bsd2.txt";

    private static final String NEW_LINE_CHAR = System.getProperty("line.separator");
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
    private static final String TEST_STRING = new StringBuilder()
            .append(TEST_LINE_1)
            .append(NEW_LINE_CHAR)
            .append(TEST_LINE_2)
            .append(NEW_LINE_CHAR + NEW_LINE_CHAR)
            .append(TEST_LINE_3)
            .append(NEW_LINE_CHAR)
            .append(TEST_LINE_4)
            .append(NEW_LINE_CHAR)
            .append(TEST_LINE_5)
            .append(NEW_LINE_CHAR + NEW_LINE_CHAR)
            .append(TEST_LINE_6)
            .append(NEW_LINE_CHAR)
            .toString();

    private static File testDir;
    private static File testFile;
    private static File testFile2;
    private static InputStream testInputStream;
    private static OutputStream testOutputStream;

    private final GrepApplication grepApp = new GrepApplication();

    @BeforeAll
    static void setUpBeforeAll() throws Exception {
        testDir = new File(TEST_DIR);
        testDir.mkdir();
        Environment.currentDirectory = TEST_DIR;
        testFile = new File(TEST_DIR + File.separator + TEST_FILENAME);
        testFile.createNewFile();
        Files.writeString(testFile.toPath(), TEST_STRING);
        testFile2 = new File(TEST_DIR + File.separator + TEST_FILENAME_2);
        testFile2.createNewFile();
        Files.writeString(testFile2.toPath(), TEST_STRING);
    }

    @AfterAll
    static void tearDownAfterAll() {
        testFile.delete();
        testFile2.delete();
        Environment.currentDirectory = DEFAULT_DIRNAME;
        testDir.delete();
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
    void run_SingleArgNullInputStream_ThrowsException() {
        String[] args = {"test"};
        assertThrows(GrepException.class, () -> grepApp.run(args, null, testOutputStream));
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
        String[] args = {"Copyright", "-"};
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
                () -> grepApp.grepFromStdin("test", false, false, false, null)
        );
    }

    @Test
    void grepFromStdin_NullFlags_ThrowsException() {
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromStdin("test", null, false, false, testInputStream)
        );
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromStdin("test", false, null, false, testInputStream)
        );
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromStdin("test", false, false, null, testInputStream)
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
        String output = assertDoesNotThrow(() -> grepApp.grepFromStdin("the above copyright notice, this list of conditions and the following disclaimer", false, true, false, testInputStream));
        List<String> result = output.lines().collect(Collectors.toList());
        assertEquals(1, result.size());
        assertEquals("2", result.get(0));
    }

    @Test
    void grepFromStdin_FindPhraseWithFilenameFlag_ReturnsFilenameAndLines() {
        String output = assertDoesNotThrow(() -> grepApp.grepFromStdin("the above copyright notice, this list of conditions and the following disclaimer", false, false, true, testInputStream));
        List<String> result = output.lines().collect(Collectors.toList());
        assertEquals(2, result.size());
        assertEquals("(standard input): " + TEST_LINE_4, result.get(0));
        assertEquals("(standard input): " + TEST_LINE_5, result.get(1));
    }

    @Test
    void grepFromFiles_EmptyFileNames_ThrowsException() {
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromFiles("test", false, false, false)
        );
    }

    @Test
    void grepFromFiles_NullFileNames_ThrowsException() {
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromFiles("test", false, false, false, (String[]) null)
        );
    }

    @Test
    void grepFromFiles_FileNamesContainNull_ThrowsException() {
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromFiles("test", false, false, false, TEST_FILENAME, null)
        );
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
                () -> grepApp.grepFromFiles("test", null, false, false, TEST_FILENAME)
        );
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromFiles("test", false, null, false, TEST_FILENAME)
        );
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromFiles("test", false, false, null, TEST_FILENAME)
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
                        "the above copyright notice, this list of conditions and the following disclaimer",//NOPMD
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
        String output = assertDoesNotThrow(() -> grepApp.grepFromFiles("the above copyright notice, this list of conditions and the following disclaimer", false, false, true, TEST_FILENAME, TEST_FILENAME_2));
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
                () -> grepApp.grepFromFileAndStdin("test", false, false, false, null, TEST_FILENAME)
        );
    }

    @Test
    void grepFromFileAndStdin_EmptyFileNames_ThrowsException() {
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromFileAndStdin("test", false, false, false, testInputStream)
        );
    }

    @Test
    void grepFromFileAndStdin_NullFileNames_ThrowsException() {
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromFileAndStdin("test", false, false, false, testInputStream, (String[]) null)
        );
    }

    @Test
    void grepFromFileAndStdin_FileNamesContainNull_ThrowsException() {
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromFileAndStdin("test", false, false, false, testInputStream, TEST_FILENAME, null)
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
                () -> grepApp.grepFromFileAndStdin("test", null, false, false, testInputStream, TEST_FILENAME)
        );
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromFileAndStdin("test", false, null, false, testInputStream, TEST_FILENAME)
        );
        assertThrows(
                GrepException.class,
                () -> grepApp.grepFromFileAndStdin("test", false, false, null, testInputStream, TEST_FILENAME)
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