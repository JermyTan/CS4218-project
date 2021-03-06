package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_FILES;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_REGEX;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_FILE_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_REGEX;
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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sg.edu.nus.comp.cs4218.app.GrepInterface;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.impl.parser.GrepArgsParser;
import sg.edu.nus.comp.cs4218.impl.result.GrepResult;
import sg.edu.nus.comp.cs4218.impl.util.CollectionUtils;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

@SuppressWarnings("PMD.GodClass")
public class GrepApplication implements GrepInterface {

    public static final String STDIN_LABEL = "(standard input)";

    /**
     * Runs the grep application with the specified arguments.
     *
     * @param args   array of arguments for the application.
     * @param stdin  an InputStream. Can be used to read in lines from stdin.
     * @param stdout an OutputStream. Lines which match supplied pattern will be output to stdout,
     *               separated by a newline character.
     * @throws GrepException if the file(s) specified do not exist or are unreadable.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws GrepException {
        if (stdout == null) {
            throw new GrepException(ERR_NO_OSTREAM);
        }

        GrepArgsParser parser = new GrepArgsParser();

        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new GrepException(e.getMessage(), e);
        }

        boolean isCaseInsensitive = parser.isCaseInsensitive();
        boolean isCountLines = parser.isCountLines();
        boolean isPrefixFileName = parser.isPrefixFileName();
        String pattern = parser.getPattern();
        String[] fileNames = parser.getFileNames().toArray(String[]::new);

        if (stdin == null && (fileNames == null || fileNames.length == 0)) {
            throw new GrepException(ERR_NO_INPUT);
        }

        String output = grepContent(
                pattern,
                isCaseInsensitive,
                isCountLines,
                isPrefixFileName,
                stdin,
                fileNames
        );

        if (output.isEmpty()) {
            return;
        }

        try {
            stdout.write(output.getBytes());
            stdout.write(STRING_NEWLINE.getBytes());
        } catch (Exception e) {
            throw new GrepException(ERR_WRITE_STREAM, e);
        }
    }

    private String grepContent(
            String pattern,
            boolean isCaseInsensitive,
            boolean isCountLines,
            boolean isPrefixFileName,
            InputStream stdin,
            String... fileNames
    ) throws GrepException {
        if (fileNames == null || fileNames.length == 0) {
            return grepFromStdin(pattern, isCaseInsensitive, isCountLines, isPrefixFileName, stdin);
        }

        if (List.of(fileNames).contains(STRING_STDIN_FLAG)) {
            return grepFromFileAndStdin(pattern, isCaseInsensitive, isCountLines, isPrefixFileName, stdin, fileNames);
        }

        return grepFromFiles(pattern, isCaseInsensitive, isCountLines, isPrefixFileName, fileNames);
    }

    private Pattern processRegexPattern(String pattern, boolean isCaseInsensitive) throws GrepException {
        try {
            return isCaseInsensitive
                    ? Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
                    : Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            throw new GrepException(ERR_INVALID_REGEX, e);
        }
    }

    private List<String> grepFromInputStream(
            Pattern grepPattern,
            InputStream inputStream
    ) throws GrepException {
        try {
            return IOUtils.getLinesFromInputStream(inputStream)
                    .stream()
                    .filter(line -> grepPattern.matcher(line).find())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new GrepException(e.getMessage(), e);
        }
    }

    private GrepResult computeGrepFromFile(
            Pattern grepPattern,
            String fileName
    ) {
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
                return new GrepResult(fileName, grepFromInputStream(grepPattern, Files.newInputStream(filePath)));

            } catch (Exception e) {
                throw new InvalidDirectoryException(fileName, ERR_READING_FILE, e);
            }

        } catch (Exception e) {
            return new GrepResult(new GrepException(e.getMessage(), e).getMessage());
        }
    }

    private GrepResult computeGrepFromStdin(Pattern grepPattern, InputStream stdin) {
        try {
            return new GrepResult(STDIN_LABEL, grepFromInputStream(grepPattern, stdin));
        } catch (Exception e) {
            return new GrepResult(new GrepException(ERR_READ_STREAM, e).getMessage());
        }
    }

    @Override
    public String grepFromFiles(
            String pattern,
            Boolean isCaseInsensitive,
            Boolean isCountLines,
            Boolean isPrefixFileName,
            String... fileNames
    ) throws GrepException {
        if (pattern == null) {
            throw new GrepException(ERR_NO_REGEX);
        }
        if (fileNames == null || fileNames.length == 0) {
            throw new GrepException(ERR_NO_FILE_ARGS);
        }

        if (CollectionUtils.isAnyNull((Object[]) fileNames)) {
            throw new GrepException(ERR_INVALID_FILES);
        }

        if (CollectionUtils.isAnyNull(isCaseInsensitive, isCountLines, isPrefixFileName)) {
            throw new GrepException(ERR_NULL_ARGS);
        }

        Pattern grepPattern = processRegexPattern(pattern, isCaseInsensitive);

        List<String> result = Arrays.stream(fileNames)
                .flatMap(fileName -> {
                    GrepResult content = computeGrepFromFile(grepPattern, fileName);

                    content.outputError();

                    String contentString = content.formatToString(isCountLines, isPrefixFileName || fileNames.length > 1);

                    return contentString.isEmpty() ? Stream.empty() : Stream.of(contentString);
                })
                .collect(Collectors.toList());

        return String.join(STRING_NEWLINE, result);
    }

    @Override
    public String grepFromStdin(
            String pattern,
            Boolean isCaseInsensitive,
            Boolean isCountLines,
            Boolean isPrefixFileName,
            InputStream stdin
    ) throws GrepException {
        if (pattern == null) {
            throw new GrepException(ERR_NO_REGEX);
        }

        if (stdin == null) {
            throw new GrepException(ERR_READ_STREAM);
        }

        if (CollectionUtils.isAnyNull(isCaseInsensitive, isCountLines, isPrefixFileName)) {
            throw new GrepException(ERR_NULL_ARGS);
        }

        Pattern grepPattern = processRegexPattern(pattern, isCaseInsensitive);

        GrepResult result = computeGrepFromStdin(grepPattern, stdin);

        result.outputError();

        return result.formatToString(isCountLines, isPrefixFileName);
    }

    @Override
    public String grepFromFileAndStdin(
            String pattern,
            Boolean isCaseInsensitive,
            Boolean isCountLines,
            Boolean isPrefixFileName,
            InputStream stdin,
            String... fileNames
    ) throws GrepException {
        if (pattern == null) {
            throw new GrepException(ERR_NO_REGEX);
        }

        if (stdin == null) {
            throw new GrepException(ERR_NO_ISTREAM);
        }

        if (fileNames == null || fileNames.length == 0) {
            throw new GrepException(ERR_NO_FILE_ARGS);
        }

        if (CollectionUtils.isAnyNull((Object[]) fileNames)) {
            throw new GrepException(ERR_INVALID_FILES);
        }

        if (CollectionUtils.isAnyNull(isCaseInsensitive, isCountLines, isPrefixFileName)) {
            throw new GrepException(ERR_NULL_ARGS);
        }

        Pattern grepPattern = processRegexPattern(pattern, isCaseInsensitive);

        List<String> result = Arrays.stream(fileNames)
                .flatMap(fileName -> {
                    GrepResult content = fileName.equals(STRING_STDIN_FLAG)
                            ? computeGrepFromStdin(grepPattern, stdin)
                            : computeGrepFromFile(grepPattern, fileName);

                    content.outputError();

                    String contentString = content.formatToString(isCountLines, isPrefixFileName || fileNames.length > 1);

                    return contentString.isEmpty() ? Stream.empty() : Stream.of(contentString);
                })
                .collect(Collectors.toList());

        return String.join(STRING_NEWLINE, result);
    }
}
