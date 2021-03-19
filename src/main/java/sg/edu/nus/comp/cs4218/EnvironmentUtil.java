package sg.edu.nus.comp.cs4218;

import java.nio.file.Files;
import java.nio.file.Path;

import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

public final class EnvironmentUtil {

    /**
     * Java VM does not support changing the current working directory.
     * For this reason, we use EnvironmentUtil.currentDirectory instead.
     */
    public static volatile String currentDirectory = System.getProperty("user.dir");


    private EnvironmentUtil() {
    }

    public static void setCurrentDirectory(String pathString) {
        try {
            Path newPath = IOUtils.resolveAbsoluteFilePath(pathString);
            if (Files.isDirectory(newPath)) {
                currentDirectory = newPath.toString();
            }
        } catch (ShellException e) {
            // do nth
        }
    }
}
