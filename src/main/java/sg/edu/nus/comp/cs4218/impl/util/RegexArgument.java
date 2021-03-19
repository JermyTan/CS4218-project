package sg.edu.nus.comp.cs4218.impl.util;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_ASTERISK;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;

@SuppressWarnings("PMD.AvoidStringBufferField")
public final class RegexArgument {
    private final StringBuilder plaintext;
    private final StringBuilder regex;
    private boolean isRegex;

    public RegexArgument() {
        this.plaintext = new StringBuilder();
        this.regex = new StringBuilder();
        this.isRegex = false;
    }

    public RegexArgument(String str) {
        this();
        merge(str);
    }

    public void append(char chr) {
        plaintext.append(chr);
        regex.append(Pattern.quote(String.valueOf(chr)));
    }

    public void appendAsterisk() {
        plaintext.append(CHAR_ASTERISK);
        regex.append(String.format("%s%s%s", "[^", STRING_FILE_SEP, "]*"));
        isRegex = true;
    }

    public void merge(RegexArgument other) {
        plaintext.append(other.plaintext);
        regex.append(other.regex);
        isRegex = isRegex || other.isRegex;
    }

    public void merge(String str) {
        plaintext.append(str);
        regex.append(Pattern.quote(str));
    }

    public List<String> globFiles() {
        List<String> globbedFiles = new ArrayList<>();

        if (isRegex) {
            Pattern regexPattern = Pattern.compile(regex.toString());

            String modifiedPlaintext = plaintext.toString().replaceAll("\\\\", "/");
            String[] tokens = modifiedPlaintext.split("/");

            String dir = STRING_EMPTY;
            for (int i = 0; i < tokens.length - 1; i++) {
                dir += tokens[i] + STRING_FILE_SEP;
            }

            boolean isAbsolute = Paths.get(dir).isAbsolute();
            boolean onlyDirectories = modifiedPlaintext.charAt(plaintext.length() - 1) == '/';

            File currentDir = Paths.get(dir).toFile();
            if (!isAbsolute) {
                currentDir = Paths.get(EnvironmentUtil.currentDirectory, dir).normalize().toFile();
            }

            globbedFiles = traverseAndFilter(regexPattern, currentDir, isAbsolute, onlyDirectories);

            Collections.sort(globbedFiles);
        }

        if (globbedFiles.isEmpty()) {
            globbedFiles.add(plaintext.toString());
        }

        return globbedFiles;
    }


    /**
     * Traverses a given File node and returns a list of absolute path that match the given regexPattern.
     * <p>
     * Assumptions:
     * - ignores files and folders that we do not have access to (insufficient read permissions)
     * - regexPattern should not be null
     *
     * @param regexPattern    Pattern object
     * @param node            File object
     * @param isAbsolute      boolean option to indicate that the regexPattern refers to an absolute path
     * @param onlyDirectories boolean option to list only the directories
     */
    private List<String> traverseAndFilter(Pattern regexPattern, File node, boolean isAbsolute, boolean onlyDirectories) {
        List<String> matches = new ArrayList<>();
        if (regexPattern == null || !node.canRead() || !node.isDirectory()) {
            return matches;
        }
        for (String current : node.list()) {
            File nextNode = new File(node, current);
            String match = isAbsolute
                    ? nextNode.getAbsolutePath()
                    : nextNode.getAbsolutePath().substring(EnvironmentUtil.currentDirectory.length() + 1);
            // TODO: Find a better way to handle this.
            if (onlyDirectories && nextNode.isDirectory()) {
                match += STRING_FILE_SEP;
            }
            if (!nextNode.isHidden() && regexPattern.matcher(match).matches()) {
                matches.add(nextNode.getAbsolutePath());
            }
            matches.addAll(traverseAndFilter(regexPattern, nextNode, isAbsolute, onlyDirectories));
        }
        return matches;
    }

    public boolean isEmpty() {
        return plaintext.length() == 0;
    }

    @Override
    public String toString() {
        return plaintext.toString();
    }
}
