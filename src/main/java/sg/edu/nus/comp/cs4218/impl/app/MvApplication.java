package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.*;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.parser.*;
import sg.edu.nus.comp.cs4218.impl.util.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static java.nio.file.StandardCopyOption.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;

@SuppressWarnings("PMD.GodClass")
public class MvApplication implements MvInterface {

    public static String constructRenameErrorMsg(String srcFile, String destFile, String error) {
        return String.format("rename %s to %s: %s", srcFile, destFile, error);
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws MvException {
        MvArgsParser parser = new MvArgsParser();

        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new MvException(e.getMessage(), e);
        }

        boolean isOverwrite = !parser.isNotOverwrite();
        String destFile = parser.getDestFile();
        String[] srcFiles = parser.getSrcFiles().toArray(String[]::new);

        try {
            if (isFormatOne(destFile)) {
                if (srcFiles.length > 1) {
                    throw new InvalidArgsException(ERR_TOO_MANY_ARGS);
                }

                String srcFile = srcFiles[0];
                mvSrcFileToDestFile(isOverwrite, srcFile, destFile);

            } else {
                mvFilesToFolder(isOverwrite, destFile, srcFiles);
            }

        } catch (Exception e) {
            throw new MvException(e.getMessage(), e);
        }
    }

    @Override
    public String mvSrcFileToDestFile(Boolean isOverwrite, String srcFile, String destFile) throws Exception { //NOPMD
        if (CollectionUtils.isAnyNull(isOverwrite, srcFile, destFile)) {
            throw new Exception(ERR_NULL_ARGS);
        }

        Path srcPath = IOUtils.resolveAbsoluteFilePath(srcFile);
        Path destPath = IOUtils.resolveAbsoluteFilePath(destFile);

        // srcFile must exist
        if (Files.notExists(srcPath)) {
            throw new InvalidDirectoryException(srcFile, ERR_FILE_NOT_FOUND);
        }

        // Cannot rename a file/folder to a existing directory
        if (Files.isDirectory(destPath)) {
            throw new Exception(constructRenameErrorMsg(srcFile, destFile, ERR_IS_DIR));
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
                throw new Exception(constructRenameErrorMsg(srcFile, destFile, ERR_INVALID_ARGS));
            }
        }

        if (!isOverwrite && Files.exists(destPath)) {
            return null;
        }

        if (Files.exists(destPath) && !Files.isWritable(destPath)) {
            throw new Exception(constructRenameErrorMsg(srcFile, destFile, ERR_NO_PERM));
        }

        try {
            Files.move(srcPath, destPath, REPLACE_EXISTING);
        } catch (IOException e) {
            throw new Exception(constructRenameErrorMsg(srcFile, destFile, ERR_CANNOT_RENAME), e);
        }

        return null;
    }

    @Override
    public String mvFilesToFolder(Boolean isOverwrite, String destFolder, String... fileNames) throws Exception {
        if (isOverwrite == null || destFolder == null) {
            throw new Exception(ERR_NULL_ARGS);
        }

        if (fileNames == null || fileNames.length == 0) {
            throw new Exception(ERR_NO_FILE_ARGS);
        }

        if (CollectionUtils.isAnyNull((Object[]) fileNames)) {
            throw new Exception(ERR_INVALID_FILES);
        }

        if (!isOverwrite) {
            fileNames = filterSrcFiles(destFolder, fileNames);

            // Return if fileNames becomes empty after filtering
            if (fileNames.length == 0) {
                return null;
            }
        }

        Path destPath = IOUtils.resolveAbsoluteFilePath(destFolder);

        // `destFolder` must exist
        if (Files.notExists(destPath)) {
            throw new Exception(ERR_FILE_NOT_FOUND);
        }

        // `destFolder` must be a directory
        if (!Files.isDirectory(destPath)) {
            throw new Exception(ERR_IS_NOT_DIR);
        }

        for (String fileName : fileNames) {
            String destFile = destFolder + STRING_FILE_SEP + new File(fileName).getName();

            // Cannot move a file to its current directory
            Path srcFilePath = IOUtils.resolveAbsoluteFilePath(fileName);
            Path destFilePath = IOUtils.resolveAbsoluteFilePath(destFile);
            if (srcFilePath.equals(destFilePath)) {
                throw new Exception(String.format(ERR_ARE_IDENTICAL, fileName, destFile));
            }

            mvSrcFileToDestFile(isOverwrite, fileName, destFile);
        }

        return null;
    }

    /**
     * @param destFile target file
     * @param srcFiles file paths for files to be moved to the target directory
     * @return srcFiles that do not overwrite existing file in the target directory after mv
     */
    private String[] filterSrcFiles(String destFile, String... srcFiles) throws Exception {
        Path destPath = IOUtils.resolveAbsoluteFilePath(destFile);

        return Arrays.stream(srcFiles)
                .filter(srcFile -> {
                    String fileName = new File(srcFile).getName();
                    return Files.notExists(destPath.resolve(fileName));
                })
                .toArray(String[]::new);
    }

    private boolean isFormatOne(String destFile) throws Exception {
        Path target = IOUtils.resolveAbsoluteFilePath(destFile);
        return Files.notExists(target) || !Files.isDirectory(target);
    }
}
