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

class CatResultTest {
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
        assertThrows(IllegalArgumentException.class, () -> new CatResult((String) null) {
        });
    }

    @Test
    void initialization_NullLines_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new CatResult((List<String>) null) {
        });
    }

    @Test
    void outputError_EmptyErrorMessage_DoesNothing() {
        captureErr();

        new CatResult(STRING_EMPTY).outputError();

        assertEquals(STRING_EMPTY, getErrOutput());
    }

    @Test
    void outputError_WhitespacesOnlyErrorMessage_DoesNothing() {
        captureErr();

        new CatResult(STRING_BLANK).outputError();

        assertEquals(STRING_EMPTY, getErrOutput());
    }

    @Test
    void outputError_IsErrorWithNonEmptyErrorMessage_PrintErrorToStdErr() {
        captureErr();

        new CatResult(STRING_MULTI_WORDS).outputError();

        assertEquals(STRING_MULTI_WORDS + STRING_NEWLINE, getErrOutput());
    }

    @Test
    void formatToString_IsError_ReturnsEmptyString() {
        assertEquals(STRING_EMPTY, new CatResult(STRING_MULTI_WORDS).formatToString(false));
        assertEquals(STRING_EMPTY, new CatResult(STRING_MULTI_WORDS).formatToString(true));
    }

    @Test
    void formatToString_EmptyLines_ReturnsEmptyString() {
        assertEquals(STRING_EMPTY, new CatResult(List.of()).formatToString(false));
        assertEquals(STRING_EMPTY, new CatResult(List.of()).formatToString(true));
    }

    @Test
    void formatToString_SingleEmptyLine_ReturnsLineString() {
        assertEquals(STRING_EMPTY, new CatResult(List.of(STRING_EMPTY)).formatToString(false));
        assertEquals("1 ", new CatResult(List.of(STRING_EMPTY)).formatToString(true));
    }

    @Test
    void formatToString_SingleNonEmptyLine_ReturnsLineString() {
        assertEquals(STRING_MULTI_WORDS, new CatResult(List.of(STRING_MULTI_WORDS)).formatToString(false));
        assertEquals("1 " + STRING_MULTI_WORDS, new CatResult(List.of(STRING_MULTI_WORDS)).formatToString(true));
    }

    @Test
    void formatToString_MultiLines_ReturnsMultiLinesString() {
        List<String> lines = List.of(STRING_MULTI_WORDS, STRING_UNICODE, STRING_BLANK, STRING_EMPTY);

        assertEquals(
                String.join(STRING_NEWLINE, lines),
                new CatResult(lines).formatToString(false)
        );

        assertEquals(
                String.join(STRING_NEWLINE,
                        IntStream.rangeClosed(1, lines.size())
                                .mapToObj(index -> String.format("%s %s", index, lines.get(index - 1)))
                                .collect(Collectors.toList())
                ),
                new CatResult(lines).formatToString(true)
        );
    }
}