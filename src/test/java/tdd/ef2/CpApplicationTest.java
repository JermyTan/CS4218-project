package tdd.ef2;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_FLAG;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CpException;
import sg.edu.nus.comp.cs4218.impl.app.CpApplication;

public class CpApplicationTest {
    public static final String TEMP = "temp-cp";
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

    private Path createDirectory(String folder) throws IOException {
        return createDirectory(folder, TEMP_PATH);
    }

    private Path createFile(String name, Path inPath) throws IOException {
        Path path = inPath.resolve(name);
        Files.createFile(path);
        return path;
    }

    private Path createDirectory(String folder, Path inPath) throws IOException {
        Path path = inPath.resolve(folder);
        Files.createDirectory(path);
        return path;
    }

    private void writeToFile(Path path, String content) throws IOException {
        Files.write(path, content.getBytes());
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
    void run_EmptyFileToNonemptyFile_OverwritesDestWithEmpty() throws IOException, AbstractApplicationException {
        String srcName = "src_file.txt";
        String destName = "dest_file.txt";
        createFile(srcName);
        Path destFile = createFile(destName);
        String destContent = "This file is not empty.";
        writeToFile(destFile, destContent);
        new CpApplication().run(toArgs(STRING_EMPTY, srcName, destName), System.in, System.out);
        assertArrayEquals(STRING_EMPTY.getBytes(), Files.readAllBytes(destFile));
    }

    @Test
    void run_NonemptyFileToEmptyFile_CopiesContentToDest() throws IOException, AbstractApplicationException {
        String srcName = "src_file.txt";
        String destName = "dest_file.txt";
        Path srcFile = createFile(srcName);
        Path destFile = createFile(destName);
        String srcContent = "This file is not empty.";
        writeToFile(srcFile, srcContent);
        new CpApplication().run(toArgs(STRING_EMPTY, srcName, destName), System.in, System.out);
        assertArrayEquals(srcContent.getBytes(), Files.readAllBytes(destFile));
    }

    @Test
    void run_NonemptyFileToNonemptyFile_OverwritesDest() throws IOException, AbstractApplicationException {
        String srcName = "src_file.txt";
        String destName = "dest_file.txt";
        Path srcFile = createFile(srcName);
        Path destFile = createFile(destName);
        String srcContent = "This is the source file.";
        String destContent = "This is the destination file.";
        writeToFile(srcFile, srcContent);
        writeToFile(destFile, destContent);
        new CpApplication().run(toArgs(STRING_EMPTY, srcName, destName), System.in, System.out);
        assertArrayEquals(srcContent.getBytes(), Files.readAllBytes(destFile));
    }

    @Test
    void run_NonemptyFileToSameFile_ThrowsException() throws IOException {
        String srcName = "src_file.txt";
        Path srcFile = createFile(srcName);
        String srcContent = "This is the same file.";
        writeToFile(srcFile, srcContent);
        assertThrows(CpException.class, () -> new CpApplication().run(toArgs(STRING_EMPTY, srcName, srcName), System.in, System.out));
    }

    @Test
    void run_DirectoryToFile_ThrowsException() throws IOException {
        String srcName = "src_dir";
        String destName = "dest_file.txt";
        createDirectory(srcName);
        Path destFile = createFile(destName);
        String destContent = "This is the destination file.";
        writeToFile(destFile, destContent);
        assertThrows(CpException.class, () -> new CpApplication().run(toArgs(STRING_EMPTY, srcName, destName), System.in, System.out));
    }

    @Test
    void run_NonexistentFileToFile_ThrowsException() throws IOException {
        String srcName = "src_file.txt";
        String destName = "dest_file.txt";
        Path destFile = createFile(destName);
        String destContent = "This is the destination file.";
        writeToFile(destFile, destContent);
        assertThrows(CpException.class, () -> new CpApplication().run(toArgs(STRING_EMPTY, srcName, destName), System.in, System.out));
    }

    @Test
    void run_FileToNonexistentFile_CreatesNewDestFile() throws IOException, AbstractApplicationException {
        String srcName = "src_file.txt";
        String destName = "dest_file.txt";
        Path srcFile = createFile(srcName);
        String srcContent = "This is the source file.";
        writeToFile(srcFile, srcContent);
        new CpApplication().run(toArgs(STRING_EMPTY, srcName, destName), System.in, System.out);
        Path destFile = TEMP_PATH.resolve(destName);
        assertTrue(Files.exists(destFile));
        assertArrayEquals(srcContent.getBytes(), Files.readAllBytes(destFile));
    }

    @Test
    void run_DirectoryToNonexistentFile_CreatesDirectoryWithDestNameWithSrcContent() throws IOException, AbstractApplicationException {
        String srcName = "src_dir";
        String fileInSrcDirName = "file_in_src_dir.txt";
        String destName = "dest_file.txt";
        Path srcDir = createDirectory(srcName);
        Path fileInSrcDir = createFile(fileInSrcDirName, srcDir);
        String srcContent = "This is the file in the source directory.";
        writeToFile(fileInSrcDir, srcContent);
        new CpApplication().run(toArgs("r", srcName, destName), System.in, System.out);
        Path destFile = TEMP_PATH.resolve(destName);
        assertTrue(Files.exists(destFile));
        assertTrue(Files.isDirectory(destFile));
        Path fileInDestDir = destFile.resolve(fileInSrcDirName);
        assertTrue(Files.exists(fileInDestDir));
        assertArrayEquals(srcContent.getBytes(), Files.readAllBytes(fileInDestDir));
    }

    @Test
    void run_FileToEmptyDirectory_CopiesToDirectory() throws IOException, AbstractApplicationException {
        String srcName = "src_file.txt";
        String destName = "dest_dir";
        Path srcFile = createFile(srcName);
        String srcContent = "This is the source file.";
        writeToFile(srcFile, srcContent);
        Path destDir = createDirectory(destName);
        new CpApplication().run(toArgs(STRING_EMPTY, srcName, destName), System.in, System.out);
        Path destFile = destDir.resolve(srcName);
        assertTrue(Files.exists(destFile));
        assertArrayEquals(srcContent.getBytes(), Files.readAllBytes(destFile));
    }

    @Test
    void run_FileToNonemptyDirectory_CopiesToDirectory() throws IOException, AbstractApplicationException {
        String srcName = "src_file.txt";
        Path srcFile = createFile(srcName);
        String srcContent = "This is the source file.";
        writeToFile(srcFile, srcContent);
        String destName = "dest_dir";
        Path destDir = createDirectory(destName);
        String destOrigName = "dest_orig_file.txt";
        Path destOrigFile = createFile(destOrigName, destDir);
        new CpApplication().run(toArgs(STRING_EMPTY, srcName, destName), System.in, System.out);
        assertTrue(Files.exists(destOrigFile));
        Path destFile = destDir.resolve(srcName);
        assertTrue(Files.exists(destFile));
        assertArrayEquals(srcContent.getBytes(), Files.readAllBytes(destFile));
    }

    @Test
    void run_DirectoryToDirectoryWithFlag_CopiesSrcDirectoryToDestDirectory() throws IOException, AbstractApplicationException {
        String srcName = "src_dir";
        String destName = "dest_dir";
        createDirectory(srcName);
        Path destDir = createDirectory(destName);
        new CpApplication().run(toArgs("r", srcName, destName), System.in, System.out);
        Path destFile = destDir.resolve(srcName);
        assertTrue(Files.exists(destFile));
        assertTrue(Files.isDirectory(destFile));
    }

    @Test
    void run_DirectoryToDirectoryWithoutFlag_DoesNothing() throws IOException, AbstractApplicationException {
        String srcName = "src_dir";
        String destName = "dest_dir";
        createDirectory(srcName);
        Path destDir = createDirectory(destName);
        new CpApplication().run(toArgs(STRING_EMPTY, srcName, destName), System.in, System.out);
        Path destFile = destDir.resolve(srcName);
        assertTrue(Files.notExists(destFile));
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    void run_DirectoryToSameDirectoryWithFlag_ThrowsException() throws IOException {
        String sameName = "same_dir";
        createDirectory(sameName);
        assertThrows(CpException.class, () -> new CpApplication().run(toArgs("r", sameName, sameName), System.in, System.out));
    }

    @Test
    void run_NonexistentDirectoryToDirectoryWithFlag_ThrowsException() throws IOException {
        String nonexistentSrcName = "nonexistent_dir";
        String destName = "dest_dir";
        createDirectory(destName);
        assertThrows(CpException.class, () -> new CpApplication().run(toArgs("r", nonexistentSrcName, destName), System.in, System.out));
    }

    @Test
    void run_FileToNonexistentDirectory_CreatesFileWithDestNameWithSrcContent() throws IOException, AbstractApplicationException {
        String srcName = "src_file.txt";
        Path srcFile = createFile(srcName);
        String srcContent = "This is the source file.";
        writeToFile(srcFile, srcContent);
        String destName = "dest_dir";
        new CpApplication().run(toArgs(STRING_EMPTY, srcName, destName), System.in, System.out);
        Path destFile = TEMP_PATH.resolve(destName);
        assertTrue(Files.exists(destFile));
        assertArrayEquals(srcContent.getBytes(), Files.readAllBytes(destFile));
    }

    @Test
    void run_DirectoryToNonexistentDirectoryWithoutFlag_ThrowsException() throws IOException {
        String srcName = "src_dir";
        createDirectory(srcName);
        String nonexistentDestName = "nonexistent_dir";
        assertThrows(CpException.class, () -> new CpApplication().run(toArgs(STRING_EMPTY, srcName, nonexistentDestName), System.in, System.out));
    }

    @Test
    void run_DirectoryToNonexistentDirectoryWithFlag_CreatesDirectoryWithSrcContent() throws IOException, AbstractApplicationException {
        String srcName = "src_dir";
        Path srcDir = createDirectory(srcName);
        String fileInSrcDirName = "file_in_src_dir.txt";
        Path fileInSrcDir = createFile(fileInSrcDirName, srcDir);
        String srcContent = "This is the file in the source directory.";
        writeToFile(fileInSrcDir, srcContent);
        String destName = "dest_dir";
        new CpApplication().run(toArgs("r", srcName, destName), System.in, System.out);
        Path destDir = TEMP_PATH.resolve(destName);
        assertTrue(Files.exists(destDir));
        assertTrue(Files.isDirectory(destDir));
        Path fileInDestDir = TEMP_PATH.resolve(destName).resolve(fileInSrcDirName);
        assertTrue(Files.exists(fileInDestDir));
        assertArrayEquals(srcContent.getBytes(), Files.readAllBytes(fileInDestDir));
    }

    @Test
    void run_MissingSrcAndDestArguments_ThrowsException() {
        assertThrows(CpException.class, () -> new CpApplication().run(toArgs(STRING_EMPTY), System.in, System.out));
    }

    @Test
    void run_FileToMissingDestArgument_ThrowsException() throws IOException {
        String srcName = "src_file.txt";
        createFile(srcName);
        assertThrows(CpException.class, () -> new CpApplication().run(toArgs(STRING_EMPTY, srcName), System.in, System.out));
    }

    @Test
    void run_DirectoryToMissingDestArgument_ThrowsException() throws IOException {
        String srcName = "src_dir";
        createDirectory(srcName);
        assertThrows(CpException.class, () -> new CpApplication().run(toArgs(STRING_EMPTY, srcName), System.in, System.out));
    }

    @Test
    void run_MultipleFilesToDirectory_CopiesToDirectory() throws IOException, AbstractApplicationException {
        String srcAName = "srcA_file.txt";
        String srcBName = "srcB_file.txt";
        String destName = "dest_dir";
        createFile(srcAName);
        createFile(srcBName);
        Path destDir = createDirectory(destName);
        new CpApplication().run(toArgs(STRING_EMPTY, srcAName, srcBName, destName), System.in, System.out);
        Path destAFile = destDir.resolve(srcAName);
        Path destBFile = destDir.resolve(srcBName);
        assertTrue(Files.exists(destAFile));
        assertTrue(Files.exists(destBFile));
    }

    @Test
    void run_MultipleFilesToFile_ThrowsException() throws IOException {
        String srcAName = "srcA_file.txt";
        String srcBName = "srcB_file.txt";
        String destName = "dest_file.txt";
        createFile(srcAName);
        createFile(srcBName);
        createFile(destName);
        assertThrows(CpException.class, () -> new CpApplication().run(toArgs(STRING_EMPTY, srcAName, srcBName, destName), System.in, System.out));
    }

    @Test
    void run_MultipleDirectoriesToDirectory_CopiesToDirectory() throws IOException, AbstractApplicationException {
        String srcAName = "srcA_dir";
        String srcBName = "srcB_dir";
        String destName = "dest_dir";
        createDirectory(srcAName);
        createDirectory(srcBName);
        Path destDir = createDirectory(destName);
        new CpApplication().run(toArgs("r", srcAName, srcBName, destName), System.in, System.out);
        Path destADir = destDir.resolve(srcAName);
        Path destBDir = destDir.resolve(srcBName);
        assertTrue(Files.exists(destADir));
        assertTrue(Files.exists(destBDir));
        assertTrue(Files.isDirectory(destADir));
        assertTrue(Files.isDirectory(destBDir));
    }

    @Test
    void run_MultipleDirectoriesToFile_ThrowsException() throws IOException {
        String srcAName = "srcA_dir";
        String srcBName = "srcB_dir";
        String destName = "dest_file.txt";
        createDirectory(srcAName);
        createDirectory(srcBName);
        createFile(destName);
        assertThrows(CpException.class, () -> new CpApplication().run(toArgs("r", srcAName, srcBName, destName), System.in, System.out));
    }

    @Test
    void run_MultipleFilesAndDirectoriesToDirectory_CopiesToDirectory() throws IOException, AbstractApplicationException {
        String srcAName = "srcA_file.txt";
        String srcBName = "srcB_dir";
        String destName = "dest_dir";
        createFile(srcAName);
        createDirectory(srcBName);
        Path destDir = createDirectory(destName);
        new CpApplication().run(toArgs("r", srcAName, srcBName, destName), System.in, System.out);
        Path destAFile = destDir.resolve(srcAName);
        Path destBFile = destDir.resolve(srcBName);
        assertTrue(Files.exists(destAFile));
        assertTrue(Files.exists(destBFile));
    }

    @Test
    void run_MultipleFilesAndDirectoriesToFile_ThrowsException() throws IOException {
        String srcAName = "srcA_file.txt";
        String srcBName = "srcB_dir";
        String destName = "dest_file.txt";
        createFile(srcAName);
        createDirectory(srcBName);
        createFile(destName);
        assertThrows(CpException.class, () -> new CpApplication().run(toArgs(STRING_EMPTY, srcAName, srcBName, destName), System.in, System.out));
    }
}
