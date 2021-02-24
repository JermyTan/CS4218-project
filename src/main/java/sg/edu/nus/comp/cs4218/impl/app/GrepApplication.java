package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.GrepInterface;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.parser.GrepArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_FLAG;

public class GrepApplication implements GrepInterface {
    private static final String STDIN_LABEL = "(standard input)";

    /**
     * Runs the grep application.
     *
     * @param args array of arguments for the application.
     * @param stdin an InputStream. Can be used to read in lines from stdin.
     * @param stdout an OutputStream. Lines which match supplied pattern will be output to stdout,
     *               separated by a newline character.
     * @throws GrepException
     */
    @Override
    @SuppressWarnings("PMD.PreserveStackTrace")
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws GrepException {
        if (args == null) {
            throw new GrepException(ERR_NULL_ARGS);
        }

        if (stdout == null) {
            throw new GrepException(ERR_NO_OSTREAM);
        }

        GrepArgsParser parser = new GrepArgsParser();

        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new GrepException(e.getMessage());
        }

        Boolean isCaseInsensitive = parser.isCaseInsensitive();
        Boolean isCountLines = parser.isCountLines();
        Boolean isPrefixFileName = parser.isPrefixFileName();
        String pattern = parser.getPattern();
        String[] fileNames = parser.getFileNames();

        if (stdin == null && fileNames == null) {
            throw new GrepException(ERR_NO_INPUT);
        }

        String result = grepContent(
                pattern,
                isCaseInsensitive,
                isCountLines,
                isPrefixFileName,
                stdin,
                fileNames
        );

        try {
            stdout.write(result.getBytes());
            stdout.write(STRING_NEWLINE.getBytes());
        } catch (Exception e) {
            throw new GrepException(ERR_WRITE_STREAM);
        }
    }

    private String grepContent(
            String pattern,
            Boolean isCaseInsensitive,
            Boolean isCountLines,
            Boolean isPrefixFileName,
            InputStream stdin,
            String... fileNames
    ) throws GrepException {
        if (fileNames == null) {
            return grepFromStdin(pattern, isCaseInsensitive, isCountLines, isPrefixFileName, stdin);
        }

        if (Arrays.stream(fileNames).anyMatch(STRING_STDIN_FLAG::equals)) {
            return grepFromFileAndStdin(pattern, isCaseInsensitive, isCountLines, isPrefixFileName, stdin, fileNames);
        }

        return grepFromFiles(pattern, isCaseInsensitive, isCountLines, isPrefixFileName, fileNames);
    }

    @SuppressWarnings("PMD.PreserveStackTrace")
    private Pattern processRegexPattern(String pattern, Boolean isCaseInsensitive) throws GrepException{
        try {
            return isCaseInsensitive
                    ? Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
                    : Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            throw new GrepException(ERR_INVALID_REGEX);
        }
    }

    @Override
    @SuppressWarnings("PMD.PreserveStackTrace")
    public String grepFromFiles(
            String pattern,
            Boolean isCaseInsensitive,
            Boolean isCountLines,
            Boolean isPrefixFileName,
            String... fileNames
    ) throws GrepException {
        if (fileNames == null || pattern == null) {
            throw new GrepException(ERR_NULL_ARGS);
        }

        Pattern grepPattern = processRegexPattern(pattern, isCaseInsensitive);

        List<String> result = Arrays.stream(fileNames)
                .flatMap(fileName -> {
                    try {
                        Path filePath = IOUtils.resolveFilePath(fileName);
                        if (!Files.exists(filePath)) {
                            throw new GrepException(String.format(STRING_LABEL_VALUE_PAIR, fileName, ERR_FILE_NOT_FOUND));
                        }

                        if (Files.isDirectory(filePath)) {
                            throw new GrepException(String.format(STRING_LABEL_VALUE_PAIR, fileName, ERR_IS_DIR));
                        }

                        try {
                            return IOUtils.getLinesFromInputStream(Files.newInputStream(filePath))
                                    .stream()
                                    .filter(line -> grepPattern.matcher(line).find())
                                    .map(line -> isPrefixFileName ? String.format(STRING_LABEL_VALUE_PAIR, fileName, line) : line);
                        } catch (Exception e) {
                            throw new GrepException(String.format(STRING_LABEL_VALUE_PAIR, fileName, ERR_READING_FILE));
                        }

                    } catch (Exception e) {
                        return Stream.of(e.getMessage());
                    }
                })
                .collect(Collectors.toList());

        return isCountLines ? String.valueOf(result.size()) : String.join(STRING_NEWLINE, result);
    }

    @Override
    public String grepFromStdin(String pattern, Boolean isCaseInsensitive, Boolean isCountLines, Boolean isPrefixFileName, InputStream stdin) throws GrepException {
        if (pattern == null || stdin == null) {
            throw new GrepException(ERR_NULL_ARGS);
        }

        Pattern grepPattern = processRegexPattern(pattern, isCaseInsensitive);

        try {
            List<String> result = IOUtils.getLinesFromInputStream(stdin)
                    .stream()
                    .filter(line -> grepPattern.matcher(line).find())
                    .map(line -> isPrefixFileName ? String.format(STRING_LABEL_VALUE_PAIR, STDIN_LABEL, line) : line)
                    .collect(Collectors.toList());

            return isCountLines ? String.valueOf(result.size()) : String.join(STRING_NEWLINE, result);
        } catch (Exception e) {
            throw new GrepException(ERR_UNEXPECTED);
        }
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
        if (pattern == null || stdin == null || fileNames == null) {
            throw new GrepException(ERR_NULL_ARGS);
        }

        // remove "-" from fileNames
        String[] sanitizedFileNames = Arrays.stream(fileNames)
                .filter(Predicate.not(STRING_STDIN_FLAG::equals))
                .toArray(String[]::new);

        String grepResultFromFiles = grepFromFiles(
                pattern,
                isCaseInsensitive,
                isCountLines,
                isPrefixFileName,
                sanitizedFileNames
        );

        String grepResultFromStdin = grepFromStdin(
                pattern,
                isCaseInsensitive,
                isCountLines,
                isPrefixFileName,
                stdin
        );

        return String.join(
                STRING_NEWLINE,
                List.of(grepResultFromFiles,grepResultFromStdin)
        );
    }
}
