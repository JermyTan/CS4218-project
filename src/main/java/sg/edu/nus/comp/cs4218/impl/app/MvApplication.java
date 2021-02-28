package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.MvInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.parser.MvArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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

                Path targetPath = IOUtils.resolveAbsoluteFilePath(target);
                if (!isOverwrite && Files.exists(targetPath)) {
                    return;
                }

                mvSrcFileToDestFile(srcFile, target);
            } else {
                if (!isOverwrite) {
                    srcFiles = filterSrcFiles(srcFiles, target);

                    // Return if srcFiles becomes empty after filtering
                    if (srcFiles.isEmpty()) {
                        return;
                    }
                }

                mvFilesToFolder(target, srcFiles.toArray(String[]::new));
            }
        } catch (Exception e) {
            throw new MvException(e.getMessage(), e);
        }
    }

    @Override
    public String mvSrcFileToDestFile(String srcFile, String destFile) throws Exception {
        Path srcPath = IOUtils.resolveAbsoluteFilePath(srcFile);
        Path destPath = IOUtils.resolveAbsoluteFilePath(destFile);

        // srcFile must exist
        if (Files.notExists(srcPath)) {
            throw new Exception(constructRenameErrorMsg(srcFile, destFile, ERR_FILE_NOT_FOUND));
        }

        // Cannot rename a file/folder to a existing directory
        if (Files.isDirectory(destPath)) {
            throw new Exception(ERR_IS_DIR);
        }

        // When renaming a file, destFile must belong to a existing directory
        if (srcPath.toFile().isFile() && Files.notExists(destPath.toAbsolutePath().getParent())) {
            throw new Exception(constructRenameErrorMsg(srcFile, destFile, ERR_FILE_NOT_FOUND));
        }


        if (Files.isDirectory(srcPath)) {
            // Cannot rename a folder to an existing file
            if (destPath.toFile().isFile()) {
                throw new Exception(constructRenameErrorMsg(srcFile, destFile, ERR_IS_NOT_DIR));
            }

            // When renaming a folder, destFolder must not contain srcFile
            if (destPath.toAbsolutePath().startsWith(srcPath.toAbsolutePath())) {
                throw new Exception(constructRenameErrorMsg(srcFile, destFile, ERR_INVALID_ARG));
            }
        }

        try {
            Files.move(srcPath, destPath, REPLACE_EXISTING);
        } catch (IOException e) {
            throw new Exception(constructRenameErrorMsg(srcFile, destFile, ERR_CANNOT_RENAME), e);
        }

        return null;
    }

    @Override
    public String mvFilesToFolder(String destFolder, String... fileNames) throws Exception {
        Path destPath = IOUtils.resolveAbsoluteFilePath(destFolder);

        // `destFolder` must exist
        if (Files.notExists(destPath)) {
            throw new Exception(ERR_FILE_NOT_FOUND);
        }

        // `destFolder` must be a directory
        if (!Files.isDirectory(destPath)) {
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
    private List<String> filterSrcFiles(List<String> srcFiles, String target) throws ShellException {
        List<String> filtered = new ArrayList<>();
        Path targetPath = IOUtils.resolveAbsoluteFilePath(target);

        for (String srcFile : srcFiles) {
            String fileName = new File(srcFile).getName();
            Path destPath = targetPath.resolve(fileName);
            if (Files.exists(destPath)) {
                continue;
            }
            filtered.add(srcFile);
        }

        return filtered;
    }

    private String constructRenameErrorMsg(String srcFile, String destFile, String error) {
        return String.format("rename %s to %s: %s", srcFile, destFile, error);
    }
}
