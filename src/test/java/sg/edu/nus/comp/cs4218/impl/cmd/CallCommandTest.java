package sg.edu.nus.comp.cs4218.impl.cmd;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.SHELL;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

class CallCommandTest {

    private static final String ORIGINAL_DIR = Environment.currentDirectory;
    private static final String TEST_DIR = Environment.currentDirectory + File.separator + RESOURCES_PATH + File.separator + "CallCommandTest";

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";
    private final InputStream stdin = mock(InputStream.class);
    private final OutputStream stdout = mock(OutputStream.class);
    private CallCommand command;
    private ApplicationRunner appRunner;

    @BeforeAll
    static void setupBeforeAll() {
        Environment.currentDirectory = TEST_DIR;
    }

    @AfterAll
    static void tearDownAfterAll() {
        Environment.currentDirectory = ORIGINAL_DIR;
    }

    private void buildCommand(List<String> argList) {
        appRunner = mock(ApplicationRunner.class);
        ArgumentResolver argumentResolver = spy(ArgumentResolver.class);
        command = new CallCommand(argList, appRunner, argumentResolver);
    }

    @Test
    public void evaluate_NullArgList_ThrowsException() {
        buildCommand(null);

        Throwable exception = assertThrows(ShellException.class, () -> command.evaluate(stdin, stdout));
        assertEquals(String.format(STRING_LABEL_VALUE_PAIR, SHELL, ERR_SYNTAX), exception.getMessage());
    }

    @Test
    public void evaluate_EmptyArgList_ThrowsException() {
        buildCommand(new ArrayList<>());

        Throwable exception = assertThrows(ShellException.class, () -> command.evaluate(stdin, stdout));
        assertEquals(String.format(STRING_LABEL_VALUE_PAIR, SHELL, ERR_SYNTAX), exception.getMessage());
    }

    @Test
    public void evaluate_NoIORedirectionNoQuotingNoGlobingNoCommandSub_AppRunWithCorrectArgs() {
        buildCommand(List.of("echo", "abc"));//NOPMD

        assertDoesNotThrow(() -> {
            command.evaluate(stdin, stdout);
            verify(appRunner).runApp("echo", new String[]{"abc"}, stdin, stdout);
        });
    }

    @Test
    public void evaluate_HasIORedirectionNoQuotingNoGlobingNoCommandSub_RedirOptionsExtracted() {
        buildCommand(List.of("cat", "<", FILE_1, ">", FILE_2));

        assertDoesNotThrow(() -> {
            command.evaluate(stdin, stdout);

            // Did not check whether inputStream / outputStream is correctly set here
            verify(appRunner).runApp(eq("cat"), eq(new String[0]), isA(InputStream.class), isA(OutputStream.class));
        });
    }

    @Test
    public void evaluate_NoIORedirectionHasQuotingNoGlobingNoCommandSub_QuotesUnwrapped() {
        buildCommand(List.of("echo", "'abc'"));

        assertDoesNotThrow(() -> {
            command.evaluate(stdin, stdout);
            verify(appRunner).runApp("echo", new String[]{"abc"}, stdin, stdout);
        });
    }

    @Test
    public void evaluate_NoIORedirectionNoQuotingHasGlobingNoCommandSub_GlobbingCompleted() {
        buildCommand(List.of("echo", "*.txt"));

        assertDoesNotThrow(() -> {
            command.evaluate(stdin, stdout);

            String file1 = IOUtils.resolveAbsoluteFilePath(FILE_1).toString();
            String file2 = IOUtils.resolveAbsoluteFilePath(FILE_2).toString();
            verify(appRunner).runApp("echo", new String[]{file1, file2}, stdin, stdout);
        });
    }
}