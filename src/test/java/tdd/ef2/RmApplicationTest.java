package tdd.ef2;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_FLAG;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.impl.app.RmApplication;

@Disabled
public class RmApplicationTest {
    public static final String TEMP = "temp";
    public static final Path TEMP_PATH = Path.of(EnvironmentUtil.currentDirectory, TEMP);
    public static Deque<Path> files = new ArrayDeque<>();

    @BeforeAll
    static void createTemp() throws IOException {
        Files.createDirectory(TEMP_PATH);
    }

    @AfterAll
    static void deleteTemp() throws IOException {
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
            args.add(Path.of(TEMP, file).toString());
        }
        return args.toArray(new String[0]);
    }

    @Test
    void run_SingleFile_DeletesFile() throws IOException, AbstractApplicationException {
        Path fileA = createFile("a.txt");
        Path fileB = createFile("bobby");
        new RmApplication().run(toArgs(STRING_EMPTY, "a.txt"), System.in, System.out);
        assertTrue(Files.notExists(fileA));
        assertTrue(Files.exists(fileB));
    }

    @Test
    void run_SpaceInName_DeletesFile() throws IOException, AbstractApplicationException {
        Path fileC = createFile("c   c");
        new RmApplication().run(toArgs(STRING_EMPTY, "c   c"), System.in, System.out);
        assertTrue(Files.notExists(fileC));
    }

    @Test
    void run_MultipleFiles_DeletesFiles() throws IOException, AbstractApplicationException {
        Path fileD = createFile("d.txt");
        Path fileE = createFile("eerie");
        new RmApplication().run(toArgs(STRING_EMPTY, "d.txt", "eerie"), System.in, System.out);
        assertTrue(Files.notExists(fileD));
        assertTrue(Files.notExists(fileE));
    }

    @Test
    void run_EmptyDirectory_DeletesDirectory() throws IOException, AbstractApplicationException {
        Path folder = createDirectory("folder");
        new RmApplication().run(toArgs("d", "folder"), System.in, System.out);
        assertTrue(Files.notExists(folder));
    }

    @Test
    void run_MultipleFilesEmptyDirectories_DeletesAll() throws IOException, AbstractApplicationException {
        Path fileG = createFile("g.txt");
        Path fileH = createFile("high");
        Path directoryA = createDirectory("directoryA");
        Path directoryB = createDirectory("directoryB");
        new RmApplication().run(toArgs("d", "g.txt", "high", "directoryA", "directoryB"), System.in, System.out);
        assertTrue(Files.notExists(fileG));
        assertTrue(Files.notExists(fileH));
        assertTrue(Files.notExists(directoryA));
        assertTrue(Files.notExists(directoryB));
    }

    @Test
    void run_DirectoryWithFiles_DeletesDirectory() throws IOException, AbstractApplicationException {
        Path directory = createDirectory("directory");
        createFile("dwf.txt", directory);
        createFile("dwf2.txt", directory);
        new RmApplication().run(toArgs("r", "directory"), System.in, System.out);
        assertTrue(Files.notExists(directory));
    }

    @Test
    void run_DirectoryInDirectory_DeletesDirectory() throws IOException, AbstractApplicationException {
        Path directoryC = createDirectory("directoryC");
        createFile("did.txt", directoryC);
        Path directory = createDirectory("directoryDid", directoryC);
        Path inner = createDirectory("directoryDid", directory);
        createFile("did.txt", inner);
        createFile("did2.txt", inner);
        new RmApplication().run(toArgs("r", "directoryC"), System.in, System.out);
        assertTrue(Files.notExists(directoryC));
    }

    @Test
    void run_MultipleFilesDirectories_DeletesAll() throws IOException, AbstractApplicationException {
        Path directoryD = createDirectory("directoryD");
        createFile("mfd.txt", directoryD);
        Path directory = createDirectory("directoryMfd", directoryD);
        Path inner = createDirectory("directoryMfd", directory);
        createFile("mfd.txt", inner);
        createFile("mfd2.txt", inner);
        Path empty = createDirectory("empty");
        Path fileI = createFile("ii");
        Path fileJ = createFile("jar");
        new RmApplication().run(toArgs("r", "directoryD", "empty", "ii", "jar"), System.in, System.out);
        assertTrue(Files.notExists(directoryD));
        assertTrue(Files.notExists(empty));
        assertTrue(Files.notExists(fileI));
        assertTrue(Files.notExists(fileJ));
    }

    @Test
    void run_AbsolutePath_DeletesDirectory() throws IOException, AbstractApplicationException {
        Path directory = createDirectory("directoryAbs");
        createDirectory("innerAbs", directory);
        new RmApplication().run(new String[]{"-r",
                TEMP_PATH.resolve("directoryAbs").toString()}, System.in, System.out);
        assertTrue(Files.notExists(directory));
    }

    @Test
    void run_ZeroArguments_ThrowsException() {
        assertThrows(RmException.class, () -> new RmApplication().run(toArgs(STRING_EMPTY), System.in, System.out));
    }

    @Test
    void run_FlagOnly_ThrowsException() {
        assertThrows(RmException.class, () -> new RmApplication().run(toArgs("d"), System.in, System.out));
    }

    @Test
    void run_UnknownFlag_ThrowsException() throws IOException {
        Path fileK = createFile("kick");
        assertThrows(RmException.class, () -> new RmApplication().run(toArgs("x", "kick"), System.in, System.out));
        assertTrue(Files.exists(fileK));
    }

    @Test
    void run_NonexistentFile_ThrowsException() {
        assertThrows(RmException.class, () -> new RmApplication().run(toArgs(STRING_EMPTY, "not exist"), System.in, System.out));
    }

    @Test
    void run_DirectoryWithoutFlag_ThrowsException() throws IOException {
        createDirectory("directoryF");
        assertThrows(RmException.class, () -> new RmApplication().run(toArgs(STRING_EMPTY, "directoryF"), System.in, System.out));
    }

    @Test
    void run_NonemptyDirectoryWithDFlag_ThrowsException() throws IOException {
        Path directory = createDirectory("directoryG");
        createFile("a.txt", directory);
        assertThrows(RmException.class, () -> new RmApplication().run(toArgs("d", "directoryG"), System.in, System.out));
    }
}
