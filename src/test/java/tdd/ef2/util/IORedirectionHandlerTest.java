package tdd.ef2.util;

import org.junit.jupiter.api.*;
import sg.edu.nus.comp.cs4218.*;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.util.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.*;

public class IORedirectionHandlerTest {
    private static final Path TEST_PATH = Path.of(EnvironmentUtil.currentDirectory, RESOURCES_PATH, "IORedirectionHandlerTest");
    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = TEST_PATH.toString();
    private IORedirectionHandler handler;

    @BeforeAll
    static void setupAll() throws Exception {
        if (Files.notExists(TEST_PATH)) {
            Files.createDirectory(TEST_PATH);
        }

        EnvironmentUtil.currentDirectory = TEST_DIR;
    }

    @AfterAll
    static void tearDownAll() throws IOException {
        EnvironmentUtil.currentDirectory = ORIGINAL_DIR;

        Files.walk(TEST_PATH)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.list(TEST_PATH).forEach(path -> {
            try {
                Files.deleteIfExists(path);
            } catch (Exception e) {
                // do nth
            }
        });
    }

    private Path createFile(String name) throws IOException {
        Path path = TEST_PATH.resolve(name);

        if (Files.notExists(path)) {
            Files.createFile(path);
        }

        return path;
    }

    @Test
    void constructor_NullArgsList_ThrowsException() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertThrows(ShellException.class, () -> new IORedirectionHandler(null, System.in, output));
    }

    //extractRedirOptions cases
    @Test
    void extractRedirOptions_ConsecutiveRedirOperators_ThrowsException() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add("<");
        argsList.add("<");
        handler = new IORedirectionHandler(argsList, System.in, output);
        assertThrows(ShellException.class, () -> handler.extractRedirOptions());
    }

    @Test
    void extractRedirOptions_ConsecutiveFileArgs_ThrowsException() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add("<");
        argsList.add("fileA.txt fileB.txt");
        handler = new IORedirectionHandler(argsList, System.in, output);
        assertThrows(ShellException.class, () -> handler.extractRedirOptions());
    }

    @Test
    void extractRedirOptions_OneRedirOperatorMultipleArgs_ThrowsException() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add(">");
        argsList.add("fileA.txt");
        argsList.add("fileB.txt");
        argsList.add("fileC.txt");
        List<String> expectedList = new ArrayList<>();
        expectedList.add("fileB.txt");
        expectedList.add("fileC.txt");
        handler = new IORedirectionHandler(argsList, System.in, output);
        Throwable exception = assertThrows(ShellException.class, () -> handler.extractRedirOptions());
        assertEquals(new ShellException(ERR_SYNTAX).getMessage(), exception.getMessage());
        handler.getOutputStream().close();
    }

    //getNoRedirArgsList cases
    @Test
    void getNoRedirArgsList_noRedirOperators_returnsNoRedirArgsList() throws AbstractApplicationException,
            ShellException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add("fileA.txt");
        argsList.add("fileB.txt");
        argsList.add("fileC.txt");
        List<String> expectedList = new ArrayList<>();
        expectedList.add("fileA.txt");
        expectedList.add("fileB.txt");
        expectedList.add("fileC.txt");
        handler = new IORedirectionHandler(argsList, System.in, output);
        handler.extractRedirOptions();
        assertTrue(handler.getNoRedirArgsList().containsAll(expectedList));
    }

    @Test
    void getNoRedirArgsList_MultipleArgsOneRedirOperator_returnsNoRedirArgsList() throws AbstractApplicationException,
            ShellException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add("fileA.txt");
        argsList.add("fileB.txt");
        argsList.add(">");
        argsList.add("fileC.txt");
        List<String> expectedList = new ArrayList<>();
        expectedList.add("fileA.txt");
        expectedList.add("fileB.txt");
        handler = new IORedirectionHandler(argsList, System.in, output);
        handler.extractRedirOptions();
        assertTrue(handler.getNoRedirArgsList().containsAll(expectedList));
        handler.getOutputStream().close();
    }

    @Test
    void getNoRedirArgsList_OneRedirOperatorOneArg_Success() throws AbstractApplicationException,
            ShellException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add(">");
        argsList.add("fileA.txt");
        handler = new IORedirectionHandler(argsList, System.in, output);
        handler.extractRedirOptions();
        assertTrue(handler.getNoRedirArgsList().isEmpty());
        assertTrue(Files.exists(Path.of(TEST_DIR, "fileA.txt")));
        handler.getOutputStream().close();
    }

    //getInputStream cases
    @Test
    void getInputStream_BeforeExtract_ReturnsDefault() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add("fileA.txt");
        handler = new IORedirectionHandler(argsList, System.in, output);
        assertEquals(handler.getInputStream(), System.in);
    }

    @Test
    void getInputStream_NoRedir_ReturnsDefault() throws AbstractApplicationException, ShellException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add("fileA.txt");
        handler = new IORedirectionHandler(argsList, System.in, output);
        handler.extractRedirOptions();
        assertEquals(handler.getInputStream(), System.in);
    }

    @Test
    void getInputStream_OutputRedir_ReturnsDefault() throws AbstractApplicationException, ShellException,
            IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add("fileA.txt");
        argsList.add(">");
        argsList.add("fileB.txt");
        handler = new IORedirectionHandler(argsList, System.in, output);
        handler.extractRedirOptions();
        assertEquals(handler.getInputStream(), System.in);
        handler.getOutputStream().close();
    }

    @Test
    void getInputStream_InputRedir_ReturnsFileInputStream() throws AbstractApplicationException, ShellException,
            IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add("fileA.txt");
        argsList.add("<");
        argsList.add("fileB.txt");
        Path filePath = createFile("fileB.txt");
        File file = new File(filePath.toString());
        handler = new IORedirectionHandler(argsList, System.in, output);
        handler.extractRedirOptions();
        assertNotEquals(handler.getInputStream(), System.in);
        handler.getInputStream().close();
    }

    //getOutputStream cases
    @Test
    void getOutputStream_BeforeExtract_ReturnsDefault() throws AbstractApplicationException, ShellException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add("fileA.txt");
        handler = new IORedirectionHandler(argsList, System.in, output);
        assertEquals(handler.getOutputStream(), output);
    }

    @Test
    void getOutputStream_NoRedir_ReturnsDefault() throws AbstractApplicationException, ShellException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add("fileA.txt");
        handler = new IORedirectionHandler(argsList, System.in, output);
        handler.extractRedirOptions();
        assertEquals(handler.getOutputStream(), output);
    }

    @Test
    void getOutputStream_InputRedir_ReturnsDefault() throws AbstractApplicationException, ShellException,
            IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add("fileA.txt");
        argsList.add("<");
        argsList.add("fileB.txt");
        createFile("fileB.txt");
        handler = new IORedirectionHandler(argsList, System.in, output);
        handler.extractRedirOptions();
        assertEquals(handler.getOutputStream(), output);
        handler.getInputStream().close();
    }

    @Test
    void getOutputStream_OutputRedir_ReturnsFileOutputStream() throws AbstractApplicationException, ShellException,
            IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add("fileA.txt");
        argsList.add(">");
        argsList.add("fileB.txt");
        handler = new IORedirectionHandler(argsList, System.in, output);
        handler.extractRedirOptions();
        assertNotEquals(handler.getOutputStream(), output);
        handler.getOutputStream().close();
    }
}
