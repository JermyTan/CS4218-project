package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.LsInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.LsException;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.impl.parser.LsArgsParser;
import sg.edu.nus.comp.cs4218.impl.result.LsResult;
import sg.edu.nus.comp.cs4218.impl.util.CollectionUtils;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_CURR_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class LsApplication implements LsInterface {
    private final static String PATH_CURR_DIR = STRING_CURR_DIR + CHAR_FILE_SEP;

    /**
     * Runs the ls application with the specified arguments.
     *
     * @param args array of arguments for the application.
     * @param stdin an InputStream, not used.
     * @param stdout an OutputStream. Contents in the given directories will be output to stdout.
     * @throws LsException if the file(s) specified do not exist or are unreadable.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws LsException {
        if (stdout == null) {
            throw new LsException(ERR_NO_OSTREAM);
        }

        LsArgsParser parser = new LsArgsParser();

        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new LsException(e.getMessage(), e);
        }

        boolean isFoldersOnly = parser.isFoldersOnly();
        boolean isRecursive = parser.isRecursive();
        boolean isSortByExt = parser.isSortByExt();
        String[] folderNames = parser.getFolderNames().toArray(String[]::new);

        String output = listFolderContent(isFoldersOnly, isRecursive, isSortByExt, folderNames);

        if (output.isEmpty()) {
            return;
        }

        try {
            stdout.write(output.getBytes());
            stdout.write(STRING_NEWLINE.getBytes());
        } catch (Exception e) {
            throw new LsException(ERR_WRITE_STREAM, e);
        }
    }

    private LsResult listFolder(boolean isFoldersOnly, String folderName) throws LsException{
        if (folderName == null) {
            throw new LsException(ERR_INVALID_FILES);
        }

        try {
            Path filePath = IOUtils.resolveAbsoluteFilePath(folderName);

            if (Files.notExists(filePath)) {
                throw new InvalidDirectoryException(folderName, ERR_FILE_NOT_FOUND);
            }

            if (!Files.isDirectory(filePath)) {
                throw new InvalidDirectoryException(folderName, ERR_IS_NOT_DIR);
            }

            try {
                List<File> files = Arrays.asList(
                        Objects.requireNonNull(
                                filePath.toFile()
                                        .listFiles(file -> !file.isHidden() && (!isFoldersOnly || file.isDirectory()))
                        )
                );

                return new LsResult(folderName, files);

            } catch (Exception e) {
                throw new InvalidDirectoryException(folderName, ERR_READING_FILE, e);
            }

        } catch (Exception e) {
            return new LsResult(new LsException(e.getMessage(), e).getMessage());
        }
    }

    private List<LsResult> listFolders(
            boolean isFoldersOnly,
            boolean isRecursive,
            String... folderNames
    ) throws LsException {
        if (folderNames == null || CollectionUtils.isAnyNull((Object[]) folderNames)) {
            throw new LsException(ERR_INVALID_FILES);
        }

        List<LsResult> result = new ArrayList<>();

        for (String folderName: folderNames) {
            LsResult content = listFolder(isFoldersOnly, folderName);

            result.add(content);

            if (isRecursive) {
                for (File file: content.getFiles()) {
                    if (!file.isDirectory()) {
                        continue;
                    }

                    String newFolderName = Paths.get(
                            folderName.isEmpty() ? PATH_CURR_DIR : folderName,
                            file.getName()
                    ).toString();
                    result.addAll(listFolders(isFoldersOnly, true, newFolderName));
                }
            }
        }

        return result;
    }

    @Override
    public String listFolderContent(
            Boolean isFoldersOnly,
            Boolean isRecursive,
            Boolean isSortByExt,
            String... folderNames
    ) throws LsException {
        // okay for folderNames itself to be null but not okay if it contains any null values
        if (folderNames != null && CollectionUtils.isAnyNull((Object[]) folderNames)) {
            throw new LsException(ERR_INVALID_FILES);
        }

        String[] nonNullFolderNames = Objects.requireNonNullElse(folderNames, new String[]{STRING_EMPTY});

        if (nonNullFolderNames.length == 0) {
            nonNullFolderNames = new String[]{STRING_EMPTY};
        }

        List<LsResult> result = listFolders(isFoldersOnly, isRecursive, nonNullFolderNames);

        result.forEach(LsResult::outputError);

        List<String> resultString = result.stream()
                .map(content -> content.formatToString(result.size() > 1, isSortByExt))
                .filter(Predicate.not(String::isEmpty))
                .collect(Collectors.toList());

        return String.join(STRING_NEWLINE + STRING_NEWLINE, resultString);
    }
}
