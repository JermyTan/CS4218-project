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

class ArgumentResolverStub extends ArgumentResolver {
    @Override
    public List<String> resolveOneArgument(String arg) {
        return List.of(arg);
    }
}

class IORedirectionHandlerTest {

    private String FILE_1 = "file1.txt";
    private String FILE_2 = "file2.txt";

    private File file1 = new File(FILE_1); // exists
    private File file2 = new File(FILE_2); // does not exist

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
    public void extractRedirOptions_nullArgList_throwsException() {
        IORedirectionHandler redirHandler = constructRedirHandler(null);
        Throwable e = assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_emptyArgList_throwsException() {
        IORedirectionHandler redirHandler = constructRedirHandler(new LinkedList<>());
        Throwable e = assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_noFileGiven_throwsException() {
        IORedirectionHandler redirHandler = constructRedirHandler(List.of("paste", "<"));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_consecutiveRedirOptions_throwsException() {
        IORedirectionHandler redirHandler = constructRedirHandler(List.of("paste", "<", "<"));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_twoInputRedir_throwsException() {
        IORedirectionHandler redirHandler = constructRedirHandler(List.of("paste", "<", FILE_1, "<", FILE_2));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_twoOutputRedir_throwsException() {
        IORedirectionHandler redirHandler = constructRedirHandler(List.of("paste", ">", FILE_1, ">", FILE_2));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_inputRedirFileDoesNotExist_throwsException() {
        IORedirectionHandler redirHandler = constructRedirHandler(List.of("paste", "<", FILE_2));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_outputRedirMultipleFiles_throwsException() {
        IORedirectionHandler redirHandler = constructRedirHandler(List.of("paste", ">", FILE_1, FILE_2));
        assertThrows(ShellException.class, () -> redirHandler.extractRedirOptions());
    }

    @Test
    public void extractRedirOptions_validArgs_removesRedirOptions() {
        IORedirectionHandler redirHandler = constructRedirHandler(List.of("paste", "<", FILE_1, ">", FILE_2));

        assertDoesNotThrow(() -> redirHandler.extractRedirOptions());

        List<String> noRedirArgsList = redirHandler.getNoRedirArgsList();
        assertEquals(1, noRedirArgsList.size());
        assertEquals("paste", noRedirArgsList.get(0));
    }
}