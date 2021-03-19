package sg.edu.nus.comp.cs4218.impl.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_BLANK;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_MULTI_WORDS;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_SINGLE_WORD;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_UNICODE;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

class GrepResultTest {
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
        assertThrows(IllegalArgumentException.class, () -> new GrepResult(null) {
        });
    }

    @Test
    void initialization_NullLabelOrLines_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new GrepResult(null, List.of(STRING_SINGLE_WORD)) {
        });
        assertThrows(IllegalArgumentException.class, () -> new GrepResult(STRING_SINGLE_WORD, null) {
        });
    }

    @Test
    void outputError_EmptyErrorMessage_DoesNothing() {
        captureErr();

        new GrepResult(STRING_EMPTY).outputError();

        assertEquals(STRING_EMPTY, getErrOutput());
    }

    @Test
    void outputError_WhitespacesOnlyErrorMessage_DoesNothing() {
        captureErr();

        new GrepResult(STRING_BLANK).outputError();

        assertEquals(STRING_EMPTY, getErrOutput());
    }

    @Test
    void outputError_isErrorWithNonEmptyErrorMessage_PrintErrorToStdErr() {
        captureErr();

        new GrepResult(STRING_MULTI_WORDS).outputError();

        assertEquals(STRING_MULTI_WORDS + STRING_NEWLINE, getErrOutput());
    }

    @Test
    void formatToString_IsError_ReturnsEmptyString() {
        assertEquals(STRING_EMPTY, new GrepResult(STRING_MULTI_WORDS).formatToString(false, false));
        assertEquals(STRING_EMPTY, new GrepResult(STRING_MULTI_WORDS).formatToString(true, false));
        assertEquals(STRING_EMPTY, new GrepResult(STRING_MULTI_WORDS).formatToString(false, true));
        assertEquals(STRING_EMPTY, new GrepResult(STRING_MULTI_WORDS).formatToString(true, true));
    }

    @Test
    void formatToString_EmptyLinesIsLineNumber_ReturnsZeroString() {
        assertEquals("0", new GrepResult(STRING_SINGLE_WORD, List.of()).formatToString(true, false));
        assertEquals(
                String.format(STRING_LABEL_VALUE_PAIR, STRING_SINGLE_WORD, "0"),
                new GrepResult(STRING_SINGLE_WORD, List.of()).formatToString(true, true)
        );
    }

    @Test
    void formatToString_MultiLinesIsLineNumber_ReturnsNumLinesString() {
        List<String> lines = List.of(STRING_MULTI_WORDS, STRING_UNICODE, STRING_BLANK, STRING_EMPTY);

        assertEquals("4", new GrepResult(STRING_SINGLE_WORD, lines).formatToString(true, false));
        assertEquals(
                String.format(STRING_LABEL_VALUE_PAIR, STRING_SINGLE_WORD, "4"),
                new GrepResult(STRING_SINGLE_WORD, lines).formatToString(true, true)
        );
    }

    @Test
    void formatToString_MultiLinesNotLineNumber_ReturnsMultiLinesString() {
        List<String> lines = List.of(STRING_MULTI_WORDS, STRING_UNICODE, STRING_BLANK, STRING_EMPTY);

        assertEquals(
                String.join(STRING_NEWLINE, lines),
                new GrepResult(STRING_MULTI_WORDS, lines).formatToString(false, false)
        );

        assertEquals(
                String.join(STRING_NEWLINE, lines.stream()
                        .map(line -> String.format(STRING_LABEL_VALUE_PAIR, STRING_MULTI_WORDS, line))
                        .collect(Collectors.toList())
                ),
                new GrepResult(STRING_MULTI_WORDS, lines).formatToString(false, true)
        );
    }
}