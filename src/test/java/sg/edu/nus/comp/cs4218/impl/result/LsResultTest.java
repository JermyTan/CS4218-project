package sg.edu.nus.comp.cs4218.impl.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_BLANK;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_MULTI_WORDS;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_SINGLE_WORD;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;

class LsResultTest {
    private static final String TEST_DIR = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + RESOURCES_PATH + STRING_FILE_SEP + "GrepResultTest";
    private static final String FILE_NAME_1 = "test.txt";
    private static final String FILE_NAME_2 = "readme.md";
    private static final String FILE_NAME_3 = "unknown";
    private static final String TEST_FILE_1 = Paths.get(TEST_DIR, FILE_NAME_1).toString();
    private static final String TEST_FILE_2 = Paths.get(TEST_DIR, FILE_NAME_2).toString();
    private static final String TEST_FILE_3 = Paths.get(TEST_DIR, FILE_NAME_3).toString();

    private OutputStream stderr;

    private void captureErr() {
        stderr = new ByteArrayOutputStream();
        System.setErr(new PrintStream(stderr));
    }

    private String getErrOutput() {
        System.setErr(System.err);
        return stderr.toString();
    }

    @Test
    void initialization_NullErrorMessage_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new LsResult(null) {
        });
    }

    @Test
    void initialization_NullLabelOrFiles_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new LsResult(null, List.of(new File(TEST_FILE_1))) {
        });
        assertThrows(IllegalArgumentException.class, () -> new LsResult(STRING_SINGLE_WORD, null) {
        });
    }

    @Test
    void outputError_EmptyErrorMessage_DoesNothing() {
        captureErr();

        new LsResult(STRING_EMPTY).outputError();

        assertEquals(STRING_EMPTY, getErrOutput());
    }

    @Test
    void outputError_WhitespacesOnlyErrorMessage_DoesNothing() {
        captureErr();

        new LsResult(STRING_BLANK).outputError();

        assertEquals(STRING_EMPTY, getErrOutput());
    }

    @Test
    void outputError_isErrorWithNonEmptyErrorMessage_PrintErrorToStdErr() {
        captureErr();

        new LsResult(STRING_MULTI_WORDS).outputError();

        assertEquals(STRING_MULTI_WORDS + STRING_NEWLINE, getErrOutput());
    }

    @Test
    void formatToString_IsError_ReturnsEmptyString() {
        assertEquals(STRING_EMPTY, new LsResult(STRING_MULTI_WORDS).formatToString(false, false));
        assertEquals(STRING_EMPTY, new LsResult(STRING_MULTI_WORDS).formatToString(true, false));
        assertEquals(STRING_EMPTY, new LsResult(STRING_MULTI_WORDS).formatToString(false, true));
        assertEquals(STRING_EMPTY, new LsResult(STRING_MULTI_WORDS).formatToString(true, true));
    }

    @Test
    void formatToString_EmptyLabelAndLines_ReturnsEmptyString() {
        assertEquals(STRING_EMPTY, new LsResult(STRING_EMPTY, List.of()).formatToString(false, false));
        assertEquals(STRING_EMPTY, new LsResult(STRING_EMPTY, List.of()).formatToString(false, true));
        assertEquals(STRING_EMPTY, new LsResult(STRING_EMPTY, List.of()).formatToString(true, false));
        assertEquals(STRING_EMPTY, new LsResult(STRING_EMPTY, List.of()).formatToString(true, true));
    }

    @Test
    void formatToString_EmptyLinesNoShowLabel_ReturnsEmptyString() {
        assertEquals(STRING_EMPTY, new LsResult(STRING_SINGLE_WORD, List.of()).formatToString(false, false));
        assertEquals(STRING_EMPTY, new LsResult(STRING_SINGLE_WORD, List.of()).formatToString(false, true));
    }

    @Test
    void formatToString_EmptyLinesShowLabel_ReturnsLabel() {
        assertEquals(STRING_SINGLE_WORD + ":", new LsResult(STRING_SINGLE_WORD, List.of()).formatToString(true, false));
        assertEquals(STRING_MULTI_WORDS + ":", new LsResult(STRING_MULTI_WORDS, List.of()).formatToString(true, true));
    }

    @Test
    void formatToString_EmptyLabel_ReturnsUnlabelledFileNames() {
        List<File> files = List.of(
                new File(TEST_FILE_2),
                new File(TEST_FILE_1),
                new File(TEST_FILE_2),
                new File(TEST_FILE_3)
        );

        List<String> expectedUnsorted = List.of(
                FILE_NAME_2,
                FILE_NAME_1,
                FILE_NAME_2,
                FILE_NAME_3
        );

        assertEquals(
                String.join(STRING_NEWLINE, expectedUnsorted),
                new LsResult(STRING_EMPTY, files).formatToString(true, false)
        );

        List<String> expectedSorted = List.of(
                FILE_NAME_3,
                FILE_NAME_2,
                FILE_NAME_2,
                FILE_NAME_1
        );

        assertEquals(
                String.join(STRING_NEWLINE, expectedSorted),
                new LsResult(STRING_EMPTY, files).formatToString(true, true)
        );
    }

    @Test
    void formatToString_NonEmptyLabelShowLabel_ReturnsLabelledFileNames() {
        List<File> files = List.of(
                new File(TEST_FILE_2),
                new File(TEST_FILE_1),
                new File(TEST_FILE_2),
                new File(TEST_FILE_3)
        );

        List<String> expectedUnsorted = List.of(
                STRING_SINGLE_WORD + ":",
                FILE_NAME_2,
                FILE_NAME_1,
                FILE_NAME_2,
                FILE_NAME_3
        );

        assertEquals(
                String.join(STRING_NEWLINE, expectedUnsorted),
                new LsResult(STRING_SINGLE_WORD, files).formatToString(true, false)
        );

        List<String> expectedSorted = List.of(
                STRING_MULTI_WORDS + ":",
                FILE_NAME_3,
                FILE_NAME_2,
                FILE_NAME_2,
                FILE_NAME_1
        );

        assertEquals(
                String.join(STRING_NEWLINE, expectedSorted),
                new LsResult(STRING_MULTI_WORDS, files).formatToString(true, true)
        );
    }
}