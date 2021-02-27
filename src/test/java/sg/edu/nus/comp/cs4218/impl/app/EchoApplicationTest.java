package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.EchoException;


import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

class EchoApplicationTest {

    private EchoApplication app;

    private static final String[] EMPTY = {""};
    private static final String[] SPACE = {"  "};
    private static final String[] STRING_1 = {"ABC", "123"};
    private static final String[] STRING_2 = {"0", "5", "abc"};
    private static final String[] STRING_3 = {"!@#$%^&*()_+{}|:<>?.,/~"};
    private static final String[] STRING_4 = null;
    private static final String[] STRING_5 = {"'\"A*B*C\"'"};


    @BeforeEach
    void setup() {
        app = new EchoApplication();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void echo_Blank_ShouldWriteBlank() throws EchoException {
        assertEquals("", app.constructResult(EMPTY));
        assertEquals("  ", app.constructResult(SPACE));
    }

    @Test
    public void echo_AlphanumericWithoutDoubleQuotes_ShouldWrite() throws EchoException {
        assertEquals("ABC 123", app.constructResult(STRING_1));
        assertEquals("0 5 abc", app.constructResult(STRING_2));
        assertEquals("'\"A*B*C\"'", app.constructResult(STRING_5));
    }

    @Test
    public void echo_SpecialCharacters_ShouldWriteAsIs() throws EchoException {
        assertEquals("!@#$%^&*()_+{}|:<>?.,/~", app.constructResult(STRING_3));
    }


    @Test
    public void echo_Null_ShouldThrow() throws EchoException {
        Throwable error = assertThrows(EchoException.class, () -> app.constructResult(STRING_4));
        assertEquals("echo: " + ERR_NULL_ARGS, error.getMessage());
    }

    @Test
    public void echo_NoOutStream_ShouldThrow() {
        Throwable error = assertThrows(EchoException.class, () -> app.run(STRING_1, System.in, null));
        assertEquals("echo: " + ERR_NO_OSTREAM, error.getMessage());
    }

    @Test
    public void echo_RuntimeIOException_ShouldThrow() throws IOException {
        try (OutputStream out = new PipedOutputStream()) {
            out.close();
            Throwable error = assertThrows(EchoException.class, () -> app.run(STRING_1, System.in, out));
            assertEquals("echo: " + ERR_WRITE_STREAM, error.getMessage());
        }
    }
}