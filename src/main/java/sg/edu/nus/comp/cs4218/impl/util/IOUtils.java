package sg.edu.nus.comp.cs4218.impl.util;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_CLOSING_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_CREATE_STREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_FILES;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_FILE_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_READ_STREAM;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

public final class IOUtils {
    private IOUtils() {
    }

    /**
     * Open an inputStream based on the file name.
     *
     * @param fileName string containing file name.
     * @return inputStream of file opened.
     * @throws ShellException if file destination is inaccessible.
     */
    public static InputStream openInputStream(String fileName) throws ShellException {
        try {
            Path path = resolveAbsoluteFilePath(fileName);

            if (Files.notExists(path)) {
                throw new InvalidDirectoryException(fileName, ERR_FILE_NOT_FOUND);
            }

            return Files.newInputStream(path);
        } catch (SecurityException e) {
            throw new ShellException(ERR_NO_PERM, e);
        } catch (IOException e) {
            throw new ShellException(ERR_CREATE_STREAM, e);
        } catch (Exception e) {
            throw new ShellException(e.getMessage(), e);
        }
    }

    /**
     * Open an outputStream based on the file name.
     *
     * @param fileName string containing file name.
     * @return outputStream of file opened.
     * @throws ShellException if file destination is inaccessible.
     */
    public static OutputStream openOutputStream(String fileName) throws ShellException {
        try {
            return Files.newOutputStream(resolveAbsoluteFilePath(fileName));
        } catch (SecurityException e) {
            throw new ShellException(ERR_NO_PERM, e);
        } catch (IOException e) {
            throw new ShellException(ERR_CREATE_STREAM, e);
        } catch (Exception e) {
            throw new ShellException(e.getMessage(), e);
        }
    }

    /**
     * Close an inputStream. If inputStream provided is System.in or null, it will be ignored.
     *
     * @param inputStream inputStream to be closed.
     * @throws ShellException if inputStream cannot be closed successfully.
     */
    public static void closeInputStream(InputStream inputStream) throws ShellException {
        if (inputStream == null || inputStream.equals(System.in)) {
            return;
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            throw new ShellException(ERR_CLOSING_STREAM, e);
        }
    }

    /**
     * Close an outputStream. If outputStream provided is System.out or null, it will be ignored.
     *
     * @param outputStream outputStream to be closed.
     * @throws ShellException if outputStream cannot be closed successfully.
     */
    public static void closeOutputStream(OutputStream outputStream) throws ShellException {
        if (outputStream == null || outputStream.equals(System.out)) {
            return;
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            throw new ShellException(ERR_CLOSING_STREAM, e);
        }
    }

    public static Path resolveAbsoluteFilePath(String fileName) throws Exception {
        if (fileName == null) {
            throw new Exception(ERR_NO_FILE_ARGS);
        }

        try {
            return Path.of(EnvironmentUtil.currentDirectory).resolve(fileName).normalize();

        } catch (InvalidPathException e) {
            throw new Exception(ERR_INVALID_FILES, e);
        }
    }

    /**
     * Returns a list of lines based on the given InputStream.
     *
     * @param input inputStream containing arguments from System.in or FileInputStream
     * @throws Exception if there is error reading from input stream.
     */
    public static List<String> getLinesFromInputStream(InputStream input) throws Exception {
        if (input == null) {
            throw new Exception(ERR_NO_ISTREAM);
        }

        List<String> output = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                output.add(line);
            }
        } catch (IOException e) {
            throw new Exception(ERR_READ_STREAM, e);
        }

        try {
            reader.close();
        } catch (IOException e) {
            throw new Exception(ERR_CLOSING_STREAM, e);
        }

        return output;
    }
}
