package tdd.bf;

import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.EchoException;
import sg.edu.nus.comp.cs4218.impl.app.EchoApplication;

import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class EchoApplicationTest {
    @Test
    public void run_SingleArgument_OutputsArgument() throws EchoException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new EchoApplication().run(new String[] {"A*B*C"}, System.in, output);
        assertArrayEquals(("A*B*C" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    public void run_MultipleArgument_SpaceSeparated() throws EchoException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new EchoApplication().run(new String[] {"A", "B", "C"}, System.in, output);
        assertArrayEquals(("A B C" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    public void run_MultipleArgumentWithSpace_SpaceSeparated() throws EchoException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new EchoApplication().run(new String[] {"A B", "C D"}, System.in, output);
        assertArrayEquals(("A B C D" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    public void run_ZeroArguments_OutputsNewline() throws EchoException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new EchoApplication().run(new String[] {}, System.in, output);
        assertArrayEquals(STRING_NEWLINE.getBytes(), output.toByteArray());
    }
}
