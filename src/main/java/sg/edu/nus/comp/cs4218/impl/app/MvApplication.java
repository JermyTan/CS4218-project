package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.MvInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.impl.parser.MvArgsParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class MvApplication implements MvInterface {

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        MvArgsParser parser = new MvArgsParser();

        try {
            parser.parse(args);

            boolean isOverwrite = parser.isOverwrite();
            String target = parser.getTarget();
            List<String> srcFiles = parser.getSrcFiles();
            if (parser.isFormatOne()) {
                String srcFile = srcFiles.get(0);

                if (!isOverwrite && new File(target).exists()) {
                    return;
                }

                mvSrcFileToDestFile(srcFile, target);
            } else {
                if (!isOverwrite) {
                    srcFiles = filterSrcFiles(srcFiles, target);
                }

                mvFilesToFolder(target, srcFiles.toArray(String[]::new));
            }
        } catch (Exception e) {
            throw new MvException(e.getMessage());
        }
    }

    @Override
    public String mvSrcFileToDestFile(String srcFile, String destFile) throws Exception {
        Path srcPath = Paths.get(srcFile);
        Path destPath = Paths.get(destFile);

        // srcFile must exist
        if (Files.notExists(srcPath)) {
            throw new Exception(ERR_FILE_NOT_FOUND);
        }

        // Cannot rename a file/folder to a existing directory
        if (destPath.toFile().isDirectory()) {
            throw new Exception(ERR_IS_DIR);
        }

        // Cannot rename a folder to an existing file
        if (srcPath.toFile().isDirectory() && destPath.toFile().isFile()) {
            throw new Exception(String.format("rename %s to %s: %s", srcFile, destFile, ERR_IS_NOT_DIR));
        }

        try {
            Files.move(srcPath, destPath, REPLACE_EXISTING);
        } catch (IOException e) {
            throw new Exception(ERR_IO_EXCEPTION);
        }

        return null;
    }

    @Override
    public String mvFilesToFolder(String destFolder, String... fileNames) throws Exception {
        Path destPath = Paths.get(destFolder);

        // `destFolder` must exist
        if (Files.notExists(destPath)) {
            throw new Exception(ERR_FILE_NOT_FOUND);
        }

        // `destFolder` must be a directory
        if (!destPath.toFile().isDirectory()) {
            throw new Exception(ERR_IS_NOT_DIR);
        }

        // There has to be at least one file
        if (fileNames.length == 0) {
            throw new Exception(ERR_MISSING_ARG);
        }

        for (String fileName : fileNames) {
            String destFile = destFolder + File.separator + new File(fileName).getName();
            mvSrcFileToDestFile(fileName, destFile);
        }

        return null;
    }

    /**
     *
     * @param srcFiles file paths for files to be moved to the target directory
     * @param target target directory
     * @return srcFiles that do not overwrite existing file in the target directory after mv
     */
    private List<String> filterSrcFiles(List<String> srcFiles, String target) {
        List<String> filtered = new ArrayList<>();
        for (String srcFile : srcFiles) {
            String fileName = new File(srcFile).getName();
            String destFile = target + File.separator + fileName;
            if (new File(destFile).exists()) {
                continue;
            }
            filtered.add(srcFile);
        }

        return filtered;
    }
}
