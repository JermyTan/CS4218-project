package tdd.integration;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.*;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;

public class IntegrationTest {
    public static final String TEMP = "temp";
    public static final Path TEMP_PATH = Path.of(EnvironmentUtil.currentDirectory, TEMP);

    @BeforeEach
    void createTemp() throws IOException {
        Files.createDirectory(TEMP_PATH);
    }

    @AfterEach
    void deleteTemp() throws IOException {
        Files.walk(TEMP_PATH)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    private Path createFile(String name) throws IOException {
        return createFile(name, TEMP_PATH);
    }

    private Path createFile(String name, Path inPath) throws IOException {
        Path path = inPath.resolve(name);
        Files.createFile(path);
        return path;
    }

    // Command Substitution

    @Test
    void testCommandSubstitution_EchoLs_DisplaysLsResult() throws IOException, AbstractApplicationException, ShellException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        createFile("Hello");
        String commandString = "echo `ls " + TEMP + "`";
        new ShellImpl().parseAndEvaluate(commandString, output);
        assertEquals("Hello" + STRING_NEWLINE, output.toString());
    }

    @Test
    void testCommandSubstitution_EchoEcho_DisplaysInnerEchoResult() throws AbstractApplicationException, ShellException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String commandString = "echo `echo \"hello\"`";
        new ShellImpl().parseAndEvaluate(commandString, output);
        assertArrayEquals(("hello" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void testCommandSubstitution_WcEcho_DisplaysLinesWordsBytesFilename() throws IOException, AbstractApplicationException, ShellException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "file.txt";
        Path filePath = createFile(fileName);
        String commandString = "wc `echo " + TEMP + STRING_FILE_SEP + fileName + "`";
        new ShellImpl().parseAndEvaluate(commandString, output);
        assertEquals(String.join(String.valueOf(CHAR_TAB), "0", "0", "0") + CHAR_TAB + TEMP + STRING_FILE_SEP + fileName + STRING_NEWLINE, output.toString());
    }

    @Test
    void testCommandSubstitution_MultipleCommandSubstitutions_DisplaysEchoResults() throws ShellException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String commandString = "echo `echo 1` `echo 2`";
        new ShellImpl().parseAndEvaluate(commandString, output);
        assertArrayEquals(("1 2" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    // Sequence Command

    @Test
    void testSequenceCommand_EchoEcho_DisplaysEchoResults() throws ShellException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String commandString = "echo 1; echo 2";
        new ShellImpl().parseAndEvaluate(commandString, output);
        assertArrayEquals(("1" + STRING_NEWLINE + "2" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void testSequenceCommand_EchoCat_DisplaysEchoCatResults() throws ShellException, AbstractApplicationException,
            IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "file.txt";
        Path filePath = createFile(fileName);
        String text = "file contents";
        Files.write(filePath, text.getBytes());
        String commandString = "echo 1; cat " + TEMP + STRING_FILE_SEP + fileName;
        new ShellImpl().parseAndEvaluate(commandString, output);
        assertArrayEquals((1 + STRING_NEWLINE + text + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void testSequenceCommand_EchoPaste_DisplaysEchoPasteResults() throws ShellException, AbstractApplicationException,
            IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "file.txt";
        Path filePath = createFile(fileName);
        String text = "file contents";
        Files.write(filePath, text.getBytes());
        String commandString = "echo 1; paste " + TEMP + STRING_FILE_SEP + fileName;
        new ShellImpl().parseAndEvaluate(commandString, output);
        assertArrayEquals((1 + STRING_NEWLINE + text + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void testSequenceCommand_MultipleSequenceCommands_DisplaysEchoResults() throws ShellException,
            AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String commandString = "echo 1; echo 2; echo 3; echo 4";
        new ShellImpl().parseAndEvaluate(commandString, output);
        assertArrayEquals(("1" + STRING_NEWLINE + "2" + STRING_NEWLINE + "3" + STRING_NEWLINE + "4" +
                STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    // Quoting

    @Test
    void testQuoting_Echo_DisplaysEchoResults() throws ShellException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String commandString = "echo 'test echo'";
        new ShellImpl().parseAndEvaluate(commandString, output);
        assertArrayEquals(("test echo" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void testQuoting_EchoBackQuote_DisplaysEchoResults() throws ShellException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String commandString = "echo 'test echo`'";
        new ShellImpl().parseAndEvaluate(commandString, output);
        assertArrayEquals(("test echo`" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void testQuoting_EchoDoubleQuote_DisplaysEchoResults() throws ShellException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String commandString = "echo \"test echo\"";
        new ShellImpl().parseAndEvaluate(commandString, output);
        assertArrayEquals(("test echo" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }

    @Test
    void testQuoting_EchoSingleQuote_DisplaysEchoResults() throws ShellException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String commandString = "echo \"test echo ' '\"";
        new ShellImpl().parseAndEvaluate(commandString, output);
        assertArrayEquals(("test echo ' '" + STRING_NEWLINE).getBytes(), output.toByteArray());
    }
}
