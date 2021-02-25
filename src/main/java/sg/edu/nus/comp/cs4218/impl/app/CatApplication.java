package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_FILE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_FILE_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_READING_FILE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_READ_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_UNEXPECTED;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_FLAG;

import sg.edu.nus.comp.cs4218.app.CatInterface;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.impl.parser.CatArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
            throw new CatException(e.getMessage());
        }

        Boolean isLineNumber = parser.isLineNumber();
        String[] fileNames = parser.getFileNames().toArray(String[]::new);

        if (stdin == null && (fileNames == null || fileNames.length == 0)) {
            throw new CatException(ERR_NO_INPUT);
        }

        String result = catContent(isLineNumber, stdin, fileNames);

        try {
            stdout.write(result.getBytes());
            stdout.write(STRING_NEWLINE.getBytes());
        } catch (Exception e) {
            throw new CatException(ERR_WRITE_STREAM);
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

    private String formatLines(List<String> lines, boolean isLineNumber) {
        List<String> result = lines;

        if (isLineNumber) {
            result = IntStream.rangeClosed(1, lines.size())
                    .mapToObj(index -> String.format(STRING_LABEL_VALUE_PAIR, index, lines.get(index-1)))
                    .collect(Collectors.toList());
        }

        return String.join(STRING_NEWLINE, result);
    }

    private String catFile(boolean isLineNumber, String fileName) throws CatException {
        if (fileName == null) {
            throw new CatException(ERR_NO_FILE_ARGS);
        }

        if (StringUtils.isBlank(fileName)) {
            throw new CatException(ERR_INVALID_FILE);
        }

        String trimmedFileName = fileName.trim();
        List<String > result;

        try {
            Path filePath = IOUtils.resolveFilePath(trimmedFileName);
            if (!Files.exists(filePath)) {
                throw new InvalidDirectoryException(trimmedFileName, ERR_FILE_NOT_FOUND);
            }

            if (Files.isDirectory(filePath)) {
                throw new InvalidDirectoryException(trimmedFileName, ERR_IS_DIR);
            }

            try {
                result = IOUtils.getLinesFromInputStream(Files.newInputStream(filePath));

            } catch (Exception e) {
                throw new InvalidDirectoryException(trimmedFileName, ERR_READING_FILE);
            }

        } catch (Exception e) {
            result = List.of(new CatException(e.getMessage()).getMessage());
        }

        return formatLines(result, isLineNumber);

    }

    @Override
    public String catFiles(Boolean isLineNumber, String... fileNames) throws CatException {
        if (fileNames == null || fileNames.length == 0) {
            throw new CatException(ERR_NO_FILE_ARGS);
        }

        String[] sanitizedFileNames = StringUtils.sanitizeStrings(fileNames);

        if (sanitizedFileNames.length == 0) {
            throw new CatException(ERR_INVALID_FILE);
        }

        List<String> result = new ArrayList<>();

        for (String fileName: sanitizedFileNames) {
            String content = catFile(isLineNumber, fileName);

            // check for empty instead of blank since the content could be purely whitespaces
            if (!content.isEmpty()) {
                result.add(content);
            }
        }

        return String.join(STRING_NEWLINE, result);
    }

    @Override
    public String catStdin(Boolean isLineNumber, InputStream stdin) throws CatException {
        if (stdin == null) {
            throw new CatException(ERR_NO_ISTREAM);
        }

        List<String> result;

        try {
            result = IOUtils.getLinesFromInputStream(stdin);
        } catch (IOException e) {
            result = List.of(new CatException(ERR_READ_STREAM).getMessage());
        } catch (Exception e) {
            result = List.of(new CatException(ERR_UNEXPECTED).getMessage());
        }

        return formatLines(result, isLineNumber);
    }

    @Override
    public String catFileAndStdin(Boolean isLineNumber, InputStream stdin, String... fileNames) throws CatException {
        if (stdin == null) {
            throw new CatException(ERR_NO_ISTREAM);
        }

        if (fileNames == null || fileNames.length == 0) {
            throw new CatException(ERR_NO_FILE_ARGS);
        }

        String[] sanitizedFileNames = StringUtils.sanitizeStrings(fileNames);

        if (sanitizedFileNames.length == 0) {
            throw new CatException(ERR_INVALID_FILE);
        }

        List<String> result = new ArrayList<>();

        for (String fileName: sanitizedFileNames) {
            String content = fileName.equals(STRING_STDIN_FLAG)
                    ? catStdin(isLineNumber, stdin)
                    : catFile(isLineNumber, fileName);

            // check for empty instead of blank since the content could be purely whitespaces
            if (!content.isEmpty()) {
                result.add(content);
            }
        }

        return String.join(STRING_NEWLINE, result);
    }
}
