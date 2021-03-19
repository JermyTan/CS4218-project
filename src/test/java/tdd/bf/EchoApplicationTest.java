package tdd.bf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.EchoException;
import sg.edu.nus.comp.cs4218.impl.app.EchoApplication;

public class EchoApplicationTest {
    @Test
    public void run_SingleArgument_OutputsArgument() throws EchoException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new EchoApplication().run(new String[]{"A*B*C"}, System.in, output);
        assertEquals("A*B*C" + STRING_NEWLINE, output.toString());
    }

    @Test
    public void run_MultipleArgument_SpaceSeparated() throws EchoException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new EchoApplication().run(new String[]{"A", "B", "C"}, System.in, output);
        assertEquals("A B C" + STRING_NEWLINE, output.toString());
    }

    @Test
    public void run_MultipleArgumentWithSpace_SpaceSeparated() throws EchoException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new EchoApplication().run(new String[]{"A B", "C D"}, System.in, output);
        assertEquals("A B C D" + STRING_NEWLINE, output.toString());
    }

    @Test
    public void run_ZeroArguments_OutputsNewline() throws EchoException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new EchoApplication().run(new String[]{}, System.in, output);
        assertEquals(STRING_NEWLINE, output.toString());
    }
}
