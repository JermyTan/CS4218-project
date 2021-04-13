package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_DIR_NOT_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_FILES;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_FILE_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import sg.edu.nus.comp.cs4218.app.RmInterface;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.parser.RmArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.CollectionUtils;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

public class RmApplication implements RmInterface {
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        RmArgsParser parser = new RmArgsParser();

        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new RmException(e.getMessage(), e);
        }

        boolean isRecursive = parser.isRecursive();
        boolean isRemoveEmptyDir = parser.isRemoveEmptyDir();
        String[] fileNames = parser.getFileNames().toArray(String[]::new);

        try {
            remove(isRemoveEmptyDir, isRecursive, fileNames);
        } catch (RmException e) {
            throw e;
        } catch (Exception e) {
            throw new RmException(e.getMessage(), e);
        }

    }

    @Override
    public void remove(Boolean isEmptyFolder, Boolean isRecursive, String... fileNames) throws RmException {
        if (CollectionUtils.isAnyNull(isEmptyFolder, isRecursive)) {
            throw new RmException(ERR_NULL_ARGS);
        }

        if (fileNames == null || fileNames.length == 0) {
            throw new RmException(ERR_NO_FILE_ARGS);
        }

        if (CollectionUtils.isAnyNull((Object[]) fileNames)) {
            throw new RmException(ERR_INVALID_FILES);
        }

        for (String fileName : fileNames) {
            try {
                removeFile(isEmptyFolder, isRecursive, fileName);
            } catch (Exception e) {
                throw new RmException(e.getMessage(), e);
            }
        }
    }

    private void removeFile(boolean isEmptyFolder, boolean isRecursive, String fileName) throws Exception {
        Path filePath = IOUtils.resolveAbsoluteFilePath(fileName);
        if (Files.notExists(filePath)) {
            throw new InvalidDirectoryException(fileName, ERR_FILE_NOT_FOUND);
        }

        if (Files.isDirectory(filePath)) {
            if (isRecursive) {
                Files.walk(filePath)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } else if (isEmptyFolder) {
                if (isEmptyDir(filePath)) {
                    Files.delete(filePath);
                } else {
                    throw new InvalidDirectoryException(fileName, ERR_DIR_NOT_EMPTY);
                }
            } else {
                throw new InvalidDirectoryException(fileName, ERR_IS_DIR);
            }
        } else {
            Files.delete(filePath);
        }
    }

    // Reference: https://www.baeldung.com/java-check-empty-directory
    private boolean isEmptyDir(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (Stream<Path> entries = Files.list(path)) {
                return !entries.findFirst().isPresent();
            }
        }

        return false;
    }
}
