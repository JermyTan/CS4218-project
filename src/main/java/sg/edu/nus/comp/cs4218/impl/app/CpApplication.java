package sg.edu.nus.comp.cs4218.impl.app;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;

import sg.edu.nus.comp.cs4218.app.CpInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CpException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.impl.parser.CpArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_FILES;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_MISSING_ARG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_NOT_DIR;

public class CpApplication implements CpInterface {

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        CpArgsParser parser = new CpArgsParser();

        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new CpException(e.getMessage(), e);
        }

        Boolean isRecursive = parser.isRecursive();
        List<String> names = parser.getFileOrFolderNames();

        try {
            if (names.size() < 2) {
                throw new InvalidArgsException(ERR_MISSING_ARG);
            } else if (names.size() == 2) {
                String firstName = names.get(0);
                String secondName = names.get(1);
                Path firstPath = IOUtils.resolveAbsoluteFilePath(firstName);
                Path secondPath = IOUtils.resolveAbsoluteFilePath(secondName);
                if (Files.isDirectory(firstPath) || Files.isDirectory(secondPath)) {
                    cpFilesToFolder(isRecursive, secondName, firstName);
                } else {
                    cpSrcFileToDestFile(isRecursive, firstName, secondName);
                }
            } else {
                String folder = names.get(names.size() - 1);
                String[] fileNames = names.subList(0, names.size() - 1).toArray(String[]::new);
                cpFilesToFolder(isRecursive, folder, fileNames);
            }
        } catch (CpException e) {
            throw e;
        } catch (Exception e) {
            throw new CpException(e.getMessage(), e);
        }
    }

    @Override
    public String cpSrcFileToDestFile(Boolean isRecursive, String srcFile, String destFile) throws Exception {
        try {
            if (srcFile == null || srcFile.length() == 0) {
                throw new InvalidDirectoryException(srcFile, ERR_INVALID_ARGS);
            }
            if (srcFile.equals(destFile)) {
                throw new InvalidArgsException(ERR_INVALID_ARGS);
            }
            Path srcFilePath = IOUtils.resolveAbsoluteFilePath(srcFile);
            Path destFilePath = IOUtils.resolveAbsoluteFilePath(destFile);
            if (Files.notExists(srcFilePath)) {
                throw new InvalidDirectoryException(srcFile, ERR_FILE_NOT_FOUND);
            }
            if (!Files.isRegularFile(srcFilePath)) {
                throw new InvalidDirectoryException(srcFile, ERR_INVALID_FILES);
            }
            Files.copy(srcFilePath, destFilePath, StandardCopyOption.REPLACE_EXISTING);
            return null;
        } catch (Exception e) {
            throw new CpException(e.getMessage(), e);
        }
    }

    @Override
    public String cpFilesToFolder(Boolean isRecursive, String destFolder, String... fileNames) throws Exception { //NOPMD
        try {
            Path destFolderPath = IOUtils.resolveAbsoluteFilePath(destFolder);
            if (Files.isRegularFile(destFolderPath)) {
                throw new InvalidDirectoryException(destFolder, ERR_IS_NOT_DIR);
            }
            boolean destExisted = false;
            if (Files.notExists(destFolderPath)) {
                if (isRecursive) {
                    Files.createDirectories(destFolderPath);
                } else {
                    throw new InvalidDirectoryException(destFolder, ERR_IS_NOT_DIR);
                }
            } else {
                destExisted = true;
            }

            if (fileNames == null || fileNames.length == 0) {
                throw new InvalidArgsException(ERR_INVALID_FILES);
            }

            Exception toBeThrown = null;
            for (String fileName : fileNames) {
                if (fileName == null || fileName.length() == 0) {
                    throw new InvalidDirectoryException(fileName, ERR_INVALID_ARGS);
                }
                Path srcFilePath = IOUtils.resolveAbsoluteFilePath(fileName);
                if (Files.notExists(srcFilePath)) {
                    throw new InvalidDirectoryException(fileName, ERR_FILE_NOT_FOUND);
                }
                if (Files.isDirectory(srcFilePath) && !isRecursive) {
                    toBeThrown = new InvalidDirectoryException(fileName, ERR_IS_DIR);
                }
                Path destFilePath = destExisted ? destFolderPath.resolve(srcFilePath.getFileName()) : destFolderPath;
                if (Files.isRegularFile(srcFilePath) || isRecursive) {
                    Files.copy(srcFilePath, destFilePath, StandardCopyOption.REPLACE_EXISTING);
                }
                if (isRecursive) {
                    Files.walkFileTree(srcFilePath, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                            new SimpleFileVisitor<>() {
                                @Override
                                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                                        throws IOException {
                                    Path targetDir = destFilePath.resolve(srcFilePath.relativize(dir));
                                    try {
                                        Files.copy(dir, targetDir, StandardCopyOption.REPLACE_EXISTING);
                                    } catch (FileAlreadyExistsException e) {
                                        if (!Files.isDirectory(targetDir)) {
                                            throw e;
                                        }
                                    }
                                    return FileVisitResult.CONTINUE;
                                }

                                @Override
                                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                        throws IOException {
                                    Files.copy(file, destFilePath.resolve(srcFilePath.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                                    return FileVisitResult.CONTINUE;
                                }
                            });
                }
            }
            if (toBeThrown != null) {
                throw toBeThrown;
            }
            return null;
        } catch (Exception e) {
            throw new CpException(e.getMessage(), e);
        }
    }
}
