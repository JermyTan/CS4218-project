package sg.edu.nus.comp.cs4218.impl.app;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_FILE_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_READING_FILE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_WRITE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_WRITING_FILE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_FLAG;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;

import sg.edu.nus.comp.cs4218.app.UniqInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.exception.UniqException;
import sg.edu.nus.comp.cs4218.impl.parser.UniqArgsParser;
import sg.edu.nus.comp.cs4218.impl.result.UniqResult;
import sg.edu.nus.comp.cs4218.impl.util.CollectionUtils;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

@SuppressWarnings("PMD.GodClass")
public class UniqApplication implements UniqInterface {

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws UniqException {
        UniqArgsParser parser = new UniqArgsParser();

        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new UniqException(e.getMessage(), e);
        }

        boolean isCount = parser.isCount();
        boolean isRepeated = parser.isRepeated();
        boolean isAllRepeated = parser.isAllRepeated();
        String inputFileName = parser.getInputFileName();
        String outputFileName = parser.getOutputFilename();

        if ((inputFileName == null || inputFileName.equals(STRING_STDIN_FLAG)) && stdin == null) {
            throw new UniqException(ERR_NO_INPUT);
        }

        if (outputFileName == null && stdout == null) {
            throw new UniqException(ERR_NO_OSTREAM);
        }

        String output = uniqContent(
                isCount,
                isRepeated,
                isAllRepeated,
                stdin,
                inputFileName,
                outputFileName
        );

        if (outputFileName != null || output.isEmpty()) {
            return;
        }

        try {
            stdout.write(output.getBytes());
            stdout.write(STRING_NEWLINE.getBytes());
        } catch (Exception e) {
            throw new UniqException(ERR_WRITE_STREAM, e);
        }
    }

    private String uniqContent(
            boolean isCount,
            boolean isRepeated,
            boolean isAllRepeated,
            InputStream stdin,
            String inputFilename,
            String outputFileName
    ) throws UniqException {
        if (inputFilename == null || inputFilename.equals(STRING_STDIN_FLAG)) {
            return uniqFromStdin(isCount, isRepeated, isAllRepeated, stdin, outputFileName);
        } else {
            return uniqFromFile(isCount, isRepeated, isAllRepeated, inputFilename, outputFileName);
        }
    }

    private void uniqToFile(String content, String fileName) throws UniqException {
        try {
            if (fileName.isEmpty()) {
                throw new InvalidDirectoryException(fileName, ERR_FILE_NOT_FOUND);
            }

            Path filePath = IOUtils.resolveAbsoluteFilePath(fileName);

            if (Files.isDirectory(filePath)) {
                throw new InvalidDirectoryException(fileName, ERR_IS_DIR);
            }

            if (Files.exists(filePath) && !Files.isWritable(filePath)) {
                throw new InvalidDirectoryException(fileName, ERR_NO_PERM);
            }

            try {
                Files.write(
                        filePath,
                        Arrays.stream(content.split(STRING_NEWLINE)).collect(Collectors.toList()),
                        CREATE,
                        WRITE,
                        TRUNCATE_EXISTING
                );
            } catch (Exception e) {
                throw new InvalidDirectoryException(fileName, ERR_WRITING_FILE, e);
            }
        } catch (Exception e) {
            throw new UniqException(e.getMessage(), e);
        }
    }

    @Override
    public String uniqFromFile(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, String inputFileName, String outputFileName) throws UniqException {
        if (inputFileName == null) {
            throw new UniqException(ERR_NO_FILE_ARGS);
        }

        if (CollectionUtils.isAnyNull(isCount, isRepeated, isAllRepeated)) {
            throw new UniqException(ERR_NULL_ARGS);
        }

        if (isAllRepeated && isCount) {
            throw new UniqException(ERR_INVALID_ARGS);
        }

        UniqResult result;

        try {
            if (inputFileName.isEmpty()) {
                throw new InvalidDirectoryException(inputFileName, ERR_FILE_NOT_FOUND);
            }

            Path filePath = IOUtils.resolveAbsoluteFilePath(inputFileName);

            if (Files.notExists(filePath)) {
                throw new InvalidDirectoryException(inputFileName, ERR_FILE_NOT_FOUND);
            }

            if (Files.isDirectory(filePath)) {
                throw new InvalidDirectoryException(inputFileName, ERR_IS_DIR);
            }

            try {
                result = new UniqResult(IOUtils.getLinesFromInputStream(Files.newInputStream(filePath)));
            } catch (Exception e) {
                throw new InvalidDirectoryException(inputFileName, ERR_READING_FILE, e);
            }
        } catch (Exception e) {
            throw new UniqException(e.getMessage(), e);
        }

        String content = result.formatToString(isCount, isRepeated, isAllRepeated);

        if (outputFileName != null) {
            uniqToFile(content, outputFileName);
        }

        return content;
    }

    @Override
    public String uniqFromStdin(Boolean isCount, Boolean isRepeated, Boolean isAllRepeated, InputStream stdin, String outputFileName) throws UniqException {
        if (stdin == null) {
            throw new UniqException(ERR_NO_ISTREAM);
        }

        if (CollectionUtils.isAnyNull(isCount, isRepeated, isAllRepeated)) {
            throw new UniqException(ERR_NULL_ARGS);
        }

        if (isAllRepeated && isCount) {
            throw new UniqException(ERR_INVALID_ARGS);
        }

        UniqResult result;

        try {
            result = new UniqResult(IOUtils.getLinesFromInputStream(stdin));
        } catch (Exception e) {
            throw new UniqException(e.getMessage(), e);
        }

        String content = result.formatToString(isCount, isRepeated, isAllRepeated);

        if (outputFileName != null) {
            uniqToFile(content, outputFileName);
        }

        return content;
    }
}
