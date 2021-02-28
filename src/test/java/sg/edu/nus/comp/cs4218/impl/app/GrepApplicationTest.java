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
    private static final String TEST_DIRNAME = Environment.currentDirectory + File.separator + RESOURCES_PATH + File.separator + "WcApplicationTest";
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
        testDir = new File(TEST_DIRNAME);
        testDir.mkdir();
        Environment.currentDirectory = TEST_DIRNAME;
        testFile = new File(TEST_DIRNAME + File.separator + TEST_FILENAME);
        testFile.createNewFile();
        Files.writeString(testFile.toPath(), TEST_STRING);
        testFile2 = new File(TEST_DIRNAME + File.separator + TEST_FILENAME_2);
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
    void testRun_WhenNullArgs_ShouldThrowException() {
        String[] args = {null};
        assertThrows(GrepException.class, () -> grepApp.run(args, testInputStream, testOutputStream));
    }

    @Test
    void testRun_WhenIllegalFlagWrongLetter_ShouldThrowException() {
        String[] args = {"-a"};
        assertThrows(GrepException.class,
                () -> grepApp.run(args, testInputStream, testOutputStream));
    }

    @Test
    void testRun_WhenIllegalFlagLegalLetterWrongCase_ShouldThrowException() {
        String[] args = {"-C"};
        assertThrows(GrepException.class,
                () -> grepApp.run(args, testInputStream, testOutputStream));
    }

    @Test
    void testRun_WhenFilenameDash_ShouldUseStdin() {
        String[] args = {"Copyright", "-"};
        assertDoesNotThrow(() -> grepApp.run(args, testInputStream, testOutputStream));
        List<String> result = testOutputStream.toString().lines().collect(Collectors.toList());
        assertEquals(1, result.size());
        assertEquals(TEST_LINE_1, result.get(0));
    }

    @Test
    void testGrepFromStdin_WhenFindWord_ShouldPrintLine() {
        String output = assertDoesNotThrow(() -> grepApp.grepFromStdin("Copyright", false, false, false, testInputStream));
        List<String> result = output.lines().collect(Collectors.toList());
        assertEquals(1, result.size());
        assertEquals(TEST_LINE_1, result.get(0));
    }

    @Test
    void testGrepFromFiles_WhenOneFileFindPhrase_ShouldPrintLine() {
        String output = assertDoesNotThrow(() -> grepApp.grepFromFiles("The Regents of the University of California", false, false, false, TEST_FILENAME));
        List<String> result = output.lines().collect(Collectors.toList());
        assertEquals(1, result.size());
        assertEquals(TEST_LINE_1, result.get(0));
    }

    @Test
    void testGrepFromStdin_WhenFindRegex_ShouldPrintLine() {
        String output = assertDoesNotThrow(() -> grepApp.grepFromStdin("\\bdistribution\\b", false, false, false, testInputStream));
        List<String> result = output.lines().collect(Collectors.toList());
        assertEquals(1, result.size());
        assertEquals(TEST_LINE_5, result.get(0));
    }

    @Test
    void testGrepFromStdin_WhenFindWord_ShouldPrintLines() {
        String output = assertDoesNotThrow(() -> grepApp.grepFromStdin("Redistribution", false, false, false, testInputStream));
        List<String> result = output.lines().collect(Collectors.toList());
        assertEquals(3, result.size());
        assertEquals(TEST_LINE_3, result.get(0));
        assertEquals(TEST_LINE_4, result.get(1));
        assertEquals(TEST_LINE_5, result.get(2));
    }

    @Test
    void testGrepFromFiles_WhenOneFileFindPhrase_ShouldPrintLines() {
        String output = assertDoesNotThrow(
                () -> grepApp.grepFromFiles(
                        "the above copyright notice, this list of conditions and the following disclaimer",//NOPMD
                        false,
                        false,
                        false, TEST_FILENAME)
        );
        List<String> result = output.lines().collect(Collectors.toList());
        assertEquals(2, result.size());
        assertEquals(TEST_LINE_4, result.get(0));
        assertEquals(TEST_LINE_5, result.get(1));
    }

    @Test
    void testGrepFromFiles_WhenOneFileFindWordWithInsensitiveFlag_ShouldPrintLines() {
        String output = assertDoesNotThrow(() -> grepApp.grepFromFiles("Regents", true, false, false, TEST_FILENAME));
        List<String> result = output.lines().collect(Collectors.toList());
        assertEquals(2, result.size());
        assertEquals(TEST_LINE_1, result.get(0));
        assertEquals(TEST_LINE_6, result.get(1));
    }

    @Test
    void testGrepFromStdin_WhenFindPhraseWithCountFlag_ShouldPrintCount() {
        String output = assertDoesNotThrow(() -> grepApp.grepFromStdin("the above copyright notice, this list of conditions and the following disclaimer", false, true, false, testInputStream));
        List<String> result = output.lines().collect(Collectors.toList());
        assertEquals(1, result.size());
        assertEquals("2", result.get(0));
    }

    @Test
    void testGrepFromStdin_WhenFindPhraseWithFilenameFlag_ShouldPrintFilenameAndLines() {
        String output = assertDoesNotThrow(() -> grepApp.grepFromStdin("the above copyright notice, this list of conditions and the following disclaimer", false, false, true, testInputStream));
        List<String> result = output.lines().collect(Collectors.toList());
        assertEquals(2, result.size());
        assertEquals("(standard input): " + TEST_LINE_4, result.get(0));
        assertEquals("(standard input): " + TEST_LINE_5, result.get(1));
    }

    @Test
    void testGrepFromFiles_WhenOneFileFindRegexWithFilenameFlag_ShouldPrintFilenameAndLines() {
        String output = assertDoesNotThrow(() -> grepApp.grepFromFiles("\\bdistribution\\b", false, false, true, TEST_FILENAME));
        List<String> result = output.lines().collect(Collectors.toList());
        assertEquals(1, result.size());
        assertEquals(TEST_FILENAME + ": " + TEST_LINE_5, result.get(0));
    }

    @Test
    void testGrepFromFiles_WhenMultipleFilesFindPhraseWithFilenameFlag_ShouldPrintFilenamesAndLines() {
        String output = assertDoesNotThrow(() -> grepApp.grepFromFiles("the above copyright notice, this list of conditions and the following disclaimer", false, false, true, TEST_FILENAME, TEST_FILENAME_2));
        List<String> result = output.lines().collect(Collectors.toList());
        assertEquals(4, result.size());
        assertEquals(TEST_FILENAME + ": " + TEST_LINE_4, result.get(0));
        assertEquals(TEST_FILENAME + ": " + TEST_LINE_5, result.get(1));
        assertEquals(TEST_FILENAME_2 + ": " + TEST_LINE_4, result.get(2));
        assertEquals(TEST_FILENAME_2 + ": " + TEST_LINE_5, result.get(3));
    }

    @Test
    void testGrepFromFileAndStdin_WhenFindPhraseWithAllFlags_ShouldPrintFilenamesAndCounts() {
        String output = assertDoesNotThrow(() -> grepApp.grepFromFileAndStdin("The Regents", true, true, true, testInputStream, TEST_FILENAME, TEST_FILENAME_2));
        List<String> result = output.lines().collect(Collectors.toList());
        assertEquals(2, result.size());
        assertEquals(TEST_FILENAME + ": 2", result.get(0));
        assertEquals(TEST_FILENAME_2 + ": 2", result.get(1));
    }

}