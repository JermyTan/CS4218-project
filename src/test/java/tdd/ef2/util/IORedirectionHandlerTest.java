package tdd.ef2.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler;

@Disabled
public class IORedirectionHandlerTest {
    public static final Path CURR_PATH = Path.of(EnvironmentUtil.currentDirectory);
    public static Deque<Path> files = new ArrayDeque<>();
    public static IORedirectionHandler handler;

    @AfterAll
    static void clear() throws IOException {
        handler = null;
    }

    private Path createFile(String name) throws IOException {
        Path path = CURR_PATH.resolve(name);
        Files.createFile(path);
        files.push(path);
        return path;
    }

    //extractRedirOptions cases
    @Test
    void extractRedirOptions_NullArgsList_ThrowsException() {
        ArgumentResolver resolver = new ArgumentResolver();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        handler = new IORedirectionHandler(null, System.in, output, resolver);
        assertThrows(ShellException.class, () -> handler.extractRedirOptions());
    }

    @Test
    void extractRedirOptions_EmptyArgsList_ThrowsException() {
        ArgumentResolver resolver = new ArgumentResolver();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        handler = new IORedirectionHandler(argsList, System.in, output, resolver);
        assertThrows(ShellException.class, () -> handler.extractRedirOptions());
    }

    @Test
    void extractRedirOptions_ConsecutiveRedirOperators_ThrowsException() {
        ArgumentResolver resolver = new ArgumentResolver();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add("<");
        argsList.add("<");
        handler = new IORedirectionHandler(argsList, System.in, output, resolver);
        assertThrows(ShellException.class, () -> handler.extractRedirOptions());
    }

    @Test
    void extractRedirOptions_ConsecutiveFileArgs_ThrowsException() {
        ArgumentResolver resolver = new ArgumentResolver();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add("<");
        argsList.add("fileA.txt fileB.txt");
        handler = new IORedirectionHandler(argsList, System.in, output, resolver);
        assertThrows(ShellException.class, () -> handler.extractRedirOptions());
    }

    //getNoRedirArgsList cases
    @Test
    void getNoRedirArgsList_noRedirOperators_returnsNoRedirArgsList() throws AbstractApplicationException,
            ShellException {
        ArgumentResolver resolver = new ArgumentResolver();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add("fileA.txt");
        argsList.add("fileB.txt");
        argsList.add("fileC.txt");
        List<String> expectedList = new ArrayList<>();
        expectedList.add("fileA.txt");
        expectedList.add("fileB.txt");
        expectedList.add("fileC.txt");
        handler = new IORedirectionHandler(argsList, System.in, output, resolver);
        handler.extractRedirOptions();
        assertTrue(handler.getNoRedirArgsList().containsAll(expectedList));
    }

    @Test
    void getNoRedirArgsList_MultipleArgsOneRedirOperator_returnsNoRedirArgsList() throws AbstractApplicationException,
            ShellException, IOException {
        ArgumentResolver resolver = new ArgumentResolver();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add("fileA.txt");
        argsList.add("fileB.txt");
        argsList.add(">");
        argsList.add("fileC.txt");
        List<String> expectedList = new ArrayList<>();
        expectedList.add("fileA.txt");
        expectedList.add("fileB.txt");
        handler = new IORedirectionHandler(argsList, System.in, output, resolver);
        handler.extractRedirOptions();
        assertTrue(handler.getNoRedirArgsList().containsAll(expectedList));
        handler.getOutputStream().close();
        Path path = CURR_PATH.resolve("fileC.txt");
        Files.delete(path);
    }

    @Test
    void getNoRedirArgsList_OneRedirOperatorMultipleArgs_returnsNoRedirArgsList() throws AbstractApplicationException,
            ShellException, IOException {
        ArgumentResolver resolver = new ArgumentResolver();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add(">");
        argsList.add("fileA.txt");
        argsList.add("fileB.txt");
        argsList.add("fileC.txt");
        List<String> expectedList = new ArrayList<>();
        expectedList.add("fileB.txt");
        expectedList.add("fileC.txt");
        handler = new IORedirectionHandler(argsList, System.in, output, resolver);
        handler.extractRedirOptions();
        assertTrue(handler.getNoRedirArgsList().containsAll(expectedList));
        handler.getOutputStream().close();
        Path path = CURR_PATH.resolve("fileA.txt");
        Files.delete(path);
    }

    @Test
    void getNoRedirArgsList_OneRedirOperatorOneArg_returnsEmptyList() throws AbstractApplicationException,
            ShellException, IOException {
        ArgumentResolver resolver = new ArgumentResolver();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add(">");
        argsList.add("fileA.txt");
        handler = new IORedirectionHandler(argsList, System.in, output, resolver);
        handler.extractRedirOptions();
        assertTrue(handler.getNoRedirArgsList().isEmpty());
        handler.getOutputStream().close();
        Path path = CURR_PATH.resolve("fileA.txt");
        Files.delete(path);
    }

    //getInputStream cases
    @Test
    void getInputStream_BeforeExtract_ReturnsDefault() throws AbstractApplicationException, ShellException {
        ArgumentResolver resolver = new ArgumentResolver();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add("fileA.txt");
        handler = new IORedirectionHandler(argsList, System.in, output, resolver);
        assertEquals(handler.getInputStream(), System.in);
    }

    @Test
    void getInputStream_NoRedir_ReturnsDefault() throws AbstractApplicationException, ShellException {
        ArgumentResolver resolver = new ArgumentResolver();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add("fileA.txt");
        handler = new IORedirectionHandler(argsList, System.in, output, resolver);
        handler.extractRedirOptions();
        assertEquals(handler.getInputStream(), System.in);
    }

    @Test
    void getInputStream_OutputRedir_ReturnsDefault() throws AbstractApplicationException, ShellException,
            IOException {
        ArgumentResolver resolver = new ArgumentResolver();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add("fileA.txt");
        argsList.add(">");
        argsList.add("fileB.txt");
        handler = new IORedirectionHandler(argsList, System.in, output, resolver);
        handler.extractRedirOptions();
        assertEquals(handler.getInputStream(), System.in);
        handler.getOutputStream().close();
        Path path = CURR_PATH.resolve("fileB.txt");
        Files.delete(path);
    }

    @Test
    void getInputStream_InputRedir_ReturnsFileInputStream() throws AbstractApplicationException, ShellException,
            IOException {
        ArgumentResolver resolver = new ArgumentResolver();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add("fileA.txt");
        argsList.add("<");
        argsList.add("fileB.txt");
        Path filePath = createFile("fileB.txt");
        File file = new File(filePath.toString());
        handler = new IORedirectionHandler(argsList, System.in, output, resolver);
        handler.extractRedirOptions();
        assertTrue(handler.getInputStream().getClass() == FileInputStream.class);
        handler.getInputStream().close();
        Files.delete(filePath);
    }

    //getOutputStream cases
    @Test
    void getOutputStream_BeforeExtract_ReturnsDefault() throws AbstractApplicationException, ShellException {
        ArgumentResolver resolver = new ArgumentResolver();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add("fileA.txt");
        handler = new IORedirectionHandler(argsList, System.in, output, resolver);
        assertEquals(handler.getOutputStream(), output);
    }

    @Test
    void getOutputStream_NoRedir_ReturnsDefault() throws AbstractApplicationException, ShellException {
        ArgumentResolver resolver = new ArgumentResolver();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add("fileA.txt");
        handler = new IORedirectionHandler(argsList, System.in, output, resolver);
        handler.extractRedirOptions();
        assertEquals(handler.getOutputStream(), output);
    }

    @Test
    void getOutputStream_InputRedir_ReturnsDefault() throws AbstractApplicationException, ShellException,
            IOException {
        ArgumentResolver resolver = new ArgumentResolver();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add("fileA.txt");
        argsList.add("<");
        argsList.add("fileB.txt");
        Path filePath = createFile("fileB.txt");
        handler = new IORedirectionHandler(argsList, System.in, output, resolver);
        handler.extractRedirOptions();
        assertEquals(handler.getOutputStream(), output);
        handler.getInputStream().close();
        Files.delete(filePath);
    }

    @Test
    void getOutputStream_OutputRedir_ReturnsFileOutputStream() throws AbstractApplicationException, ShellException,
            IOException {
        ArgumentResolver resolver = new ArgumentResolver();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        List<String> argsList = new ArrayList<>();
        argsList.add("fileA.txt");
        argsList.add(">");
        argsList.add("fileB.txt");
        handler = new IORedirectionHandler(argsList, System.in, output, resolver);
        handler.extractRedirOptions();
        assertTrue(handler.getOutputStream().getClass() == FileOutputStream.class);
        handler.getOutputStream().close();
        Path path = CURR_PATH.resolve("fileB.txt");
        Files.delete(path);
    }
}
