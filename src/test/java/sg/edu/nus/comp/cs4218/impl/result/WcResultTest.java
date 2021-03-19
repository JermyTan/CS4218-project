package sg.edu.nus.comp.cs4218.impl.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_BLANK;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_MULTI_WORDS;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_SINGLE_WORD;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

class WcResultTest {
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
        assertThrows(IllegalArgumentException.class, () -> new WcResult(null) {
        });
    }

    @Test
    void initialization_NullLabel_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new WcResult(null, 0, 0, 0) {
        });
    }

    @Test
    void outputError_EmptyErrorMessage_DoesNothing() {
        captureErr();

        new WcResult(STRING_EMPTY).outputError();

        assertEquals(STRING_EMPTY, getErrOutput());
    }

    @Test
    void outputError_WhitespacesOnlyErrorMessage_DoesNothing() {
        captureErr();

        new WcResult(STRING_BLANK).outputError();

        assertEquals(STRING_EMPTY, getErrOutput());
    }

    @Test
    void outputError_isErrorWithNonEmptyErrorMessage_PrintErrorToStdErr() {
        captureErr();

        new WcResult(STRING_MULTI_WORDS).outputError();

        assertEquals(STRING_MULTI_WORDS + STRING_NEWLINE, getErrOutput());
    }

    @Test
    void formatToString_IsError_ReturnsEmptyString() {
        assertEquals(STRING_EMPTY, new WcResult(STRING_MULTI_WORDS).formatToString(false, false, false));
        assertEquals(STRING_EMPTY, new WcResult(STRING_MULTI_WORDS).formatToString(true, true, true));
    }

    @Test
    void formatToString_EmptyLabel_ReturnsStringWithoutLabel() {
        assertEquals(
                "1\t2\t3",
                new WcResult(STRING_EMPTY, 1, 2, 3).formatToString(true, true, true)
        );
        assertEquals(
                "2",
                new WcResult(STRING_EMPTY, 1, 2, 3).formatToString(false, false, true)
        );
        assertEquals(
                "3",
                new WcResult(STRING_EMPTY, 1, 2, 3).formatToString(true, false, false)
        );
        assertEquals(
                "1",
                new WcResult(STRING_EMPTY, 1, 2, 3).formatToString(false, true, false)
        );
        assertEquals(
                "1\t3",
                new WcResult(STRING_EMPTY, 1, 2, 3).formatToString(true, true, false)
        );
    }

    @Test
    void formatToString_NonEmptyLabel_ReturnsStringWithoutLabel() {
        assertEquals(
                "1\t2\t3\t" + STRING_SINGLE_WORD,
                new WcResult(STRING_SINGLE_WORD, 1, 2, 3).formatToString(true, true, true)
        );
        assertEquals(
                "1\t2\t" + STRING_SINGLE_WORD,
                new WcResult(STRING_SINGLE_WORD, 1, 2, 3).formatToString(false, true, true)
        );
        assertEquals(
                "2\t3\t" + STRING_SINGLE_WORD,
                new WcResult(STRING_SINGLE_WORD, 1, 2, 3).formatToString(true, false, true)
        );
        assertEquals(
                "3\t" + STRING_SINGLE_WORD,
                new WcResult(STRING_SINGLE_WORD, 1, 2, 3).formatToString(true, false, false)
        );
        assertEquals(
                STRING_SINGLE_WORD,
                new WcResult(STRING_SINGLE_WORD, 1, 2, 3).formatToString(false, false, false)
        );
    }
}