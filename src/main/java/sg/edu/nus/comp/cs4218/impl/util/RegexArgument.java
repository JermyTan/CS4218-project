package sg.edu.nus.comp.cs4218.impl.util;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_ASTERISK;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;

import java.io.File;
import java.nio.file.Path;
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
        regex.append(String.format("%s%s%s", "[^", Pattern.quote(STRING_FILE_SEP), "]*"));
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
        if (!isRegex) {
            return List.of(plaintext.toString());
        }

        String modifiedPlaintext = plaintext.toString().replaceAll(Pattern.quote(STRING_FILE_SEP), "/");
        String[] tokens = modifiedPlaintext.split("/");
        String dir = STRING_EMPTY;
        for (int i = 0; i < tokens.length - 1; i++) {
            dir += tokens[i] + STRING_FILE_SEP;
        }

        boolean isOnlyDirectories = modifiedPlaintext.charAt(modifiedPlaintext.length() - 1) == CHAR_FILE_SEP;

        File currentDir = Path.of(dir).isAbsolute()
                ? Path.of(dir).toFile()
                : Path.of(EnvironmentUtil.currentDirectory, dir).normalize().toFile();

        Pattern regexPattern = Pattern.compile(regex.toString());
        List<String> globbedFiles = new ArrayList<>();

        if (currentDir.exists()) {
            for (File file : currentDir.listFiles()) {
                String filePathName = dir + file.getName();

                if (isOnlyDirectories && file.isDirectory()) {
                    filePathName += STRING_FILE_SEP;
                }

                if (!regexPattern.matcher(filePathName).matches()) {
                    continue;
                }

                globbedFiles.add(filePathName);
            }
        }

        Collections.sort(globbedFiles);

        if (globbedFiles.isEmpty()) {
            globbedFiles.add(plaintext.toString());
        }

        return globbedFiles;
    }

    public boolean isEmpty() {
        return plaintext.length() == 0;
    }

    @Override
    public String toString() {
        return plaintext.toString();
    }
}
