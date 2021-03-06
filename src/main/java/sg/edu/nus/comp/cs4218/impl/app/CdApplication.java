package sg.edu.nus.comp.cs4218.impl.app;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_NOT_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_MISSING_ARG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOO_MANY_ARGS;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.app.CdInterface;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

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
        if (args.length == 0) {
            throw new CdException(ERR_MISSING_ARG);
        }
        if (args.length > 1) {
            throw new CdException(ERR_TOO_MANY_ARGS);
        }

        changeToDirectory(args[0]);
    }

    private String getNormalizedAbsolutePath(String pathStr) throws CdException {
        try {
            Path path = IOUtils.resolveAbsoluteFilePath(pathStr);

            if (Files.notExists(path)) {
                throw new InvalidDirectoryException(pathStr, ERR_FILE_NOT_FOUND);
            }

            if (!Files.isDirectory(path)) {
                throw new InvalidDirectoryException(pathStr, ERR_IS_NOT_DIR);
            }

            if (!Files.isExecutable(path)) {
                throw new InvalidDirectoryException(pathStr, ERR_NO_PERM);
            }

            return path.toString();

        } catch (Exception e) {
            throw new CdException(e.getMessage(), e);
        }
    }

    @Override
    public void changeToDirectory(String path) throws CdException {
        EnvironmentUtil.currentDirectory = getNormalizedAbsolutePath(path);
    }
}
