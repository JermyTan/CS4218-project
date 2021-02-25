package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.WcInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.impl.parser.WcArgsParser;
import sg.edu.nus.comp.cs4218.impl.result.WcResult;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_FLAG;

@SuppressWarnings("PMD.GodClass")
public class WcApplication implements WcInterface {

    private static final String TOTAL_LABEL = "total";
    private static final String STDIN_LABEL = STRING_EMPTY;

    /**
     * Runs the wc application with the specified arguments.
     *
     * @param args   array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  an InputStream. Can be used to read in lines from stdin.
     * @param stdout an OutputStream. The output of the command is written to this OutputStream.
     * @throws WcException if the file(s) specified do not exist or are unreadable.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws WcException {
        if (stdout == null) {
            throw new WcException(ERR_NO_OSTREAM);
        }

        WcArgsParser parser = new WcArgsParser();

        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new WcException(e.getMessage(), e);
        }

        boolean isDefault = parser.isDefault();
        boolean isBytes = parser.isBytes() || isDefault;
        boolean isLines = parser.isLines() || isDefault;
        boolean isWords = parser.isWords() || isDefault;
        String[] fileNames = parser.getFileNames().toArray(String[]::new);

        if (stdin == null && (fileNames == null || fileNames.length == 0)) {
            throw new WcException(ERR_NO_INPUT);
        }

        String output = wcContent(
                isBytes,
                isLines,
                isWords,
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
            throw new WcException(ERR_WRITE_STREAM, e);
        }
    }

    private String wcContent(
            boolean isBytes,
            boolean isLines,
            boolean isWords,
            InputStream stdin,
            String... fileNames
    ) throws WcException {
        if (fileNames == null || fileNames.length == 0) {
            return countFromStdin(isBytes, isLines, isWords, stdin);
        }

        if (List.of(fileNames).contains(STRING_STDIN_FLAG)) {
            return countFromFileAndStdin(isBytes, isLines, isWords, stdin, fileNames);
        }

        return countFromFiles(isBytes, isLines, isWords, fileNames);
    }

    private List<WcResult> computeAndAttachTotalStatistics(List<WcResult> statisticsList) {
        WcResult totalStatistics = new WcResult(
                TOTAL_LABEL,
                statisticsList.stream().mapToLong(WcResult::getNumLines).sum(),
                statisticsList.stream().mapToLong(WcResult::getNumWords).sum(),
                statisticsList.stream().mapToLong(WcResult::getNumBytes).sum()
        );

        return Stream.concat(statisticsList.stream(), Stream.of(totalStatistics)).collect(Collectors.toList());
    }

    private WcResult computeStatisticsFromInputStream(
            String label,
            InputStream inputStream,
            Path filePath
    ) throws WcException {
        try {
            List<String> lines = IOUtils.getLinesFromInputStream(inputStream);

            long numLines = lines.size();
            long numWords = lines.stream().map(StringUtils::tokenize).flatMap(Stream::of).count();
            long numBytes = filePath == null
                    ? lines.stream().mapToInt(line -> line.getBytes().length).sum() + numWords
                    : Files.size(filePath);


            return new WcResult(label, numLines, numWords, numBytes);

        } catch (ShellException e) {
            throw new WcException(e.getMessage(), e);
        } catch (Exception e) {
            throw new WcException(ERR_READ_STREAM, e);
        }
    }

    private WcResult computeStatisticsFromFile(String fileName) throws WcException {
        if (fileName == null) {
            throw new WcException(ERR_NO_FILE_ARGS);
        }

        if (fileName.isBlank()) {
            throw new WcException(ERR_INVALID_FILES);
        }

        String trimmedFileName = fileName.trim();

        try {
            Path filePath = IOUtils.resolveFilePath(trimmedFileName);
            if (!Files.exists(filePath)) {
                throw new InvalidDirectoryException(trimmedFileName, ERR_FILE_NOT_FOUND);
            }

            if (Files.isDirectory(filePath)) {
                throw new InvalidDirectoryException(trimmedFileName, ERR_IS_DIR);
            }

            try {
                return computeStatisticsFromInputStream(trimmedFileName, Files.newInputStream(filePath), filePath);
            } catch (Exception e) {
                throw new InvalidDirectoryException(trimmedFileName, ERR_READING_FILE, e);
            }

        } catch (Exception e) {
            return new WcResult(new WcException(e.getMessage(), e).getMessage());
        }
    }

    private WcResult computeStatisticsFromStdin(InputStream stdin) throws WcException{
        if (stdin == null) {
            throw new WcException(ERR_NO_ISTREAM);
        }

        try {
            return computeStatisticsFromInputStream(STDIN_LABEL, stdin, null);
        } catch (Exception e) {
            return new WcResult(new WcException(ERR_READ_STREAM, e).getMessage());
        }
    }

    private String formatStatistics(
            List<WcResult> statisticsList,
            boolean isBytes,
            boolean isLines,
            boolean isWords
    ) {
        int numEntries = statisticsList.size();

        List<String> result = (numEntries > 1 ? computeAndAttachTotalStatistics(statisticsList) : statisticsList)
                .stream()
                .map(statistics -> statistics.formatToString(isBytes, isLines, isWords))
                .filter(Predicate.not(String::isBlank))
                .collect(Collectors.toList());

        return String.join(STRING_NEWLINE, result).trim();
    }

    @Override
    public String countFromFiles(
            Boolean isBytes,
            Boolean isLines,
            Boolean isWords,
            String... fileNames
    ) throws WcException {
        if (fileNames == null || fileNames.length == 0) {
            throw new WcException(ERR_NO_FILE_ARGS);
        }

        String[] sanitizedFileNames = StringUtils.sanitizeStrings(fileNames);

        if (sanitizedFileNames.length == 0) {
            throw new WcException(ERR_INVALID_FILES);
        }

        List<WcResult> result = new ArrayList<>();

        for (String fileName: sanitizedFileNames) {
            WcResult statistics = computeStatisticsFromFile(fileName);

            statistics.outputError();

            result.add(statistics);
        }

        return formatStatistics(result, isBytes, isLines, isWords);
    }

    @Override
    public String countFromStdin(
            Boolean isBytes,
            Boolean isLines,
            Boolean isWords,
            InputStream stdin
    ) throws WcException {
        if (stdin == null) {
            throw new WcException(ERR_NO_ISTREAM);
        }

        WcResult statistics = computeStatisticsFromStdin(stdin);

        statistics.outputError();

        return formatStatistics(List.of(statistics), isBytes, isLines, isWords);
    }

    @Override
    public String countFromFileAndStdin(
            Boolean isBytes,
            Boolean isLines,
            Boolean isWords,
            InputStream stdin,
            String... fileNames
    ) throws WcException {
        if (stdin == null) {
            throw new WcException(ERR_NO_ISTREAM);
        }

        if (fileNames == null || fileNames.length == 0) {
            throw new WcException(ERR_NO_FILE_ARGS);
        }

        String[] sanitizedFileNames = StringUtils.sanitizeStrings(fileNames);

        if (sanitizedFileNames.length == 0) {
            throw new WcException(ERR_INVALID_FILES);
        }

        List<WcResult> result = new ArrayList<>();

        for (String fileName: sanitizedFileNames) {
            WcResult statistics = fileName.equals(STRING_STDIN_FLAG)
                    ? computeStatisticsFromStdin(stdin)
                    : computeStatisticsFromFile(fileName);

            statistics.outputError();

            result.add(statistics);
        }

        return formatStatistics(result, isBytes, isLines, isWords);
    }
}
