package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_MISSING_ARG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_TAB;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_STDIN_FLAG;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import sg.edu.nus.comp.cs4218.app.PasteInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.impl.parser.PasteArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

@SuppressWarnings("PMD.GodClass")
public class PasteApplication implements PasteInterface {
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws PasteException {
        PasteArgsParser parser = new PasteArgsParser();

        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new PasteException(e.getMessage(), e);
        }

        boolean isSerial = parser.isSerial();
        String[] fileNames = parser.getFileNames().toArray(String[]::new);

        if (stdin == null && (fileNames == null || fileNames.length == 0)) {
            throw new PasteException(ERR_NO_INPUT);
        }

        try {
            List<InputStream> streams = fileNamesToInputStreams(stdin, fileNames);
            String result = mergeInputStreams(isSerial, streams.toArray(InputStream[]::new));
            stdout.write(result.getBytes());
            stdout.write(STRING_NEWLINE.getBytes());
        } catch (PasteException e) {
            throw e;
        } catch (Exception e) {
            throw new PasteException(e.getMessage(), e);
        }
    }

    @Override
    public String mergeStdin(Boolean isSerial, InputStream stdin) throws Exception {
        try {
            if (isSerial == null) {
                throw new InvalidArgsException(ERR_MISSING_ARG);
            }
            if (stdin == null) {
                throw new InvalidArgsException(ERR_NO_ISTREAM);
            }
            InputStream[] streams = {stdin};
            return mergeInputStreams(isSerial, streams);
        } catch (Exception e) {
            throw new PasteException(e.getMessage(), e);
        }
    }

    @Override
    public String mergeFile(Boolean isSerial, String... fileNames) throws Exception {
        try {
            if (isSerial == null) {
                throw new InvalidArgsException(ERR_MISSING_ARG);
            }
            if (fileNames == null || fileNames.length == 0) {
                throw new InvalidArgsException(ERR_MISSING_ARG);
            }
            List<InputStream> streams = fileNamesToInputStreams(null, fileNames);
            return mergeInputStreams(isSerial, streams.toArray(InputStream[]::new));
        } catch (Exception e) {
            throw new PasteException(e.getMessage(), e);
        }
    }

    @Override
    public String mergeFileAndStdin(Boolean isSerial, InputStream stdin, String... fileNames) throws Exception {
        try {
            if (isSerial == null) {
                throw new InvalidArgsException(ERR_MISSING_ARG);
            }
            if (stdin == null) {
                throw new InvalidArgsException(ERR_NO_ISTREAM);
            }
            if (fileNames == null || fileNames.length == 0) {
                throw new InvalidArgsException(ERR_MISSING_ARG);
            }
            List<InputStream> streams = fileNamesToInputStreams(stdin, fileNames);
            streams.add(0, stdin);
            return mergeInputStreams(isSerial, streams.toArray(InputStream[]::new));
        } catch (Exception e) {
            throw new PasteException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("PMD.CloseResource")
    public String mergeInputStreams(Boolean isSerial, InputStream... streams) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        if (isSerial) {
            for (InputStream stream : streams) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line = reader.lines().collect(Collectors.joining(String.valueOf(CHAR_TAB)));
                stringBuilder.append(line.trim()).append(STRING_NEWLINE);
                reader.close();
                stream.close();
            }
        } else {
            boolean hasData = true;
            Map<InputStream, BufferedReader> streamToReader = new HashMap<>();
            for (InputStream stream : streams) {
                if (!streamToReader.containsKey(stream)) {
                    streamToReader.put(stream, new BufferedReader(new InputStreamReader(stream)));
                }
            }
            while (hasData) {
                StringBuilder line = new StringBuilder();
                hasData = false;
                for (InputStream stream : streams) {
                    BufferedReader reader = streamToReader.get(stream);
                    String element = reader.readLine();
                    if (element != null) {
                        line.append(element.trim());
                        hasData = true;
                    }
                    line.append('\t');
                }
                if (hasData) {
                    stringBuilder.append(line.toString().stripTrailing()).append(STRING_NEWLINE);
                }
            }
            for (InputStream stream : streams) {
                streamToReader.get(stream).close();
                stream.close();
            }
        }
        return stringBuilder.toString().trim();
    }

    public List<InputStream> fileNamesToInputStreams(InputStream stdin, String... fileNames) throws Exception {
        List<InputStream> streams = new ArrayList<>();
        if (fileNames == null || fileNames.length == 0) {
            streams.add(stdin);
            return streams;
        }
        for (String fileName : fileNames) {
            if (stdin != null && fileName.equals(STRING_STDIN_FLAG)) {
                streams.add(stdin);
            } else {
                Path filePath = IOUtils.resolveAbsoluteFilePath(fileName);
                if (Files.notExists(filePath)) {
                    throw new InvalidDirectoryException(fileName, ERR_FILE_NOT_FOUND);
                }
                if (Files.isDirectory(filePath)) {
                    throw new InvalidDirectoryException(fileName, ERR_IS_DIR);
                }
                streams.add(Files.newInputStream(filePath));
            }
        }
        return streams;
    }
}
