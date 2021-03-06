package sg.edu.nus.comp.cs4218.impl.cmd;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_CAT;
import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_ECHO;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_SINGLE_WORD;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;

class CallCommandTest {

    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + RESOURCES_PATH + STRING_FILE_SEP + "CallCommandTest";

    private static final String FILE_1 = "file1.txt";
    private static final String FILE_2 = "file2.txt";
    private final InputStream stdin = mock(InputStream.class);
    private final OutputStream stdout = mock(OutputStream.class);
    private CallCommand command;
    private ApplicationRunner appRunner;

    @BeforeAll
    static void setupBeforeAll() {
        EnvironmentUtil.currentDirectory = TEST_DIR;
    }

    @AfterAll
    static void tearDownAfterAll() {
        EnvironmentUtil.currentDirectory = ORIGINAL_DIR;
    }

    private void buildCommand(List<String> argsList) throws ShellException {
        appRunner = mock(ApplicationRunner.class);
        command = new CallCommand(argsList, appRunner);
    }

    @Test
    public void initialization_NullArgsList_ThrowsException() {
        assertThrows(ShellException.class, () -> buildCommand(null));
    }

    @Test
    public void initialization_NullAppRunner_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            new CallCommand(List.of(APP_ECHO, STRING_SINGLE_WORD), null);
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
            list.add(APP_ECHO);
            list.add(null);
            list.add(STRING_SINGLE_WORD);

            buildCommand(list);
        });
    }

    @Test
    public void evaluate_NoIORedirectionNoQuotingNoGlobingNoCommandSub_AppRunWithCorrectArgs() {
        assertDoesNotThrow(() -> {
            buildCommand(List.of(APP_ECHO, STRING_SINGLE_WORD));

            command.evaluate(stdin, stdout);

            verify(appRunner).runApp(APP_ECHO, new String[]{STRING_SINGLE_WORD}, stdin, stdout);
        });
    }

    @Test
    public void evaluate_EmptyParsedArgList_NoAppRun() {
        assertDoesNotThrow(() -> {
            buildCommand(List.of("<", FILE_1, ">", FILE_2));

            command.evaluate(stdin, stdout);

            // No app is run
            verify(appRunner, never()).runApp(any(), any(), eq(stdin), eq(stdout));
        });
    }

    @Test
    public void evaluate_HasIORedirectionNoQuotingNoGlobingNoCommandSub_RedirOptionsExtracted() {
        assertDoesNotThrow(() -> {
            buildCommand(List.of(APP_CAT, "<", FILE_1, ">", FILE_2));

            command.evaluate(stdin, stdout);

            // Did not check whether inputStream / outputStream is correctly set here
            verify(appRunner).runApp(eq(APP_CAT), eq(new String[0]), isA(InputStream.class), isA(OutputStream.class));
        });
    }

    @Test
    public void evaluate_NoIORedirectionHasQuotingNoGlobingNoCommandSub_QuotesUnwrapped() {
        assertDoesNotThrow(() -> {
            buildCommand(List.of(APP_ECHO, "'Test'"));

            command.evaluate(stdin, stdout);

            verify(appRunner).runApp(APP_ECHO, new String[]{STRING_SINGLE_WORD}, stdin, stdout);
        });
    }

    @Test
    public void evaluate_NoIORedirectionNoQuotingHasGlobingNoCommandSub_GlobbingCompleted() {
        assertDoesNotThrow(() -> {
            buildCommand(List.of(APP_ECHO, "*.txt"));

            command.evaluate(stdin, stdout);

            verify(appRunner).runApp(APP_ECHO, new String[]{FILE_1, FILE_2}, stdin, stdout);
        });
    }

    @Test
    public void terminate_BeforeEvaluate_DoesNothing() {
        assertDoesNotThrow(() -> {
            buildCommand(List.of(APP_ECHO, STRING_SINGLE_WORD));

            command.terminate();

            command.evaluate(stdin, stdout);
            verify(appRunner).runApp(APP_ECHO, new String[]{STRING_SINGLE_WORD}, stdin, stdout);
        });
    }

    @Test
    public void getArgsList_NonEmptyArgsList_ReturnsNonEmptyArgsList() {
        assertDoesNotThrow(() -> {
            List<String> argsList = List.of(APP_ECHO, STRING_SINGLE_WORD);
            buildCommand(argsList);

            // test before evaluate
            assertEquals(argsList, command.getArgsList());

            command.evaluate(stdin, stdout);

            // test after evaluation
            assertEquals(argsList, command.getArgsList());
        });
    }
}