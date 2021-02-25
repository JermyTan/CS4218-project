package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.WcInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.impl.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.impl.parser.WcArgsParser;
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
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_TAB;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_FLAG;

@SuppressWarnings("PMD.GodClass")
public class WcApplication implements WcInterface {
    private static final String TOTAL_LABEL = "total";
    private static final String STDIN_LABEL = STRING_EMPTY;

    private static class WcStatistics {
        long numLines = 0;
        long numWords = 0;
        long numBytes = 0;
        String label;
        boolean isError = false;

        WcStatistics(String label) {
            this.label = label;
        }

        WcStatistics(String label, boolean isError) {
            this.label = label;
            this.isError = isError;
        }

        String formatToString(boolean isBytes, boolean isLines, boolean isWords) {
            if (isError) {
                return label;
            }

            return String.format(
                    "%s%s%s%s",
                    isLines ? String.format("%s%s", numLines, CHAR_TAB) : "",
                    isWords ? String.format("%s%s", numWords, CHAR_TAB) : "",
                    isBytes ? String.format("%s%s", numBytes, CHAR_TAB) : "",
                    label
            ).trim();
        }
    }

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
    @SuppressWarnings("PMD.PreserveStackTrace")
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws WcException {
        if (stdout == null) {
            throw new WcException(ERR_NO_OSTREAM);
        }

        WcArgsParser parser = new WcArgsParser();

        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new WcException(e.getMessage());
        }

        Boolean isDefault = parser.isDefault();
        Boolean isBytes = parser.isBytes() || isDefault;
        Boolean isLines = parser.isLines() || isDefault;
        Boolean isWords = parser.isWords() || isDefault;
        String[] fileNames = parser.getFileNames().toArray(String[]::new);

        if (stdin == null && (fileNames == null || fileNames.length == 0)) {
            throw new WcException(ERR_NO_INPUT);
        }

        String result = wcContent(
                isBytes,
                isLines,
                isWords,
                stdin,
                fileNames
        );

        try {
            stdout.write(result.getBytes());
            stdout.write(STRING_NEWLINE.getBytes());
        } catch (Exception e) {
            throw new WcException(ERR_WRITE_STREAM);
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

    private List<WcStatistics> computeAndAttachTotalStatistics(List<WcStatistics> statisticsList) {
        WcStatistics totalStatistics = new WcStatistics(TOTAL_LABEL);

        statisticsList.forEach(statistics -> {
            totalStatistics.numLines += statistics.numLines;
            totalStatistics.numWords += statistics.numWords;
            totalStatistics.numBytes += statistics.numBytes;
        });

        return Stream.concat(statisticsList.stream(), Stream.of(totalStatistics)).collect(Collectors.toList());
    }

    private WcStatistics computeStatisticsFromInputStream(
            String label,
            InputStream inputStream,
            Path filePath
    ) throws Exception {
        List<String> lines = IOUtils.getLinesFromInputStream(inputStream);

        WcStatistics statistics = new WcStatistics(label);
        statistics.numLines = lines.size();
        statistics.numWords = lines.stream().map(StringUtils::tokenize).flatMap(Stream::of).count();
        statistics.numBytes = filePath == null
                ? lines.stream().mapToInt(line -> line.getBytes().length).sum() + statistics.numLines
                : Files.size(filePath);

        return statistics;
    }

    @SuppressWarnings("PMD.PreserveStackTrace")
    private WcStatistics computeStatisticsFromFile(String fileName) throws WcException {
        if (fileName == null) {
            throw new WcException(ERR_NO_FILE_ARGS);
        }

        if (StringUtils.isBlank(fileName)) {
            throw new WcException(ERR_INVALID_FILE);
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
                throw new InvalidDirectoryException(trimmedFileName, ERR_READING_FILE);
            }

        } catch (Exception e) {
            return new WcStatistics(new WcException(e.getMessage()).getMessage(), true);
        }
    }

    private WcStatistics computeStatisticsFromStdin(InputStream stdin) throws WcException{
        if (stdin == null) {
            throw new WcException(ERR_NO_ISTREAM);
        }

        WcStatistics statistics;

        try {
            statistics = computeStatisticsFromInputStream(STDIN_LABEL, stdin, null);
        } catch (IOException e) {
            statistics = new WcStatistics(new WcException(ERR_READ_STREAM).getMessage(), true);
        } catch (Exception e) {
            statistics = new WcStatistics(new WcException(ERR_UNEXPECTED).getMessage(), true);
        }

        return statistics;
    }

    private String formatStatistics(
            List<WcStatistics> statisticsList,
            boolean isBytes,
            boolean isLines,
            boolean isWords
    ) {
        int numEntries = statisticsList.size();

        List<String> result = (numEntries > 1 ? computeAndAttachTotalStatistics(statisticsList) : statisticsList)
                .stream()
                .map(statistics -> statistics.formatToString(isBytes, isLines, isWords))
                .filter(Predicate.not(StringUtils::isBlank))
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
            throw new WcException(ERR_INVALID_FILE);
        }

        List<WcStatistics> result = new ArrayList<>();

        for (String fileName: sanitizedFileNames) {
            result.add(computeStatisticsFromFile(fileName));
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

        return computeStatisticsFromStdin(stdin).formatToString(isBytes, isLines, isWords).trim();
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
            throw new WcException(ERR_INVALID_FILE);
        }

        List<WcStatistics> result = new ArrayList<>();

        for (String fileName: sanitizedFileNames) {
            result.add(fileName.equals(STRING_STDIN_FLAG)
                            ? computeStatisticsFromStdin(stdin)
                            : computeStatisticsFromFile(fileName));
        }

        return formatStatistics(result, isBytes, isLines, isWords);
    }
}
