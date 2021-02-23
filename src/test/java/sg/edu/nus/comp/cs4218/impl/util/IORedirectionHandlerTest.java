package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_REDIR_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_REDIR_OUTPUT;

class ArgumentResolverStub extends ArgumentResolver {
    @Override
    public List<String> resolveOneArgument(String arg) {
        return List.of(arg);
    }
}

class IORedirectionHandlerTest {
    private final String STRING_REDIR_INPUT = String.valueOf(CHAR_REDIR_INPUT);
    private final String STRING_REDIR_OUTPUT = String.valueOf(CHAR_REDIR_OUTPUT);

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";

    private final File file1 = new File(FILE_1); // exists
    private final File file2 = new File(FILE_2); // does not exist

    @BeforeEach
    void setup() throws IOException {
        file1.createNewFile();
    }

    @AfterEach
    void tearDown() {
        file1.delete();
        file2.delete();
    }

    private IORedirectionHandler constructRedirHandler(List<String> argsList) {
        return new IORedirectionHandler(argsList, System.in, System.out, new ArgumentResolverStub());
    }

    @Test
    public void extractRedirOptions_NullArgList_ThrowsException() {
        IORedirectionHandler redirHandler = constructRedirHandler(null);
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_EmptyArgList_ThrowsException() {
        IORedirectionHandler redirHandler = constructRedirHandler(new LinkedList<>());
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_NoFileSpecified_ThrowsException() {
        IORedirectionHandler redirHandler = constructRedirHandler(List.of(STRING_REDIR_INPUT));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_ConsecutiveRedirOptions_ThrowsException() {
        IORedirectionHandler redirHandler = constructRedirHandler(List.of(STRING_REDIR_INPUT, STRING_REDIR_INPUT));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_TwoInputRedir_ThrowsException() {
        IORedirectionHandler redirHandler = constructRedirHandler(List.of(STRING_REDIR_INPUT, FILE_1, STRING_REDIR_INPUT, FILE_2));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_TwoOutputRedir_ThrowsException() {
        IORedirectionHandler redirHandler = constructRedirHandler(List.of(STRING_REDIR_OUTPUT, FILE_1, STRING_REDIR_OUTPUT, FILE_2));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_InputRedirFileDoesNotExist_ThrowsException() {
        IORedirectionHandler redirHandler = constructRedirHandler(List.of(STRING_REDIR_INPUT, FILE_2));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_OutputRedirMultipleFiles_ThrowsException() {
        IORedirectionHandler redirHandler = constructRedirHandler(List.of(STRING_REDIR_OUTPUT, FILE_1, FILE_2));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_ValidArgList_RemovesRedirOptions() {
        IORedirectionHandler redirHandler = constructRedirHandler(List.of("paste", STRING_REDIR_INPUT, FILE_1, STRING_REDIR_OUTPUT, FILE_2));

        assertDoesNotThrow(() -> redirHandler.extractRedirOptions());

        List<String> noRedirArgsList = redirHandler.getNoRedirArgsList();
        assertEquals(1, noRedirArgsList.size());
        assertEquals("paste", noRedirArgsList.get(0));
    }
}