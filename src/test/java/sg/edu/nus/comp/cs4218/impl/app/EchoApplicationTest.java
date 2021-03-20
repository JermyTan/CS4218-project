package sg.edu.nus.comp.cs4218.impl.app;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedOutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.EchoException;

class EchoApplicationTest {

    private static final String[] EMPTY = {STRING_EMPTY};
    private static final String[] SPACE = {"  "};
    private static final String[] STRING_1 = {"ABC", "123"};
    private static final String[] STRING_2 = {"0", "5", "abc"};
    private static final String[] STRING_3 = {"!@#$%^&*()_+{}|:<>?.,/~"};
    private static final String[] STRING_4 = null;
    private static final String[] STRING_5 = {"'\"A*B*C\"'"};
    private EchoApplication app;

    @BeforeEach
    void setup() {
        app = new EchoApplication();
    }

    @Test
    public void constructResult_Blank_ShouldWriteBlank() {
        assertDoesNotThrow(() -> {
            assertEquals(STRING_EMPTY, app.constructResult(EMPTY));
            assertEquals("  ", app.constructResult(SPACE));
        });
    }

    @Test
    public void constructResult_AlphanumericWithoutDoubleQuotes_ShouldWrite() {
        assertDoesNotThrow(() -> {
            assertEquals("ABC 123", app.constructResult(STRING_1));
            assertEquals("0 5 abc", app.constructResult(STRING_2));
            assertEquals("'\"A*B*C\"'", app.constructResult(STRING_5));
        });
    }

    @Test
    public void constructResult_SpecialCharacters_ShouldWriteAsIs() {
        assertDoesNotThrow(() -> {
            assertEquals("!@#$%^&*()_+{}|:<>?.,/~", app.constructResult(STRING_3));
        });
    }


    @Test
    public void constructResult_Null_ThrowsException() {
        Throwable error = assertThrows(EchoException.class, () -> app.constructResult(STRING_4));
        assertEquals("echo: " + ERR_NULL_ARGS, error.getMessage());
    }

    @Test
    public void run_NoOutStream_ThrowsException() {
        Throwable error = assertThrows(EchoException.class, () -> app.run(STRING_1, System.in, null));
        assertEquals("echo: " + ERR_NO_OSTREAM, error.getMessage());
    }

    @Test
    public void run_RuntimeIOException_ThrowsException() throws IOException {
        try (OutputStream out = new PipedOutputStream()) {
            out.close();
            Throwable error = assertThrows(EchoException.class, () -> app.run(STRING_1, System.in, out));
            assertEquals("echo: " + ERR_WRITE_STREAM, error.getMessage());
        }
    }
}