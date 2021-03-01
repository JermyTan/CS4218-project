package sg.edu.nus.comp.cs4218.impl.cmd;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
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

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
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

    private void buildCommand(List<String> argsList) throws ShellException {
        appRunner = mock(ApplicationRunner.class);
        ArgumentResolver argumentResolver = spy(ArgumentResolver.class);
        command = new CallCommand(argsList, appRunner, argumentResolver);
    }

    @Test
    public void initialization_NullArgsList_ThrowsException() {
        assertThrows(ShellException.class, () -> buildCommand(null));
    }

    @Test
    public void initialization_NullAppRunner_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            new CallCommand(List.of("echo", "abc"), null, mock(ArgumentResolver.class));
        });
    }

    @Test
    public void initialization_NullArgumentResolver_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            new CallCommand(List.of("echo", "abc"), mock(ApplicationRunner.class), null);
        });
    }

    @Test
    public void initialization_EmptyArgsList_ThrowsException() {
        assertThrows(ShellException.class, () -> buildCommand(List.of()));
    }

    @Test
    public void initialization_ArgsListContainsNull_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            List<String> list = new ArrayList<>();
            list.add("echo");
            list.add(null);
            list.add("dec");

            buildCommand(list);
        });
    }

    @Test
    public void evaluate_NoIORedirectionNoQuotingNoGlobingNoCommandSub_AppRunWithCorrectArgs() {
        assertDoesNotThrow(() -> {
            buildCommand(List.of("echo", "abc"));

            command.evaluate(stdin, stdout);

            verify(appRunner).runApp("echo", new String[]{"abc"}, stdin, stdout);
        });
    }

    @Test
    public void evaluate_HasIORedirectionNoQuotingNoGlobingNoCommandSub_RedirOptionsExtracted() {
        assertDoesNotThrow(() -> {
            buildCommand(List.of("cat", "<", FILE_1, ">", FILE_2));

            command.evaluate(stdin, stdout);

            // Did not check whether inputStream / outputStream is correctly set here
            verify(appRunner).runApp(eq("cat"), eq(new String[0]), isA(InputStream.class), isA(OutputStream.class));
        });
    }

    @Test
    public void evaluate_NoIORedirectionHasQuotingNoGlobingNoCommandSub_QuotesUnwrapped() {
        assertDoesNotThrow(() -> {
            buildCommand(List.of("echo", "'abc'"));

            command.evaluate(stdin, stdout);

            verify(appRunner).runApp("echo", new String[]{"abc"}, stdin, stdout);
        });
    }

    @Test
    public void evaluate_NoIORedirectionNoQuotingHasGlobingNoCommandSub_GlobbingCompleted() {
        assertDoesNotThrow(() -> {
            buildCommand(List.of("echo", "*.txt"));

            command.evaluate(stdin, stdout);

            String file1 = IOUtils.resolveAbsoluteFilePath(FILE_1).toString();
            String file2 = IOUtils.resolveAbsoluteFilePath(FILE_2).toString();
            verify(appRunner).runApp("echo", new String[]{file1, file2}, stdin, stdout);
        });
    }

    @Test
    public void terminate_BeforeEvaluate_DoesNothing() {
        assertDoesNotThrow(() -> {
            buildCommand(List.of("echo", "'abc'"));

            command.terminate();

            command.evaluate(stdin, stdout);
            verify(appRunner).runApp("echo", new String[]{"abc"}, stdin, stdout);
        });
    }

    @Test
    public void getArgsList_NonEmptyArgsList_ReturnsNonEmptyArgsList() {
        assertDoesNotThrow(() -> {
            List<String> argsList = List.of("echo", "'abc'");
            buildCommand(argsList);

            // test before evaluate
            assertEquals(argsList, command.getArgsList());

            command.evaluate(stdin, stdout);

            // test after evaluation
            assertEquals(argsList, command.getArgsList());
        });
    }
}