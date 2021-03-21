package sg.edu.nus.comp.cs4218.impl.app;

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
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.RmException;
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
        } catch (Exception e) {
            throw new RmException(e.getMessage(), e);
        }

    }

    @Override
    public void remove(Boolean isEmptyFolder, Boolean isRecursive, String... fileNames) throws Exception {
        if (CollectionUtils.isAnyNull(isEmptyFolder, isRecursive)) {
            throw new Exception(ERR_NULL_ARGS);
        }

        if (fileNames == null || fileNames.length == 0) {
            throw new Exception(ERR_NO_FILE_ARGS);
        }

        if (CollectionUtils.isAnyNull((Object[]) fileNames)) {
            throw new Exception(ERR_INVALID_FILES);
        }

        for (String fileName : fileNames) {
            Path filePath = IOUtils.resolveAbsoluteFilePath(fileName);
            if (Files.notExists(filePath)) {
                throw new Exception(ERR_FILE_NOT_FOUND);
            }

            if (Files.isDirectory(filePath)) {
                if (isRecursive) {
                    Files.walk(filePath)
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                } else if (isEmptyFolder && isEmptyDir(filePath)) {
                    Files.delete(filePath);
                } else {
                    throw new Exception(ERR_IS_DIR);
                }
            } else {
                Files.delete(filePath);
            }
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
