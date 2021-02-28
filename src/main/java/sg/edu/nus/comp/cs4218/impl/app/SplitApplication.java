package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_ILLEGAL_BYTE_COUNT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_ILLEGAL_LINE_COUNT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IO_EXCEPTION;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.SplitInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.SplitException;
import sg.edu.nus.comp.cs4218.impl.parser.SplitArgsParser;

public class SplitApplication implements SplitInterface {

    private static final String DEFAULT_PREFIX = "x";
    private static final int DEFAULT_LINES = 1000;

    private static final String DASH = "-";

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws SplitException {
        SplitArgsParser parser = new SplitArgsParser();
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new SplitException(e.getMessage(), e);
        }
        Boolean shouldSplitByBytes = parser.shouldSplitByBytes();
        Boolean hasNoOfLinesOrBytes = parser.hasNoOfLinesOrBytes();
        String noOfLinesOrBytes = parser.getNoOfLinesOrBytes();
        Boolean hasFileName = parser.hasFileName();
        String fileName = parser.getFileName();
        Boolean hasPrefix = parser.hasPrefix();
        String prefix = hasPrefix ? parser.getPrefix() : DEFAULT_PREFIX;

        if (shouldSplitByBytes) {
            String bytesPerFile = noOfLinesOrBytes;
            if (!hasFileName || fileName.equals(DASH)) {
                splitStdinByBytes(stdin, prefix, bytesPerFile);
            } else {
                splitFileByBytes(fileName, prefix, bytesPerFile);
            }
        } else {
            int linesPerFile;
            try {
                linesPerFile = hasNoOfLinesOrBytes ? Integer.parseInt(noOfLinesOrBytes) : DEFAULT_LINES;
            } catch (NumberFormatException e) {
                throw new SplitException(ERR_ILLEGAL_LINE_COUNT, e);
            }
            if (linesPerFile < 1) {
                throw new SplitException(ERR_ILLEGAL_LINE_COUNT);
            }

            if (!hasFileName || fileName.equals(DASH)) {
                splitStdinByLines(stdin, prefix, linesPerFile);
            } else {
                splitFileByLines(fileName, prefix, linesPerFile);
            }
        }
    }

    private String generateSuffix(int num) {
        StringBuilder suffix = new StringBuilder();
        int times = num / 676;
        suffix.append("z".repeat(times));
        int remainingNum = num - 676 * times;
        remainingNum -= 1;
        char left = (char) ('a' + remainingNum / 26);
        char right = (char) ('a' + remainingNum % 26);
        suffix.append(left).append(right);
        return suffix.toString();
    }

    private int parseBytes(String string) throws SplitException {
        Pattern pattern = Pattern.compile("^(\\d+)([b,k,m]?)$");
        Matcher matcher = pattern.matcher(string);
        if (!matcher.matches()) {
            throw new SplitException(ERR_ILLEGAL_BYTE_COUNT);
        }
        int noOfBytes;
        try {
            noOfBytes = Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException e) {
            throw new SplitException(ERR_ILLEGAL_BYTE_COUNT, e);
        }
        if (noOfBytes < 1) {
            throw new SplitException(ERR_ILLEGAL_BYTE_COUNT);
        }
        String appendage = matcher.group(2);
        switch (appendage) {
        case "":
            return noOfBytes;
        case "b":
            return noOfBytes * 512;
        case "k":
            return noOfBytes * 1024;
        case "m":
            return noOfBytes * 1048576;
        default:
            throw new SplitException(ERR_ILLEGAL_BYTE_COUNT);
        }
    }

    /**
     * Split a file into fixed size pieces with specified number of
     * lines. Output splits naming convention: prefix + counter.
     * Default prefix is "x". Default counter is aa, ab, ..., zz,
     * zaa, zab, ..., zzz, zzaa, etc. For example: xaa, xab, etc.
     * This is the default option for 'split'.
     *
     * @param fileName     String of source file name
     * @param prefix       String of output file prefix (default is 'x')
     * @param linesPerFile Int of lines to have in the output file
     *                     (default is 1,000 lines)
     * @throws Exception
     */
    public void splitFileByLines(String fileName, String prefix, int linesPerFile) throws SplitException {
        try {
            splitStdinByLines(new FileInputStream(fileName), prefix, linesPerFile);
        } catch (FileNotFoundException e) {
            throw new SplitException(ERR_FILE_NOT_FOUND, e);
        }
    }

    /**
     * Split a file into fixed size pieces with specified number of
     * lines. Output splits naming convention: prefix + counter.
     * Default prefix is "x". Default counter is aa, ab, ..., zz,
     * zaa, zab, ..., zzz, zzaa, etc. For example: xaa, xab, etc.
     *
     * @param fileName     String of source file name
     * @param prefix       String of output file prefix (default is 'x')
     * @param bytesPerFile String of number of bytes of content to
     *                     fit into a file. Can have a suffix of either 'b', 'k', or 'm'.
     *                     Impact of suffix:
     *                     'b' - multiply the bytes by 512
     *                     'k' - multiply the bytes by 1024
     *                     'm' - multiply the bytes by 1048576
     * @throws Exception
     */
    public void splitFileByBytes(String fileName, String prefix, String bytesPerFile) throws SplitException {
        try {
            splitStdinByBytes(new FileInputStream(fileName), prefix, bytesPerFile);
        } catch (FileNotFoundException e) {
            throw new SplitException(ERR_FILE_NOT_FOUND, e);
        }
    }

    /**
     * Split input from stdin into fixed size pieces with specified number of
     * lines. Output splits naming convention: prefix + counter.
     * Default prefix is "x". Default counter is aa, ab, ..., zz,
     * zaa, zab, ..., zzz, zzaa, etc. For example: xaa, xab, etc.
     * This is the default option for 'split'.
     *
     * @param stdin        InputStream containing arguments from Stdin
     * @param prefix       String of output file prefix (default is 'x')
     * @param linesPerFile Int of lines to have in the output file
     *                     (default is 1,000 lines)
     * @throws Exception
     */
    public void splitStdinByLines(InputStream stdin, String prefix, int linesPerFile) throws SplitException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdin));
            PrintWriter writer = null;
            int linesRead = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (linesRead % linesPerFile == 0) {
                    if (writer != null) {
                        writer.close();
                    }
                    String fileName = Environment.currentDirectory + File.separator + prefix + generateSuffix(linesRead / linesPerFile + 1);
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

    /**
     * Split input from stdin into fixed size pieces with specified number of
     * lines. Output splits naming convention: prefix + counter.
     * Default prefix is "x". Default counter is aa, ab, ..., zz,
     * zaa, zab, ..., zzz, zzaa, etc. For example: xaa, xab, etc.
     *
     * @param stdin        InputStream containing arguments from Stdin
     * @param prefix       String of output file prefix (default is 'x')
     * @param bytesPerFile String of number of bytes of content to
     *                     fit into a file. Can have a suffix of either 'b', 'k', or 'm'.
     *                     Impact of suffix:
     *                     'b' - multiply the bytes by 512
     *                     'k' - multiply the bytes by 1024
     *                     'm' - multiply the bytes by 1048576
     * @throws Exception
     */
    public void splitStdinByBytes(InputStream stdin, String prefix, String bytesPerFile) throws SplitException {
        try {
            int noOfBytesPerFile = parseBytes(bytesPerFile);
            byte[] buffer = new byte[noOfBytesPerFile];

            int pieceNo = 1;
            int bytesRead;
            while ((bytesRead = stdin.read(buffer, 0, noOfBytesPerFile)) != -1) {
                String fileName = Environment.currentDirectory + File.separator + prefix + generateSuffix(pieceNo);
                FileOutputStream outputStream = new FileOutputStream(fileName);
                outputStream.write(buffer, 0, bytesRead);
                outputStream.close();
                pieceNo++;
            }
        } catch (IOException e) {
            throw new SplitException(ERR_IO_EXCEPTION, e);
        }
    }

}
