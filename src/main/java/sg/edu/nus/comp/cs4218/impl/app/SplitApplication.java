package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_ILLEGAL_BYTE_COUNT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_ILLEGAL_LINE_COUNT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IO_EXCEPTION;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_FLAG;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.app.SplitInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.SplitException;
import sg.edu.nus.comp.cs4218.impl.parser.SplitArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

@SuppressWarnings("PMD.GodClass")
public class SplitApplication implements SplitInterface {

    public static final int DEFAULT_LINES = 1000;
    public static final String DEFAULT_PREFIX = "x";

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws SplitException {
        SplitArgsParser parser = new SplitArgsParser();

        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new SplitException(e.getMessage(), e);
        }

        boolean isSplitByBytes = parser.isSplitByBytes();
        String numOfLinesOrBytes = parser.getNumOfLinesOrBytes();
        String fileName = parser.getFileName();
        String prefix = Objects.requireNonNullElse(parser.getPrefix(), DEFAULT_PREFIX);

        if (isSplitByBytes) {
            if (fileName == null || fileName.equals(STRING_STDIN_FLAG)) {
                splitStdinByBytes(stdin, prefix, numOfLinesOrBytes);
            } else {
                splitFileByBytes(fileName, prefix, numOfLinesOrBytes);
            }
        } else {
            int linesPerFile;
            try {
                linesPerFile = Integer.parseInt(Objects.requireNonNullElse(numOfLinesOrBytes, String.valueOf(DEFAULT_LINES)));
            } catch (NumberFormatException e) {
                throw new SplitException(ERR_ILLEGAL_LINE_COUNT, e);
            }
            if (fileName == null || fileName.equals(STRING_STDIN_FLAG)) {
                splitStdinByLines(stdin, prefix, linesPerFile);
            } else {
                splitFileByLines(fileName, prefix, linesPerFile);
            }
        }
    }

    private String generateSuffix(int num) {
        StringBuilder suffix = new StringBuilder();
        int times = (num - 1) / 676;
        suffix.append("z".repeat(times));
        int remainingNum = num - 676 * times;
        remainingNum -= 1;
        char left = (char) ('a' + remainingNum / 26);
        char right = (char) ('a' + remainingNum % 26);
        suffix.append(left).append(right);
        return suffix.toString();
    }

    private int parseBytes(String string) throws SplitException {
        Pattern pattern = Pattern.compile("^(\\d+)([bkm]?)$");
        Matcher matcher = pattern.matcher(string);
        if (!matcher.matches()) {
            throw new SplitException(ERR_ILLEGAL_BYTE_COUNT);
        }
        int numOfBytes;
        try {
            numOfBytes = Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            throw new SplitException(ERR_ILLEGAL_BYTE_COUNT, e);
        }
        if (numOfBytes < 1) {
            throw new SplitException(ERR_ILLEGAL_BYTE_COUNT);
        }

        final String appendage = matcher.group(2);

        switch (appendage) {
        case STRING_EMPTY:
            return numOfBytes;
        case "b":
            return numOfBytes * 512;
        case "k":
            return numOfBytes * 1024;
        case "m":
            return numOfBytes * 1048576;
        default:
            throw new SplitException(ERR_ILLEGAL_BYTE_COUNT);
        }
    }

    @Override
    public void splitFileByLines(String fileName, String prefix, int linesPerFile) throws SplitException {
        try {
            Path filePath = IOUtils.resolveAbsoluteFilePath(fileName);
            if (Files.notExists(filePath)) {
                throw new SplitException(ERR_FILE_NOT_FOUND);
            }
            if (Files.isDirectory(filePath)) {
                throw new SplitException(ERR_IS_DIR);
            }
            splitStdinByLines(Files.newInputStream(filePath), prefix, linesPerFile);
        } catch (IOException | ShellException e) {
            throw new SplitException(ERR_IO_EXCEPTION, e);
        }
    }

    @Override
    public void splitFileByBytes(String fileName, String prefix, String bytesPerFile) throws SplitException {
        try {
            Path filePath = IOUtils.resolveAbsoluteFilePath(fileName);
            if (Files.notExists(filePath)) {
                throw new SplitException(ERR_FILE_NOT_FOUND);
            }
            if (Files.isDirectory(filePath)) {
                throw new SplitException(ERR_IS_DIR);
            }
            splitStdinByBytes(Files.newInputStream(filePath), prefix, bytesPerFile);
        } catch (IOException | ShellException e) {
            throw new SplitException(ERR_IO_EXCEPTION, e);
        }
    }

    @Override
    public void splitStdinByLines(InputStream stdin, String prefix, int linesPerFile) throws SplitException {
        if (stdin == null) {
            throw new SplitException(ERR_NO_ISTREAM);
        }

        if (linesPerFile < 1) {
            throw new SplitException(ERR_ILLEGAL_LINE_COUNT);
        }

        String validPrefix = prefix;

        if (prefix == null || StringUtils.isBlank(prefix)) {
            validPrefix = DEFAULT_PREFIX;
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
            PrintWriter writer = null;//NOPMD
            int linesRead = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (linesRead % linesPerFile == 0) {
                    if (writer != null) {
                        writer.close();
                    }
                    String fileName = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + validPrefix + generateSuffix(linesRead / linesPerFile + 1);
                    writer = new PrintWriter(fileName, StandardCharsets.UTF_8);
                }
                writer.println(line);
                linesRead++;
            }
            if (writer != null) {
                writer.close();
            }
            reader.close();
        } catch (IOException e) {
            throw new SplitException(ERR_IO_EXCEPTION, e);
        }
    }

    @Override
    public void splitStdinByBytes(InputStream stdin, String prefix, String bytesPerFile) throws SplitException {
        if (stdin == null) {
            throw new SplitException(ERR_NO_ISTREAM);
        }

        if (bytesPerFile == null) {
            throw new SplitException(ERR_NULL_ARGS);
        }

        String validPrefix = prefix;

        if (prefix == null || StringUtils.isBlank(prefix)) {
            validPrefix = DEFAULT_PREFIX;
        }

        try {
            int numOfBytesPerFile = parseBytes(bytesPerFile);
            byte[] buffer = new byte[numOfBytesPerFile];

            int pieceNo = 1;
            int bytesRead;
            while ((bytesRead = stdin.read(buffer, 0, numOfBytesPerFile)) != -1) {
                String fileName = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + validPrefix + generateSuffix(pieceNo);
                FileOutputStream outputStream = new FileOutputStream(fileName);//NOPMD
                outputStream.write(buffer, 0, bytesRead);
                outputStream.close();
                pieceNo++;
            }
        } catch (IOException e) {
            throw new SplitException(ERR_IO_EXCEPTION, e);
        }
    }
}
