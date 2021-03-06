package sg.edu.nus.comp.cs4218.impl.result;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

public class LsResult extends Result {
    private String label = STRING_EMPTY;
    private List<File> files = List.of();

    public LsResult(String label, List<File> files) {
        super(false);

        if (label == null || files == null) {
            throw new IllegalArgumentException(ERR_NULL_ARGS);
        }

        this.label = label;
        this.files = List.copyOf(files);
    }

    public LsResult(String errorMessage) {
        super(true, errorMessage);
    }

    public List<File> getFiles() {
        return List.copyOf(files);
    }

    public String formatToString(boolean showLabel, boolean isSortByExt) {
        if (isError) {
            return STRING_EMPTY;
        }

        List<String> result = files.stream()
                .map(File::getName)
                .sorted(isSortByExt ? Comparator.comparing(StringUtils::getFileExtension).thenComparing(Comparator.naturalOrder()) : Comparator.naturalOrder())
                .collect(Collectors.toList());

        if (showLabel && !label.isEmpty()) {
            result = Stream.concat(Stream.of(
                    String.format(STRING_LABEL_VALUE_PAIR, label, STRING_EMPTY).stripTrailing()),
                    result.stream()
            ).collect(Collectors.toList());
        }

        return String.join(STRING_NEWLINE, result);
    }
}
