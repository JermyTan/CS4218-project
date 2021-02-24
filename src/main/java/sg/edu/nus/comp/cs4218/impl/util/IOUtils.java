package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

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
    @SuppressWarnings("PMD.PreserveStackTrace")
    public static InputStream openInputStream(String fileName) throws ShellException {
        try {
            return Files.newInputStream(resolveFilePath(fileName));
        } catch (SecurityException e) {
            throw new ShellException(ERR_NO_PERM);
        } catch (Exception e) {
            throw new ShellException(e.getMessage());
        }
    }

    /**
     * Open an outputStream based on the file name.
     *
     * @param fileName string containing file name.
     * @return outputStream of file opened.
     * @throws ShellException if file destination is inaccessible.
     */
    @SuppressWarnings("PMD.PreserveStackTrace")
    public static OutputStream openOutputStream(String fileName) throws ShellException {
        try {
            return Files.newOutputStream(resolveFilePath(fileName));
        } catch (SecurityException e) {
            throw new ShellException(ERR_NO_PERM);
        } catch (Exception e) {
            throw new ShellException(e.getMessage());
        }
    }

    /**
     * Close an inputStream. If inputStream provided is System.in or null, it will be ignored.
     *
     * @param inputStream inputStream to be closed.
     * @throws ShellException if inputStream cannot be closed successfully.
     */
    @SuppressWarnings("PMD.PreserveStackTrace")
    public static void closeInputStream(InputStream inputStream) throws ShellException {
        if (inputStream == null || inputStream.equals(System.in)) {
            return;
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            throw new ShellException(ERR_CLOSING_STREAM);
        }
    }

    /**
     * Close an outputStream. If outputStream provided is System.out or null, it will be ignored.
     *
     * @param outputStream outputStream to be closed.
     * @throws ShellException if outputStream cannot be closed successfully.
     */
    @SuppressWarnings("PMD.PreserveStackTrace")
    public static void closeOutputStream(OutputStream outputStream) throws ShellException {
        if (outputStream == null || outputStream.equals(System.out)) {
            return;
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            throw new ShellException(ERR_CLOSING_STREAM);
        }
    }

    @SuppressWarnings("PMD.PreserveStackTrace")
    public static Path resolveFilePath(String fileName) throws Exception {
        if (fileName == null) {
            throw new Exception(ERR_NO_FILE_ARGS);
        }

        try {
            return Paths.get(Environment.currentDirectory).resolve(fileName);
        } catch (InvalidPathException e) {
            throw new Exception(ERR_INVALID_FILE);
        }
    }

    /**
     * Returns a list of lines based on the given InputStream.
     *
     * @param input inputStream containing arguments from System.in or FileInputStream
     * @throws Exception if there is error reading from input stream.
     */
    public static List<String> getLinesFromInputStream(InputStream input) throws Exception {
        List<String> output = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;
        while ((line = reader.readLine()) != null) {
            output.add(line);
        }
        reader.close();
        return output;
    }
}
