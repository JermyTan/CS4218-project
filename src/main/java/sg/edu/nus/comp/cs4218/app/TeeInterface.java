package sg.edu.nus.comp.cs4218.app;

import java.io.InputStream;

import sg.edu.nus.comp.cs4218.Application;

public interface TeeInterface extends Application {

    /**
     * Reads from standard input and write to both the standard output and files
     *
     * @param isAppend  Boolean option to append the standard input to the contents of the input files
     * @param stdin     InputStream containing arguments from Stdin
     * @param fileNames Array of String of file names
     * @return
     * @throws Exception
     */
    String teeFromStdin(Boolean isAppend, InputStream stdin, String... fileNames) throws Exception;
}
