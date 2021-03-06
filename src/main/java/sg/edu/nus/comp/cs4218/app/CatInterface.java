package sg.edu.nus.comp.cs4218.app;

import java.io.InputStream;

import sg.edu.nus.comp.cs4218.Application;

public interface CatInterface extends Application {
    /**
     * Returns string containing the content of the specified file
     *
     * @param isLineNumber Prefix lines with their corresponding line number starting from 1
     * @param fileNames    Array of String of file names
     * @return
     * @throws Exception
     */
    String catFiles(Boolean isLineNumber, String... fileNames) throws Exception;

    /**
     * Returns string containing the content of the standard input
     *
     * @param isLineNumber Prefix lines with their corresponding line number starting from 1
     * @param stdin        InputStream containing arguments from Stdin
     * @return
     * @throws Exception
     */
    String catStdin(Boolean isLineNumber, InputStream stdin)
            throws Exception;


    /**
     * Returns string containing the content of the standard input and specified file
     *
     * @param isLineNumber Prefix lines with their corresponding line number starting from 1
     * @param stdin        InputStream containing arguments from Stdin
     * @param fileNames    Array of String of file names
     * @return
     * @throws Exception
     */
    String catFileAndStdin(Boolean isLineNumber, InputStream stdin, String... fileNames) throws Exception;
}
