package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_FILES;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_FILE_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_READING_FILE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_READ_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_FLAG;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sg.edu.nus.comp.cs4218.app.CatInterface;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.impl.parser.CatArgsParser;
import sg.edu.nus.comp.cs4218.impl.result.CatResult;
import sg.edu.nus.comp.cs4218.impl.util.CollectionUtils;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

@SuppressWarnings("PMD.GodClass")
public class CatApplication implements CatInterface {

    /**
     * Runs the cat application with the specified arguments.
     *
     * @param args   array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  an InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout an OutputStream. The output of the command is written to this OutputStream.
     * @throws CatException if the file(s) specified do not exist or are unreadable.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws CatException {
        if (stdout == null) {
            throw new CatException(ERR_NO_OSTREAM);
        }

        CatArgsParser parser = new CatArgsParser();

        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new CatException(e.getMessage(), e);
        }

        boolean isLineNumber = parser.isLineNumber();
        String[] fileNames = parser.getFileNames().toArray(String[]::new);

        if (stdin == null && (fileNames == null || fileNames.length == 0)) {
            throw new CatException(ERR_NO_INPUT);
        }

        String output = catContent(isLineNumber, stdin, fileNames);

        if (output.isEmpty()) {
            return;
        }

        try {
            stdout.write(output.getBytes());
            stdout.write(STRING_NEWLINE.getBytes());
        } catch (Exception e) {
            throw new CatException(ERR_WRITE_STREAM, e);
        }
    }

    private String catContent(boolean isLineNumber, InputStream stdin, String... fileNames) throws CatException {
        if (fileNames == null || fileNames.length == 0) {
            return catStdin(isLineNumber, stdin);
        }

        if (List.of(fileNames).contains(STRING_STDIN_FLAG)) {
            return catFileAndStdin(isLineNumber, stdin, fileNames);
        }

        return catFiles(isLineNumber, fileNames);
    }

    private CatResult computeCatFile(String fileName) {
        try {
            if (fileName.isEmpty()) {
                throw new InvalidDirectoryException(fileName, ERR_FILE_NOT_FOUND);
            }

            Path filePath = IOUtils.resolveAbsoluteFilePath(fileName);

            if (Files.notExists(filePath)) {
                throw new InvalidDirectoryException(fileName, ERR_FILE_NOT_FOUND);
            }

            if (Files.isDirectory(filePath)) {
                throw new InvalidDirectoryException(fileName, ERR_IS_DIR);
            }

            try {
                return new CatResult(IOUtils.getLinesFromInputStream(Files.newInputStream(filePath)));

            } catch (Exception e) {
                throw new InvalidDirectoryException(fileName, ERR_READING_FILE, e);
            }

        } catch (Exception e) {
            return new CatResult(new CatException(e.getMessage(), e).getMessage());
        }
    }

    private CatResult computeCatStdin(InputStream stdin) {
        try {
            return new CatResult(IOUtils.getLinesFromInputStream(stdin));
        } catch (Exception e) {
            return new CatResult(new CatException(ERR_READ_STREAM, e).getMessage());
        }
    }

    @Override
    public String catFiles(Boolean isLineNumber, String... fileNames) throws CatException {
        if (fileNames == null || fileNames.length == 0) {
            throw new CatException(ERR_NO_FILE_ARGS);
        }

        if (CollectionUtils.isAnyNull((Object[]) fileNames)) {
            throw new CatException(ERR_INVALID_FILES);
        }

        if (isLineNumber == null) {
            throw new CatException(ERR_NULL_ARGS);
        }

        List<String> result = Arrays.stream(fileNames)
                .flatMap(fileName -> {
                    CatResult content = computeCatFile(fileName);

                    content.outputError();

                    String contentString = content.formatToString(isLineNumber);

                    return !contentString.isEmpty() ? Stream.of(contentString) : Stream.empty();
                })
                .collect(Collectors.toList());

        return String.join(STRING_NEWLINE, result);
    }

    @Override
    public String catStdin(Boolean isLineNumber, InputStream stdin) throws CatException {
        if (stdin == null) {
            throw new CatException(ERR_NO_ISTREAM);
        }

        if (isLineNumber == null) {
            throw new CatException(ERR_NULL_ARGS);
        }

        CatResult result = computeCatStdin(stdin);

        result.outputError();

        return result.formatToString(isLineNumber);
    }

    @Override
    public String catFileAndStdin(Boolean isLineNumber, InputStream stdin, String... fileNames) throws CatException {
        if (stdin == null) {
            throw new CatException(ERR_NO_ISTREAM);
        }

        if (fileNames == null || fileNames.length == 0) {
            throw new CatException(ERR_NO_FILE_ARGS);
        }

        if (CollectionUtils.isAnyNull((Object[]) fileNames)) {
            throw new CatException(ERR_INVALID_FILES);
        }

        if (isLineNumber == null) {
            throw new CatException(ERR_NULL_ARGS);
        }

        List<String> result = Arrays.stream(fileNames)
                .flatMap(fileName -> {
                    CatResult content = fileName.equals(STRING_STDIN_FLAG)
                            ? computeCatStdin(stdin)
                            : computeCatFile(fileName);

                    content.outputError();

                    String contentString = content.formatToString(isLineNumber);

                    return !contentString.isEmpty() ? Stream.of(contentString) : Stream.empty();
                })
                .collect(Collectors.toList());

        return String.join(STRING_NEWLINE, result);
    }
}
