package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_REDIR_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_REDIR_OUTPUT;

class IORedirectionHandlerTest {

    private final String STRING_REDIR_INPUT = String.valueOf(CHAR_REDIR_INPUT);
    private final String STRING_REDIR_OUTPUT = String.valueOf(CHAR_REDIR_OUTPUT);

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";

    private final Path file1 = Paths.get(FILE_1); // exists
    private final Path file2 = Paths.get(FILE_2); // does not exist

    private IORedirectionHandler redirHandler;
    private ArgumentResolver mockedArgumentResolver;
    private InputStream mockedStdin;
    private OutputStream mockedStdout;

    @BeforeEach
    void setup() {
        try {
            Files.createFile(file1);

            mockedArgumentResolver = mock(ArgumentResolver.class);
            when(mockedArgumentResolver.resolveOneArgument(FILE_1)).thenReturn(List.of(FILE_1));
            when(mockedArgumentResolver.resolveOneArgument(FILE_2)).thenReturn(List.of(FILE_2));

            mockedStdin = mock(InputStream.class);
            mockedStdout = mock(OutputStream.class);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        try {
            Files.deleteIfExists(file1);
            Files.deleteIfExists(file2);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private void buildRedirHandler(List<String> argsList) {
        redirHandler = new IORedirectionHandler(argsList, mockedStdin, mockedStdout, mockedArgumentResolver);
    }

    @Test
    public void extractRedirOptions_NullArgList_ThrowsException() {
        buildRedirHandler(null);
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_EmptyArgList_ThrowsException() {
        buildRedirHandler(new LinkedList<>());
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_NoFileSpecified_ThrowsException() {
        buildRedirHandler(List.of(STRING_REDIR_INPUT));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_ConsecutiveRedirOptions_ThrowsException() {
        buildRedirHandler(List.of(STRING_REDIR_INPUT, STRING_REDIR_INPUT));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_TwoInputRedir_ThrowsException() {
        buildRedirHandler(List.of(STRING_REDIR_INPUT, FILE_1, STRING_REDIR_INPUT, FILE_2));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_TwoOutputRedir_ThrowsException() {
        buildRedirHandler(List.of(STRING_REDIR_OUTPUT, FILE_1, STRING_REDIR_OUTPUT, FILE_2));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_InputRedirFileDoesNotExist_ThrowsException() {
        buildRedirHandler(List.of(STRING_REDIR_INPUT, FILE_2));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_OutputRedirMultipleFiles_ThrowsException() {
        buildRedirHandler(List.of(STRING_REDIR_OUTPUT, FILE_1, FILE_2));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_ValidArgList_RemovesRedirOptions() {
        buildRedirHandler(List.of("paste", STRING_REDIR_INPUT, FILE_1, STRING_REDIR_OUTPUT, FILE_2));

        assertDoesNotThrow(() -> redirHandler.extractRedirOptions());

        List<String> noRedirArgsList = redirHandler.getNoRedirArgsList();
        assertEquals(1, noRedirArgsList.size());
        assertEquals("paste", noRedirArgsList.get(0));
    }
}