package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.CdInterface;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class CdApplication implements CdInterface {

    /**
     * Runs the cd application with the specified arguments.
     * Assumption: The application must take in one arg. (cd without args is not supported)
     *
     * @param args   array of arguments for the application.
     * @param stdin  an InputStream, not used.
     * @param stdout an OutputStream, not used.
     * @throws CdException if path is invalid.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws CdException {
        if (args == null) {
            throw new CdException(ERR_NULL_ARGS);
        }
        if (args.length == 0){
            throw new CdException(ERR_MISSING_ARG);
        }
        if (args.length > 1){
            throw new CdException(ERR_TOO_MANY_ARGS);
        }

        changeToDirectory(args[0]);
    }

    private String getNormalizedAbsolutePath(String pathStr) throws CdException {
        if (StringUtils.isBlank(pathStr)) {
            throw new CdException(ERR_MISSING_ARG);
        }

        try {
            Path path = IOUtils.resolveFilePath(pathStr);

            if (!Files.exists(path)) {
                throw new InvalidDirectoryException(pathStr, ERR_FILE_NOT_FOUND);
            }

            if (!Files.isDirectory(path)) {
                throw new InvalidDirectoryException(pathStr, ERR_IS_NOT_DIR);
            }

            return path.toString();

        } catch (ShellException | InvalidDirectoryException e) {
            throw new CdException(e.getMessage(), e);
        }
    }

    @Override
    public void changeToDirectory(String path) throws CdException {
        Environment.currentDirectory = getNormalizedAbsolutePath(path);
    }
}
