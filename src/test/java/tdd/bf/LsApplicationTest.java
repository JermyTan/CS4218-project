package tdd.bf;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_CURR_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_FLAG;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.exception.LsException;
import sg.edu.nus.comp.cs4218.impl.app.LsApplication;

public class LsApplicationTest {
    public static final String TEMP = "temp-ls";
    public static final Path TEMP_PATH = Paths.get(EnvironmentUtil.currentDirectory, TEMP);
    public static Deque<Path> files = new ArrayDeque<>();
    private OutputStream stderr;

    private void captureErr() {
        stderr = new ByteArrayOutputStream();
        System.setErr(new PrintStream(stderr));
    }

    private String getErrOutput() {
        System.setErr(System.err);
        return stderr.toString();
    }

    @BeforeEach
    void createTemp() throws IOException {
        Files.createDirectory(TEMP_PATH);
    }

    @AfterEach
    void deleteTemp() throws IOException {
        for (Path file : files) {
            Files.deleteIfExists(file);
        }
        Files.delete(TEMP_PATH);
    }

    private Path createFile(String name) throws IOException {
        return createFile(name, TEMP_PATH);
    }

    private Path createDirectory(String folder) throws IOException {
        return createDirectory(folder, TEMP_PATH);
    }

    private Path createFile(String name, Path inPath) throws IOException {
        Path path = inPath.resolve(name);
        Files.createFile(path);
        files.push(path);
        return path;
    }

    private Path createDirectory(String folder, Path inPath) throws IOException {
        Path path = inPath.resolve(folder);
        Files.createDirectory(path);
        files.push(path);
        return path;
    }

    private String[] toArgs(String flag, String... files) {
        List<String> args = new ArrayList<>();
        if (!flag.isEmpty()) {
            args.add(STRING_STDIN_FLAG + flag);
        }
        for (String file : files) {
            args.add(Paths.get(TEMP, file).toString());
        }
        return args.toArray(new String[0]);
    }

    @Test
    void run_NoDirectoriesNoFlags_DisplaysFilesAndDirectories() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String currPathString = EnvironmentUtil.currentDirectory;
        EnvironmentUtil.currentDirectory = TEMP_PATH.toString();
        createFile("fileA.txt");
        createDirectory("folderB");
        new LsApplication().run(toArgs(STRING_EMPTY), System.in, output);
        assertEquals("fileA.txt" + STRING_NEWLINE + "folderB" + STRING_NEWLINE, output.toString());
        EnvironmentUtil.currentDirectory = currPathString;
    }

    @Test
    void run_NoDirectoriesDirectoryFlag_DisplaysDirectories() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String currPathString = EnvironmentUtil.currentDirectory;
        EnvironmentUtil.currentDirectory = TEMP_PATH.toString();
        createFile("fileA.txt");
        createDirectory("folderB");
        new LsApplication().run(toArgs("d"), System.in, output);
        assertEquals("folderB" + STRING_NEWLINE, output.toString());
        EnvironmentUtil.currentDirectory = currPathString;
    }

    @Test
    void run_NoDirectoriesRecursiveFlag_DisplaysFilesAndDirectoriesRecursively() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String currPathString = EnvironmentUtil.currentDirectory;
        EnvironmentUtil.currentDirectory = TEMP_PATH.toString();
        createFile("fileA.txt");
        Path folderBPath = createDirectory("folderB");
        createFile("file_in_folderB.txt", folderBPath);
        new LsApplication().run(toArgs("R"), System.in, output);
        assertEquals("fileA.txt" + STRING_NEWLINE + "folderB" + STRING_NEWLINE + STRING_NEWLINE + STRING_CURR_DIR + STRING_FILE_SEP +
                "folderB:" + STRING_NEWLINE + "file_in_folderB.txt" + STRING_NEWLINE, output.toString());
        EnvironmentUtil.currentDirectory = currPathString;
    }

    @Test
    void run_NoDirectoriesSortFlag_DisplaysSortedFilesAndDirectories() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String currPathString = EnvironmentUtil.currentDirectory;
        EnvironmentUtil.currentDirectory = TEMP_PATH.toString();
        createFile("fileA.txt");
        createFile("imageB.jpg");
        createFile("documentC.doc");
        createDirectory("folderD");
        new LsApplication().run(toArgs("X"), System.in, output);
        assertEquals("folderD" + STRING_NEWLINE + "documentC.doc" + STRING_NEWLINE + "imageB.jpg" + STRING_NEWLINE +
                "fileA.txt" + STRING_NEWLINE, output.toString());
        EnvironmentUtil.currentDirectory = currPathString;
    }

    @Test
    void run_SingleEmptyDirectoryNoFlags_DisplaysEmpty() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        createDirectory("empty_folder");
        new LsApplication().run(toArgs(STRING_EMPTY, "empty_folder"), System.in, output);
        assertEquals(STRING_EMPTY, output.toString());
    }

    @Test
    void run_SingleEmptyDirectoryAllFlags_DisplaysEmpty() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        createDirectory("empty_folder");
        new LsApplication().run(toArgs("RdX", "empty_folder"), System.in, output);
        assertEquals(STRING_EMPTY, output.toString());
    }

    @Test
    void run_SingleDirectoryWithFilesNoFlags_DisplaysFilesAndDirectories() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path dir = createDirectory("folder");
        createFile("fileA.txt", dir);
        createFile("imageB.jpg", dir);
        createFile("documentC.doc", dir);
        new LsApplication().run(toArgs(STRING_EMPTY, "folder"), System.in, output);
        assertEquals(String.join(STRING_NEWLINE, "documentC.doc", "fileA.txt", "imageB.jpg", STRING_EMPTY), output.toString());
    }

    @Test
    void run_SingleDirectoryWithFilesSortFlag_DisplaysSortedFilesAndDirectories() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path dir = createDirectory("folder");
        createFile("fileA.txt", dir);
        createFile("imageB.jpg", dir);
        createFile("documentC.doc", dir);
        new LsApplication().run(toArgs("X", "folder"), System.in, output);
        assertEquals(String.join(STRING_NEWLINE, "documentC.doc", "imageB.jpg", "fileA.txt", STRING_EMPTY), output.toString());
    }

    @Test
    void run_SingleDirectoryWithFilesAllFlags_DisplaysEmpty() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path dir = createDirectory("folder");
        createFile("fileA.txt", dir);
        createFile("imageB.jpg", dir);
        createFile("documentC.doc", dir);
        new LsApplication().run(toArgs("RdX", "folder"), System.in, output);

        String expected = STRING_EMPTY;
        assertEquals(expected, output.toString());
    }

    @Test
    void run_SingleDirectoryWithEmptyDirectoryNoFlags_DisplaysNestedDirectory() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path dir = createDirectory("folder");
        createDirectory("empty_folder", dir);
        new LsApplication().run(toArgs(STRING_EMPTY, "folder"), System.in, output);

        assertEquals("empty_folder" + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_SingleDirectoryWithEmptyDirectoryAllFlags_DisplaysDirectoriesRecursively() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path dir = createDirectory("folder");
        createDirectory("empty_folder", dir);
        new LsApplication().run(toArgs("RdX", "folder"), System.in, output);
        assertEquals(TEMP + STRING_FILE_SEP + "folder:" + STRING_NEWLINE + "empty_folder" + STRING_NEWLINE + STRING_NEWLINE +
                TEMP + STRING_FILE_SEP + "folder" + STRING_FILE_SEP + "empty_folder:" + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_SingleDirectoryWithNonemptyDirectoriesNoFlags_DisplaysFilesAndDirectories() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path dir = createDirectory("folder");
        createFile("file_in_folder.txt", dir);
        createFile("image_in_folder.jpg", dir);
        createFile("document_in_folder.doc", dir);
        Path dirA = createDirectory("subfolderA", dir);
        createFile("file_in_subfolderA.txt", dirA);
        createFile("image_in_subfolderA.jpg", dirA);
        createFile("document_in_subfolderA.doc", dirA);
        Path dirB = createDirectory("subfolderB", dir);
        createFile("file_in_subfolderB.txt", dirB);
        createFile("image_in_subfolderB.jpg", dirB);
        createFile("document_in_subfolderB.doc", dirB);
        new LsApplication().run(toArgs(STRING_EMPTY, "folder"), System.in, output);
        assertEquals("document_in_folder.doc" + STRING_NEWLINE +
                "file_in_folder.txt" + STRING_NEWLINE + "image_in_folder.jpg" + STRING_NEWLINE + "subfolderA" + STRING_NEWLINE +
                "subfolderB" + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_SingleDirectoryWithNonemptyDirectoriesDirectoryFlag_DisplaysDirectories() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path dir = createDirectory("folder");
        createFile("file_in_folder.txt", dir);
        createFile("image_in_folder.jpg", dir);
        createFile("document_in_folder.doc", dir);
        Path dirA = createDirectory("subfolderA", dir);
        createFile("file_in_subfolderA.txt", dirA);
        createFile("image_in_subfolderA.jpg", dirA);
        createFile("document_in_subfolderA.doc", dirA);
        Path dirB = createDirectory("subfolderB", dir);
        createFile("file_in_subfolderB.txt", dirB);
        createFile("image_in_subfolderB.jpg", dirB);
        createFile("document_in_subfolderB.doc", dirB);
        new LsApplication().run(toArgs("d", "folder"), System.in, output);
        assertEquals("subfolderA" + STRING_NEWLINE + "subfolderB" + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_SingleDirectoryWithNonemptyDirectoriesRecursiveFlag_DisplaysFilesAndDirectoriesRecursively() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path dir = createDirectory("folder");
        createFile("file_in_folder.txt", dir);
        createFile("image_in_folder.jpg", dir);
        createFile("document_in_folder.doc", dir);
        Path dirA = createDirectory("subfolderA", dir);
        createFile("file_in_subfolderA.txt", dirA);
        createFile("image_in_subfolderA.jpg", dirA);
        createFile("document_in_subfolderA.doc", dirA);
        Path dirB = createDirectory("subfolderB", dir);
        createFile("file_in_subfolderB.txt", dirB);
        createFile("image_in_subfolderB.jpg", dirB);
        createFile("document_in_subfolderB.doc", dirB);
        new LsApplication().run(toArgs("R", "folder"), System.in, output);

        String expected = TEMP + STRING_FILE_SEP + "folder:" + STRING_NEWLINE + "document_in_folder.doc" + STRING_NEWLINE + "file_in_folder.txt" +
                STRING_NEWLINE + "image_in_folder.jpg" + STRING_NEWLINE + "subfolderA" + STRING_NEWLINE + "subfolderB" + STRING_NEWLINE +
                STRING_NEWLINE + TEMP + STRING_FILE_SEP + "folder" + STRING_FILE_SEP + "subfolderA:" + STRING_NEWLINE + "document_in_subfolderA.doc" +
                STRING_NEWLINE + "file_in_subfolderA.txt" + STRING_NEWLINE + "image_in_subfolderA.jpg" + STRING_NEWLINE + STRING_NEWLINE +
                TEMP + STRING_FILE_SEP + "folder" + STRING_FILE_SEP + "subfolderB:" + STRING_NEWLINE + "document_in_subfolderB.doc" + STRING_NEWLINE +
                "file_in_subfolderB.txt" + STRING_NEWLINE + "image_in_subfolderB.jpg" + STRING_NEWLINE;

        assertEquals(expected, output.toString());
    }

    @Test
    void run_SingleDirectoryWithNonemptyDirectoriesRecursiveAndDirectoryFlags_DisplaysDirectoriesRecursively() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path dir = createDirectory("folder");
        createFile("file_in_folder.txt", dir);
        createFile("image_in_folder.jpg", dir);
        createFile("document_in_folder.doc", dir);
        Path dirA = createDirectory("subfolderA", dir);
        createFile("file_in_subfolderA.txt", dirA);
        createFile("image_in_subfolderA.jpg", dirA);
        createFile("document_in_subfolderA.doc", dirA);
        Path dirB = createDirectory("subfolderB", dir);
        createFile("file_in_subfolderB.txt", dirB);
        createFile("image_in_subfolderB.jpg", dirB);
        createFile("document_in_subfolderB.doc", dirB);
        new LsApplication().run(toArgs("Rd", "folder"), System.in, output);
        assertEquals(TEMP + STRING_FILE_SEP + "folder:" + STRING_NEWLINE + "subfolderA" + STRING_NEWLINE + "subfolderB" + STRING_NEWLINE +
                STRING_NEWLINE + TEMP + STRING_FILE_SEP + "folder" + STRING_FILE_SEP + "subfolderA:" + STRING_NEWLINE + STRING_NEWLINE +
                TEMP + STRING_FILE_SEP + "folder" + STRING_FILE_SEP + "subfolderB:" + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_SingleDirectoryWithNonemptyDirectoriesAllFlags_DisplaysDirectoriesRecursively() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path dir = createDirectory("folder");
        createFile("file_in_folder.txt", dir);
        createFile("image_in_folder.jpg", dir);
        createFile("document_in_folder.doc", dir);
        Path dirA = createDirectory("subfolderA", dir);
        createFile("file_in_subfolderA.txt", dirA);
        createFile("image_in_subfolderA.jpg", dirA);
        createFile("document_in_subfolderA.doc", dirA);
        Path dirB = createDirectory("subfolderB", dir);
        createFile("file_in_subfolderB.txt", dirB);
        createFile("image_in_subfolderB.jpg", dirB);
        createFile("document_in_subfolderB.doc", dirB);
        new LsApplication().run(toArgs("RdX", "folder"), System.in, output);

        String expected = TEMP + STRING_FILE_SEP + "folder:" + STRING_NEWLINE + "subfolderA" + STRING_NEWLINE + "subfolderB" + STRING_NEWLINE +
                STRING_NEWLINE + TEMP + STRING_FILE_SEP + "folder" + STRING_FILE_SEP + "subfolderA:" + STRING_NEWLINE + STRING_NEWLINE +
                TEMP + STRING_FILE_SEP + "folder" + STRING_FILE_SEP + "subfolderB:" + STRING_NEWLINE;

        assertEquals(expected, output.toString());
    }

    @Test
    void run_MultipleEmptyDirectoriesNoFlags_DisplaysEmpty() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        createDirectory("empty_folderA");
        createDirectory("empty_folderB");
        new LsApplication().run(toArgs(STRING_EMPTY, "empty_folderA", "empty_folderB"), System.in, output);
        assertEquals(TEMP + STRING_FILE_SEP + "empty_folderA:" + STRING_NEWLINE + STRING_NEWLINE +
                TEMP + STRING_FILE_SEP + "empty_folderB:" + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_MultipleEmptyDirectoriesAllFlags_DisplaysEmpty() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        createDirectory("empty_folderA");
        createDirectory("empty_folderB");
        new LsApplication().run(toArgs("RdX", "empty_folderA", "empty_folderB"), System.in, output);
        assertEquals(TEMP + STRING_FILE_SEP + "empty_folderA:" + STRING_NEWLINE + STRING_NEWLINE +
                TEMP + STRING_FILE_SEP + "empty_folderB:" + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_MultipleDirectoriesWithNonemptyDirectoriesNoFlags_DisplaysFilesAndDirectories() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path firstDir = createDirectory("first_folder");
        createFile("file_in_first_folder.txt", firstDir);
        createFile("image_in_first_folder.jpg", firstDir);
        createFile("document_in_first_folder.doc", firstDir);

        Path dirA = createDirectory("subfolderA", firstDir);
        createFile("file_in_subfolderA.txt", dirA);
        createFile("image_in_subfolderA.jpg", dirA);
        createFile("document_in_subfolderA.doc", dirA);

        Path secondDir = createDirectory("second_folder");
        createFile("file_in_second_folder.txt", secondDir);
        createFile("image_in_second_folder.jpg", secondDir);
        createFile("document_in_second_folder.doc", secondDir);

        Path dirB = createDirectory("subfolderB", secondDir);
        createFile("file_in_subfolderB.txt", dirB);
        createFile("image_in_subfolderB.jpg", dirB);
        createFile("document_in_subfolderB.doc", dirB);

        new LsApplication().run(toArgs(STRING_EMPTY, "first_folder", "second_folder"), System.in, output);
        assertEquals(TEMP + STRING_FILE_SEP + "first_folder:" + STRING_NEWLINE + "document_in_first_folder.doc" + STRING_NEWLINE +
                "file_in_first_folder.txt" + STRING_NEWLINE + "image_in_first_folder.jpg" + STRING_NEWLINE + "subfolderA" + STRING_NEWLINE +
                STRING_NEWLINE + TEMP + STRING_FILE_SEP + "second_folder:" + STRING_NEWLINE + "document_in_second_folder.doc" + STRING_NEWLINE +
                "file_in_second_folder.txt" + STRING_NEWLINE + "image_in_second_folder.jpg" + STRING_NEWLINE + "subfolderB" +
                STRING_NEWLINE, output.toString());
    }

    @Test
    void run_MultipleDirectoriesWithNonemptyDirectoriesDirectoryFlag_DisplaysDirectories() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path firstDir = createDirectory("first_folder");
        createFile("file_in_first_folder.txt", firstDir);
        createFile("image_in_first_folder.jpg", firstDir);
        createFile("document_in_first_folder.doc", firstDir);

        Path dirA = createDirectory("subfolderA", firstDir);
        createFile("file_in_subfolderA.txt", dirA);
        createFile("image_in_subfolderA.jpg", dirA);
        createFile("document_in_subfolderA.doc", dirA);

        Path secondDir = createDirectory("second_folder");
        createFile("file_in_second_folder.txt", secondDir);
        createFile("image_in_second_folder.jpg", secondDir);
        createFile("document_in_second_folder.doc", secondDir);

        Path dirB = createDirectory("subfolderB", secondDir);
        createFile("file_in_subfolderB.txt", dirB);
        createFile("image_in_subfolderB.jpg", dirB);
        createFile("document_in_subfolderB.doc", dirB);

        new LsApplication().run(toArgs("d", "first_folder", "second_folder"), System.in, output);
        assertEquals(TEMP + STRING_FILE_SEP + "first_folder:" + STRING_NEWLINE + "subfolderA" + STRING_NEWLINE +
                STRING_NEWLINE + TEMP + STRING_FILE_SEP + "second_folder:" + STRING_NEWLINE + "subfolderB" +
                STRING_NEWLINE, output.toString());
    }

    @Test
    void run_MultipleDirectoriesWithNonemptyDirectoriesRecursiveFlag_DisplaysFilesAndDirectoriesRecursively() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path firstDir = createDirectory("first_folder");
        createFile("file_in_first_folder.txt", firstDir);
        createFile("image_in_first_folder.jpg", firstDir);
        createFile("document_in_first_folder.doc", firstDir);

        Path dirA = createDirectory("subfolderA", firstDir);
        createFile("file_in_subfolderA.txt", dirA);
        createFile("image_in_subfolderA.jpg", dirA);
        createFile("document_in_subfolderA.doc", dirA);

        Path secondDir = createDirectory("second_folder");
        createFile("file_in_second_folder.txt", secondDir);
        createFile("image_in_second_folder.jpg", secondDir);
        createFile("document_in_second_folder.doc", secondDir);

        Path dirB = createDirectory("subfolderB", secondDir);
        createFile("file_in_subfolderB.txt", dirB);
        createFile("image_in_subfolderB.jpg", dirB);
        createFile("document_in_subfolderB.doc", dirB);

        new LsApplication().run(toArgs("R", "first_folder", "second_folder"), System.in, output);
        assertEquals(TEMP + STRING_FILE_SEP + "first_folder:" + STRING_NEWLINE + "document_in_first_folder.doc" + STRING_NEWLINE +
                "file_in_first_folder.txt" + STRING_NEWLINE + "image_in_first_folder.jpg" + STRING_NEWLINE + "subfolderA" + STRING_NEWLINE +
                STRING_NEWLINE + TEMP + STRING_FILE_SEP + "first_folder" + STRING_FILE_SEP + "subfolderA:" + STRING_NEWLINE +
                "document_in_subfolderA.doc" + STRING_NEWLINE + "file_in_subfolderA.txt" + STRING_NEWLINE + "image_in_subfolderA.jpg" +
                STRING_NEWLINE + STRING_NEWLINE + TEMP + STRING_FILE_SEP + "second_folder:" + STRING_NEWLINE + "document_in_second_folder.doc" +
                STRING_NEWLINE + "file_in_second_folder.txt" + STRING_NEWLINE + "image_in_second_folder.jpg" + STRING_NEWLINE + "subfolderB" +
                STRING_NEWLINE + STRING_NEWLINE + TEMP + STRING_FILE_SEP + "second_folder" + STRING_FILE_SEP + "subfolderB:" + STRING_NEWLINE +
                "document_in_subfolderB.doc" + STRING_NEWLINE + "file_in_subfolderB.txt" + STRING_NEWLINE + "image_in_subfolderB.jpg" +
                STRING_NEWLINE, output.toString());
    }

    @Test
    void run_MultipleDirectoriesWithNonemptyDirectoriesRecursiveAndDirectoryFlags_DisplaysDirectoriesRecursively() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path firstDir = createDirectory("first_folder");
        createFile("file_in_first_folder.txt", firstDir);
        createFile("image_in_first_folder.jpg", firstDir);
        createFile("document_in_first_folder.doc", firstDir);

        Path dirA = createDirectory("subfolderA", firstDir);
        createFile("file_in_subfolderA.txt", dirA);
        createFile("image_in_subfolderA.jpg", dirA);
        createFile("document_in_subfolderA.doc", dirA);

        Path secondDir = createDirectory("second_folder");
        createFile("file_in_second_folder.txt", secondDir);
        createFile("image_in_second_folder.jpg", secondDir);
        createFile("document_in_second_folder.doc", secondDir);

        Path dirB = createDirectory("subfolderB", secondDir);
        createFile("file_in_subfolderB.txt", dirB);
        createFile("image_in_subfolderB.jpg", dirB);
        createFile("document_in_subfolderB.doc", dirB);

        new LsApplication().run(toArgs("Rd", "first_folder", "second_folder"), System.in, output);
        assertEquals(TEMP + STRING_FILE_SEP + "first_folder:" + STRING_NEWLINE + "subfolderA" + STRING_NEWLINE +
                STRING_NEWLINE + TEMP + STRING_FILE_SEP + "first_folder" + STRING_FILE_SEP + "subfolderA:" +
                STRING_NEWLINE + STRING_NEWLINE + TEMP + STRING_FILE_SEP + "second_folder:" + STRING_NEWLINE + "subfolderB" +
                STRING_NEWLINE + STRING_NEWLINE + TEMP + STRING_FILE_SEP + "second_folder" + STRING_FILE_SEP + "subfolderB:" +
                STRING_NEWLINE, output.toString());
    }

    @Test
    void run_MultipleDirectoriesWithNonemptyDirectoriesSortFlag_DisplaysSortedFilesAndDirectories() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path firstDir = createDirectory("first_folder");
        createFile("file_in_first_folder.txt", firstDir);
        createFile("image_in_first_folder.jpg", firstDir);
        createFile("document_in_first_folder.doc", firstDir);

        Path dirA = createDirectory("subfolderA", firstDir);
        createFile("file_in_subfolderA.txt", dirA);
        createFile("image_in_subfolderA.jpg", dirA);
        createFile("document_in_subfolderA.doc", dirA);

        Path secondDir = createDirectory("second_folder");
        createFile("file_in_second_folder.txt", secondDir);
        createFile("image_in_second_folder.jpg", secondDir);
        createFile("document_in_second_folder.doc", secondDir);

        Path dirB = createDirectory("subfolderB", secondDir);
        createFile("file_in_subfolderB.txt", dirB);
        createFile("image_in_subfolderB.jpg", dirB);
        createFile("document_in_subfolderB.doc", dirB);

        new LsApplication().run(toArgs("X", "first_folder", "second_folder"), System.in, output);
        assertEquals(TEMP + STRING_FILE_SEP + "first_folder:" + STRING_NEWLINE + "subfolderA" + STRING_NEWLINE +
                "document_in_first_folder.doc" + STRING_NEWLINE + "image_in_first_folder.jpg" + STRING_NEWLINE +
                "file_in_first_folder.txt" + STRING_NEWLINE + STRING_NEWLINE + TEMP + STRING_FILE_SEP + "second_folder:" +
                STRING_NEWLINE + "subfolderB" + STRING_NEWLINE + "document_in_second_folder.doc" + STRING_NEWLINE +
                "image_in_second_folder.jpg" + STRING_NEWLINE + "file_in_second_folder.txt" + STRING_NEWLINE, output.toString());
    }

    @Test
    void run_MultipleDirectoriesWithNonemptyDirectoriesSortFlag_DisplaysDirectoriesRecursively() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path firstDir = createDirectory("first_folder");
        createFile("file_in_first_folder.txt", firstDir);
        createFile("image_in_first_folder.jpg", firstDir);
        createFile("document_in_first_folder.doc", firstDir);

        Path dirA = createDirectory("subfolderA", firstDir);
        createFile("file_in_subfolderA.txt", dirA);
        createFile("image_in_subfolderA.jpg", dirA);
        createFile("document_in_subfolderA.doc", dirA);

        Path secondDir = createDirectory("second_folder");
        createFile("file_in_second_folder.txt", secondDir);
        createFile("image_in_second_folder.jpg", secondDir);
        createFile("document_in_second_folder.doc", secondDir);

        Path dirB = createDirectory("subfolderB", secondDir);
        createFile("file_in_subfolderB.txt", dirB);
        createFile("image_in_subfolderB.jpg", dirB);
        createFile("document_in_subfolderB.doc", dirB);

        new LsApplication().run(toArgs("RdX", "first_folder", "second_folder"), System.in, output);
        assertEquals(TEMP + STRING_FILE_SEP + "first_folder:" + STRING_NEWLINE + "subfolderA" + STRING_NEWLINE +
                STRING_NEWLINE + TEMP + STRING_FILE_SEP + "first_folder" + STRING_FILE_SEP + "subfolderA:" +
                STRING_NEWLINE + STRING_NEWLINE + TEMP + STRING_FILE_SEP + "second_folder:" + STRING_NEWLINE + "subfolderB" +
                STRING_NEWLINE + STRING_NEWLINE + TEMP + STRING_FILE_SEP + "second_folder" + STRING_FILE_SEP + "subfolderB:" +
                STRING_NEWLINE, output.toString());
    }

    @Test
    void run_AbsolutePath_DisplaysDirectoriesRecursively() throws IOException, AbstractApplicationException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Path firstDir = createDirectory("first_folder");
        createFile("file_in_first_folder.txt", firstDir);
        createFile("image_in_first_folder.jpg", firstDir);
        createFile("document_in_first_folder.doc", firstDir);

        Path dirA = createDirectory("subfolderA", firstDir);
        createFile("file_in_subfolderA.txt", dirA);
        createFile("image_in_subfolderA.jpg", dirA);
        createFile("document_in_subfolderA.doc", dirA);

        Path secondDir = createDirectory("second_folder");
        createFile("file_in_second_folder.txt", secondDir);
        createFile("image_in_second_folder.jpg", secondDir);
        createFile("document_in_second_folder.doc", secondDir);

        Path dirB = createDirectory("subfolderB", secondDir);
        createFile("file_in_subfolderB.txt", dirB);
        createFile("image_in_subfolderB.jpg", dirB);
        createFile("document_in_subfolderB.doc", dirB);

        String prefix = TEMP_PATH.toString() + STRING_FILE_SEP;

        new LsApplication().run(new String[]{"-RdX",
                prefix + "first_folder",
                prefix + "second_folder"}, System.in, output);
        assertEquals(prefix + "first_folder:" + STRING_NEWLINE + "subfolderA" + STRING_NEWLINE +
                STRING_NEWLINE + prefix + "first_folder" + STRING_FILE_SEP + "subfolderA:" +
                STRING_NEWLINE + STRING_NEWLINE + prefix + "second_folder:" + STRING_NEWLINE + "subfolderB" +
                STRING_NEWLINE + STRING_NEWLINE + prefix + "second_folder" + STRING_FILE_SEP + "subfolderB:" +
                STRING_NEWLINE, output.toString());
    }

    @Test
    void run_NonexistentDirectory_DisplaysErrorMessage() throws AbstractApplicationException {
        captureErr();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new LsApplication().run(toArgs(STRING_EMPTY, "nonexistent_folder"), System.in, output);

        String expected = new LsException(new InvalidDirectoryException(TEMP + STRING_FILE_SEP + "nonexistent_folder", ERR_FILE_NOT_FOUND).getMessage()).getMessage();

        assertEquals(STRING_EMPTY, output.toString());

        assertEquals(expected + STRING_NEWLINE, getErrOutput());
    }

    @Test
    void run_UnknownFlag_ThrowsException() {
        assertThrows(LsException.class, () -> new LsApplication().run(toArgs("q", STRING_EMPTY), System.in, System.out));
    }
}
