package sg.edu.nus.comp.cs4218.impl.app;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_ARE_IDENTICAL;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_CANNOT_RENAME;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_FILES;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_NOT_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_FILE_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOO_MANY_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import sg.edu.nus.comp.cs4218.app.MvInterface;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.parser.MvArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.CollectionUtils;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

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

        } catch (MvException e) {
            throw e;
        } catch (Exception e) {
            throw new MvException(e.getMessage(), e);
        }
    }

    @Override
    public String mvSrcFileToDestFile(Boolean isOverwrite, String srcFile, String destFile) throws MvException {
        if (CollectionUtils.isAnyNull(isOverwrite, srcFile, destFile)) {
            throw new MvException(ERR_NULL_ARGS);
        }

        renameFile(isOverwrite, srcFile, destFile);

        return null;
    }

    @Override
    public String mvFilesToFolder(Boolean isOverwrite, String destFolder, String... fileNames) throws MvException {
        if (isOverwrite == null || destFolder == null) {
            throw new MvException(ERR_NULL_ARGS);
        }

        if (fileNames == null || fileNames.length == 0) {
            throw new MvException(ERR_NO_FILE_ARGS);
        }

        if (CollectionUtils.isAnyNull((Object[]) fileNames)) {
            throw new MvException(ERR_INVALID_FILES);
        }

        if (fileNames.length == 0) {
            return null;
        }

        try {
            Path destPath = IOUtils.resolveAbsoluteFilePath(destFolder);

            // `destFolder` must exist
            if (Files.notExists(destPath)) {
                throw new MvException(ERR_FILE_NOT_FOUND);
            }

            // `destFolder` must be a directory
            if (!Files.isDirectory(destPath)) {
                throw new MvException(ERR_IS_NOT_DIR);
            }

            for (String fileName : fileNames) {
                moveFile(isOverwrite, destFolder, fileName);
            }
        } catch (MvException e) {
            throw e;
        } catch (Exception e) {
            throw new MvException(e.getMessage(), e);
        }

        return null;
    }

    private boolean isFormatOne(String destFile) throws Exception {
        Path target = IOUtils.resolveAbsoluteFilePath(destFile);
        return Files.notExists(target) || !Files.isDirectory(target);
    }

    private void renameFile(boolean isOverwrite, String srcFile, String destFile) throws MvException {
        try {
            Path srcPath = IOUtils.resolveAbsoluteFilePath(srcFile);
            Path destPath = IOUtils.resolveAbsoluteFilePath(destFile);

            // srcFile must exist
            if (Files.notExists(srcPath)) {
                throw new MvException(new InvalidDirectoryException(srcFile, ERR_FILE_NOT_FOUND).getMessage());
            }

            // When renaming a file, destFile must belong to a existing directory
            if (srcPath.toFile().isFile() && Files.notExists(destPath.toAbsolutePath().getParent())) {
                throw new MvException(constructRenameErrorMsg(srcFile, destFile, ERR_FILE_NOT_FOUND));
            }

            if (Files.isDirectory(srcPath)) {
                // Cannot rename a folder to an existing file
                if (destPath.toFile().isFile()) {
                    throw new MvException(constructRenameErrorMsg(srcFile, destFile, ERR_IS_NOT_DIR));
                }

                // When renaming a folder, destFolder must not contain srcFile
                if (destPath.toAbsolutePath().startsWith(srcPath.toAbsolutePath())) {
                    throw new MvException(constructRenameErrorMsg(srcFile, destFile, ERR_INVALID_ARGS));
                }
            }

            if (!isOverwrite && Files.exists(destPath)) {
                return;
            }

            if (Files.exists(destPath) && !Files.isWritable(destPath)) {
                throw new MvException(constructRenameErrorMsg(srcFile, destFile, ERR_NO_PERM));
            }

            try {
                Files.move(srcPath, destPath, REPLACE_EXISTING);
            } catch (IOException e) {
                throw new MvException(constructRenameErrorMsg(srcFile, destFile, ERR_CANNOT_RENAME), e);
            }

        } catch (MvException e) {
            throw e;
        } catch (Exception e) {
            throw new MvException(e.getMessage(), e);
        }
    }

    private void moveFile(boolean isOverwrite, String destFolder, String fileName) throws MvException {
        try {
            String destFile = destFolder + STRING_FILE_SEP + new File(fileName).getName();

            Path srcFilePath = IOUtils.resolveAbsoluteFilePath(fileName);
            Path destFilePath = IOUtils.resolveAbsoluteFilePath(destFile);

            // Cannot move a file to its current directory
            if (srcFilePath.equals(destFilePath)) {
                throw new MvException(String.format(ERR_ARE_IDENTICAL, fileName, destFile));
            }

            renameFile(isOverwrite, fileName, destFile);

        } catch (MvException e) {
            throw e;
        } catch (Exception e) {
            throw new MvException(e.getMessage(), e);
        }
    }
}
