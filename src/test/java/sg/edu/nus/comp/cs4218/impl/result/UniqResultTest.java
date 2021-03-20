package sg.edu.nus.comp.cs4218.impl.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_BLANK;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_MULTI_WORDS;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_UNICODE;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

public class UniqResultTest {
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
        assertThrows(IllegalArgumentException.class, () -> new UniqResult((String) null) {
        });
    }

    @Test
    void initialization_NullLines_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new UniqResult((List<String>) null) {
        });
    }

    @Test
    void outputError_EmptyErrorMessage_DoesNothing() {
        captureErr();

        new UniqResult(STRING_EMPTY).outputError();

        assertEquals(STRING_EMPTY, getErrOutput());
    }

    @Test
    void outputError_WhitespacesOnlyErrorMessage_DoesNothing() {
        captureErr();

        new UniqResult(STRING_BLANK).outputError();

        assertEquals(STRING_EMPTY, getErrOutput());
    }

    @Test
    void outputError_IsErrorWithNonEmptyErrorMessage_PrintErrorToStdErr() {
        captureErr();

        new UniqResult(STRING_MULTI_WORDS).outputError();

        assertEquals(STRING_MULTI_WORDS + STRING_NEWLINE, getErrOutput());
    }

    @Test
    void formatToString_IsError_ReturnsEmptyString() {
        assertEquals(STRING_EMPTY, new UniqResult(STRING_MULTI_WORDS).formatToString(false, false, false));
        assertEquals(STRING_EMPTY, new UniqResult(STRING_MULTI_WORDS).formatToString(true, true, true));
    }

    @Test
    void formatToString_EmptyLines_ReturnsEmptyString() {
        assertEquals(STRING_EMPTY, new UniqResult(List.of()).formatToString(false, false, false));
        assertEquals(STRING_EMPTY, new UniqResult(List.of()).formatToString(true, true, true));
    }

    @Test
    void formatToString_SingleEmptyLine_ReturnsLineString() {
        assertEquals(STRING_EMPTY, new UniqResult(List.of(STRING_EMPTY)).formatToString(false, false, false));
        assertEquals("1 " + STRING_EMPTY, new UniqResult(List.of(STRING_EMPTY)).formatToString(true, false, false));
        assertEquals(STRING_EMPTY, new UniqResult(List.of(STRING_EMPTY)).formatToString(true, true, false));
        assertEquals(STRING_EMPTY, new UniqResult(List.of(STRING_EMPTY)).formatToString(true, true, true));
    }

    @Test
    void formatToString_SingleNonEmptyLine_ReturnsLineString() {
        assertEquals(STRING_MULTI_WORDS, new UniqResult(List.of(STRING_MULTI_WORDS)).formatToString(false, false, false));
        assertEquals("1 " + STRING_MULTI_WORDS, new UniqResult(List.of(STRING_MULTI_WORDS)).formatToString(true, false, false));
        assertEquals(STRING_EMPTY, new UniqResult(List.of(STRING_MULTI_WORDS)).formatToString(true, true, false));
        assertEquals(STRING_EMPTY, new UniqResult(List.of(STRING_MULTI_WORDS)).formatToString(true, true, true));
    }

    @Test
    void formatToString_MultiLines_ReturnsMultiLinesString() {
        List<String> lines = List.of(
                STRING_MULTI_WORDS,
                STRING_MULTI_WORDS,
                STRING_UNICODE,
                STRING_UNICODE,
                STRING_UNICODE,
                STRING_BLANK,
                STRING_BLANK,
                STRING_EMPTY
        );

        assertEquals(
                String.join(STRING_NEWLINE, STRING_MULTI_WORDS, STRING_UNICODE, STRING_BLANK, STRING_EMPTY),
                new UniqResult(lines).formatToString(false, false, false)
        );

        assertEquals(
                String.join(
                        STRING_NEWLINE,
                        "2 " + STRING_MULTI_WORDS,
                        "3 " + STRING_UNICODE,
                        "2 " + STRING_BLANK,
                        "1 " + STRING_EMPTY
                ),
                new UniqResult(lines).formatToString(true, false, false)
        );

        assertEquals(
                String.join(
                        STRING_NEWLINE,
                        STRING_MULTI_WORDS,
                        STRING_UNICODE,
                        STRING_BLANK
                        ),
                new UniqResult(lines).formatToString(false, true, false)
        );

        assertEquals(
                String.join(
                        STRING_NEWLINE,
                        "2 " + STRING_MULTI_WORDS,
                        "3 " + STRING_UNICODE,
                        "2 " + STRING_BLANK
                ),
                new UniqResult(lines).formatToString(true, true, false)
        );

        String isAllRepeatedString = String.join(
                STRING_NEWLINE,
                STRING_MULTI_WORDS,
                STRING_MULTI_WORDS,
                STRING_UNICODE,
                STRING_UNICODE,
                STRING_UNICODE,
                STRING_BLANK,
                STRING_BLANK
        );

        assertEquals(
                isAllRepeatedString,
                new UniqResult(lines).formatToString(false, false, true)
        );

        assertEquals(
                isAllRepeatedString,
                new UniqResult(lines).formatToString(false, true, true)
        );

        assertEquals(
                isAllRepeatedString,
                new UniqResult(lines).formatToString(true, false, true)
        );

        assertEquals(
                isAllRepeatedString,
                new UniqResult(lines).formatToString(true, true, true)
        );
    }
}
