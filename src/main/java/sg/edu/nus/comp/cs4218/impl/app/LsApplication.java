package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.LsInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.LsException;
import sg.edu.nus.comp.cs4218.exception.InvalidDirectoryException;
import sg.edu.nus.comp.cs4218.impl.parser.LsArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_CURR_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class LsApplication implements LsInterface {

    private final static String PATH_CURR_DIR = STRING_CURR_DIR + CHAR_FILE_SEP;

    /**
     * Runs the ls application with the specified arguments.
     *
     * @param args array of arguments for the application.
     * @param stdin an InputStream, not used.
     * @param stdout an OutputStream. Contents in the given directories will be output to stdout.
     * @throws LsException if the file(s) specified do not exist or are unreadable.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws LsException {
        if (stdout == null) {
            throw new LsException(ERR_NO_OSTREAM);
        }

        LsArgsParser parser = new LsArgsParser();

        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new LsException(e.getMessage(), e);
        }

        boolean isFoldersOnly = parser.isFoldersOnly();
        boolean isRecursive = parser.isRecursive();
        boolean isSortByExt = parser.isSortByExt();
        String[] folderNames = parser.getFolderNames().toArray(String[]::new);

        String result = listFolderContent(isFoldersOnly, isRecursive, isSortByExt, folderNames);

        if (result.isEmpty()) {
            return;
        }

        try {
            stdout.write(result.getBytes());
            stdout.write(STRING_NEWLINE.getBytes());
        } catch (Exception e) {
            throw new LsException(ERR_WRITE_STREAM, e);
        }
    }

    @Override
    public String listFolderContent(Boolean isFoldersOnly, Boolean isRecursive, Boolean isSortByExt,
                                    String... folderNames) throws LsException {
        if (folderNames.length == 0 && !isRecursive) {
            return listCwdContent(isFoldersOnly, isSortByExt);
        }

        List<Path> paths;
        if (folderNames.length == 0) {
            String[] directories = new String[1];
            directories[0] = Environment.currentDirectory;
            paths = resolvePaths(directories);
        } else {
            paths = resolvePaths(folderNames);
        }

        return buildResult(paths, isFoldersOnly, isRecursive, isSortByExt);
    }

    /**
     * Lists only the current directory's content and RETURNS. This does not account for recursive
     * mode in cwd.
     *
     * @param isFoldersOnly
     * @param isSortByExt
     * @return current directory's content.
     */
    private String listCwdContent(Boolean isFoldersOnly, Boolean isSortByExt) throws LsException {
        String cwd = Environment.currentDirectory;
        return formatContents(getContents(Paths.get(cwd), isFoldersOnly), isSortByExt);
    }

    /**
     * Builds the resulting string to be written into the output stream.
     * <p>
     * NOTE: This is recursively called if user wants recursive mode.
     *
     * @param paths list of java.nio.Path objects to list
     * @param isFoldersOnly only list the folder contents
     * @param isRecursive recursive mode, repeatedly ls the child directories
     * @param isSortByExt sorts folder contents alphabetically by file extension (characters after the last ‘.’ (without quotes)). Files with no extension are sorted first.
     * @return string to be written to output stream.
     */
    private String buildResult(List<Path> paths, Boolean isFoldersOnly, Boolean isRecursive, Boolean isSortByExt) {
        // TODO: refactor and redirect error to stderr
        StringBuilder result = new StringBuilder();
        for (Path path : paths) {
            try {
                String relativePath = getRelativeToCwd(path).toString();
                result.append(StringUtils.isBlank(relativePath) ? PATH_CURR_DIR : relativePath);
                result.append(String.format(":%s", STRING_NEWLINE));

                List<Path> contents = getContents(path, isFoldersOnly);
                String formattedContents = formatContents(contents, isSortByExt);
                result.append(formattedContents);

                if (!formattedContents.isEmpty()) {
                    // Empty directories should not have an additional new line
                    result.append(STRING_NEWLINE);
                }
                result.append(STRING_NEWLINE);

                // RECURSE!
                if (isRecursive) {
                    result.append(buildResult(contents, isFoldersOnly, true, isSortByExt));
                }
            } catch (LsException e) {
                // NOTE: This is pretty hackish IMO - we should find a way to change this
                // If the user is in recursive mode, and if we resolve a file that isn't a directory
                // we should not spew the error message.
                //
                // However the user might have written a command like `ls invalid1 valid1 -R`, what
                // do we do then?
                if (!isRecursive) {
                    result.append(e.getMessage());
                    result.append(STRING_NEWLINE);
                }
            }
        }

        return result.toString().trim();
    }

    /**
     * Formats the contents of a directory into a single string.
     *
     * @param contents list of items in a directory
     * @param isSortByExt sorts folder contents alphabetically by file extension (characters after the last ‘.’ (without quotes)). Files with no extension are sorted first.
     * @return formatted directory's contents.
     */
    private String formatContents(List<Path> contents, Boolean isSortByExt) {
        List<String> fileNames = contents.stream()
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toList());

        if (isSortByExt) {
            fileNames.sort(Comparator.comparing(StringUtils::getFileExtension).thenComparing(Comparator.naturalOrder()));
        }

        return String.join(STRING_NEWLINE, fileNames);
    }

    /**
     * Gets the contents in a single specified directory.
     *
     * @param directory
     * @return list of files + directories in the passed directory.
     */
    private List<Path> getContents(Path directory, Boolean isFoldersOnly) throws LsException {
        try {
            if (!Files.exists(directory)) {
                throw new InvalidDirectoryException(getRelativeToCwd(directory).toString(), ERR_FILE_NOT_FOUND);
            }

            if (!Files.isDirectory(directory)) {
                throw new InvalidDirectoryException(getRelativeToCwd(directory).toString(), ERR_IS_NOT_DIR);
            }

            File[] files = directory.toFile().listFiles();
            return Arrays.stream(files == null ? new File[0] : files)
                    .filter(file -> !file.isHidden() && (!isFoldersOnly || file.isDirectory()))
                    .map(File::toPath)
                    .sorted()
                    .collect(Collectors.toList());
        } catch (InvalidDirectoryException e) {
            throw new LsException(e.getMessage(), e);
        }
    }

    /**
     * Resolve all paths given as arguments into a list of Path objects for easy path management.
     *
     * @param directories
     * @return list of java.nio.Path objects.
     */
    private List<Path> resolvePaths(String... directories) {
        return Arrays.stream(directories).map(this::resolvePath).collect(Collectors.toList());
    }

    /**
     * Converts a String into a java.nio.Path objects. Also resolves if the current path provided
     * is an absolute path.
     *
     * @param directory
     * @return path of directory.
     */
    private Path resolvePath(String directory) {
        if (directory.charAt(0) == '/') {
            // This is an absolute path
            return Paths.get(directory).normalize();
        }

        return Paths.get(Environment.currentDirectory, directory).normalize();
    }

    /**
     * Converts a path to a relative path to the current directory.
     *
     * @param path
     * @return relative path.
     */
    private Path getRelativeToCwd(Path path) {
        return Paths.get(Environment.currentDirectory).relativize(path);
    }
}
