package sg.edu.nus.comp.cs4218.app;

import sg.edu.nus.comp.cs4218.Application;

import java.io.InputStream;

public interface SplitInterface extends Application {

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
    void splitFileByLines(String fileName, String prefix, int linesPerFile)
            throws Exception;

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
    void splitFileByBytes(String fileName, String prefix, String bytesPerFile)
            throws Exception;

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
    void splitStdinByLines(InputStream stdin, String prefix, int linesPerFile)
            throws Exception;

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
    void splitStdinByBytes(InputStream stdin, String prefix, String bytesPerFile)
            throws Exception;
}
