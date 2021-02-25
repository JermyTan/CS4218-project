package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static java.nio.file.StandardOpenOption.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import sg.edu.nus.comp.cs4218.app.TeeInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.TeeException;
import sg.edu.nus.comp.cs4218.impl.parser.TeeArgsParser;
import sg.edu.nus.comp.cs4218.impl.result.TeeResult;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

public class TeeApplication implements TeeInterface {

    /**
     * Runs the tee application with the specified arguments.
     *
     * @param args   array of arguments for the application. Each array element is the path to an
     *               output file. If no files are specified, only stdout is used.
     * @param stdin  an InputStream. Can be used to read in lines from stdin.
     * @param stdout an OutputStream. The output of the command is written to this OutputStream.
     * @throws TeeException
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws TeeException {
        if (stdin == null) {
            throw new TeeException(ERR_NO_ISTREAM);
        }

        if (stdout == null) {
            throw new TeeException(ERR_NO_OSTREAM);
        }

        TeeArgsParser parser = new TeeArgsParser();

        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new TeeException(e.getMessage(), e);
        }

        boolean isAppend = parser.isAppend();
        String[] fileNames = parser.getFileNames().toArray(String[]::new);

        String output = teeFromStdin(isAppend, stdin, fileNames);

        if (output.isEmpty()) {
            return;
        }

        try {
            stdout.write(output.getBytes());
            stdout.write(STRING_NEWLINE.getBytes());
        } catch (Exception e) {
            throw new TeeException(ERR_WRITE_STREAM, e);
        }
    }

    private List<String> teeFromInputStream(InputStream inputStream) throws TeeException {
        try {
            return IOUtils.getLinesFromInputStream(inputStream);
        } catch (ShellException e) {
            throw new TeeException(e.getMessage(), e);
        }
    }

    private TeeResult teeToFile(boolean isAppend, List<String> content, String fileName) throws TeeException {
        if (content == null) {
            throw new TeeException(ERR_NULL_ARGS);
        }

        if (fileName == null) {
            throw new TeeException(ERR_NO_FILE_ARGS);
        }

        if (fileName.isBlank()) {
            throw new TeeException(ERR_INVALID_FILES);
        }

        String trimmedFileName = fileName.trim();

        try {
            Path filePath = IOUtils.resolveFilePath(trimmedFileName);

            if (Files.isDirectory(filePath)) {
                throw new InvalidDirectoryException(trimmedFileName, ERR_IS_DIR);
            }

            try {
                if (isAppend) {
                    Files.write(filePath, content, CREATE, WRITE, APPEND);
                } else {
                    Files.write(filePath, content, CREATE, WRITE, TRUNCATE_EXISTING);
                }

                return new TeeResult();
            } catch (Exception e) {
                throw new InvalidDirectoryException(trimmedFileName, ERR_FILE_NOT_FOUND, e);
            }

        } catch (Exception e) {
            return new TeeResult(new TeeException(e.getMessage()).getMessage());
        }
    }

    @Override
    public String teeFromStdin(Boolean isAppend, InputStream stdin, String... fileNames) throws TeeException {
        if (stdin == null) {
            throw new TeeException(ERR_NO_ISTREAM);
        }

        List<String> result = teeFromInputStream(stdin);

        String[] sanitizedFileNames = StringUtils.sanitizeStrings(fileNames);

        for (String fileName: sanitizedFileNames) {
            teeToFile(isAppend, result, fileName).outputError();
        }

        return String.join(STRING_NEWLINE, result);
    }
}
