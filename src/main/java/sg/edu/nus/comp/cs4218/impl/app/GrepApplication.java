package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.GrepInterface;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.impl.parser.GrepArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_FLAG;

@SuppressWarnings("PMD.GodClass")
public class GrepApplication implements GrepInterface {
    private static final String STDIN_LABEL = "(standard input)";

    private static class GrepResult {
        String label;
        List<String> lines;
        boolean isError = false;

        GrepResult(String label, List<String> lines) {
            this.label = label;
            this.lines = lines;
        }

        GrepResult(String label, List<String> lines, boolean isError) {
            this.label = label;
            this.lines = lines;
            this.isError = isError;
        }

        String formatToString(boolean isCountLines, boolean isPrefixFileName) {
            String result;

            if (isError) {
                result = String.join(STRING_NEWLINE, lines);
            } else if (isCountLines) {
                String stringCount = String.valueOf(lines.size());
                result = isPrefixFileName ? String.format(STRING_LABEL_VALUE_PAIR, label, stringCount) : stringCount;
            } else {
                List<String> formattedLines = lines.stream()
                        .map(line -> isPrefixFileName ? String.format(STRING_LABEL_VALUE_PAIR, label, line) : line)
                        .collect(Collectors.toList());
                result = String.join(STRING_NEWLINE, formattedLines);
            }

            return result.trim();
        }
    }

    /**
     * Runs the grep application with the specified arguments.
     *
     * @param args array of arguments for the application.
     * @param stdin an InputStream. Can be used to read in lines from stdin.
     * @param stdout an OutputStream. Lines which match supplied pattern will be output to stdout,
     *               separated by a newline character.
     * @throws GrepException if the file(s) specified do not exist or are unreadable.
     */
    @Override
    @SuppressWarnings("PMD.PreserveStackTrace")
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws GrepException {
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
        String[] fileNames = parser.getFileNames().toArray(String[]::new);

        if (stdin == null && (fileNames == null || fileNames.length == 0)) {
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

    @SuppressWarnings("PMD.PreserveStackTrace")
    private Pattern processRegexPattern(String pattern, boolean isCaseInsensitive) throws GrepException{
        try {
            return isCaseInsensitive
                    ? Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
                    : Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            throw new GrepException(ERR_INVALID_REGEX);
        }
    }

    private List<String> grepFromInputStream(
            Pattern grepPattern,
            InputStream inputStream
    ) throws Exception {
        return IOUtils.getLinesFromInputStream(inputStream)
                .stream()
                .filter(line -> grepPattern.matcher(line).find())
                .collect(Collectors.toList());
    }

    @SuppressWarnings("PMD.PreserveStackTrace")
    private String grepFromFile(
            String pattern,
            boolean isCaseInsensitive,
            boolean isCountLines,
            boolean isPrefixFileName,
            String fileName
    ) throws GrepException {
        if (pattern == null) {
            throw new GrepException(ERR_NO_REGEX);
        }

        if (fileName == null) {
            throw new GrepException(ERR_NO_FILE_ARGS);
        }

        if (StringUtils.isBlank(fileName)) {
            throw new GrepException(ERR_INVALID_FILE);
        }

        Pattern grepPattern = processRegexPattern(pattern, isCaseInsensitive);

        String trimmedFileName = fileName.trim();
        GrepResult result;

        try {
            Path filePath = IOUtils.resolveFilePath(trimmedFileName);
            if (!Files.exists(filePath)) {
                throw new InvalidDirectoryException(trimmedFileName, ERR_FILE_NOT_FOUND);
            }

            if (Files.isDirectory(filePath)) {
                throw new InvalidDirectoryException(trimmedFileName, ERR_IS_DIR);
            }

            try {
                result = new GrepResult(trimmedFileName, grepFromInputStream(grepPattern, Files.newInputStream(filePath)));

            } catch (Exception e) {
                throw new InvalidDirectoryException(trimmedFileName, ERR_READING_FILE);
            }

        } catch (Exception e) {
            result = new GrepResult(trimmedFileName, List.of(new GrepException(e.getMessage()).getMessage()), true);
        }

        return result.formatToString(isCountLines, isPrefixFileName).trim();
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
        if (pattern == null) {
            throw new GrepException(ERR_NO_REGEX);
        }
        if (fileNames == null || fileNames.length == 0) {
            throw new GrepException(ERR_NO_FILE_ARGS);
        }

        String[] sanitizedFileNames = StringUtils.sanitizeStrings(fileNames);

        if (sanitizedFileNames.length == 0) {
            throw new GrepException(ERR_INVALID_FILE);
        }

        List<String> result = new ArrayList<>();

        for (String fileName: sanitizedFileNames) {
            String grepString = grepFromFile(
                    pattern,
                    isCaseInsensitive,
                    isCountLines,
                    isPrefixFileName || fileNames.length > 1,
                    fileName
            );

            if (!StringUtils.isBlank(grepString)) {
                result.add(grepString);
            }
        }

        return String.join(STRING_NEWLINE, result).trim();
    }

    @Override
    @SuppressWarnings("PMD.PreserveStackTrace")
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

        Pattern grepPattern = processRegexPattern(pattern, isCaseInsensitive);

        GrepResult result;

        try {
            result = new GrepResult(STDIN_LABEL, grepFromInputStream(grepPattern, stdin));
        } catch (IOException e) {
            result = new GrepResult(STDIN_LABEL, List.of(new GrepException(ERR_READ_STREAM).getMessage()), true);
        } catch (Exception e) {
            result = new GrepResult(STDIN_LABEL, List.of(new GrepException(ERR_UNEXPECTED).getMessage()), true);
        }

        return result.formatToString(isCountLines, isPrefixFileName).trim();
    }

    @Override
    @SuppressWarnings("PMD.ExcessiveMethodLength")
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

        String[] sanitizedFileNames = StringUtils.sanitizeStrings(fileNames);

        if (sanitizedFileNames.length == 0) {
            throw new GrepException(ERR_INVALID_FILE);
        }

        List<String> result = new ArrayList<>();

        for (String fileName: sanitizedFileNames) {
            String grepString;

            if (fileName.equals(STRING_STDIN_FLAG)) {
                grepString = grepFromStdin(
                        pattern,
                        isCaseInsensitive,
                        isCountLines,
                        true,
                        stdin
                );
            } else {
                grepString = grepFromFile(
                        pattern,
                        isCaseInsensitive,
                        isCountLines,
                        true,
                        fileName
                );
            }

            if (!StringUtils.isBlank(grepString)) {
                result.add(grepString);
            }
        }

        return String.join(STRING_NEWLINE, result).trim();
    }
}
