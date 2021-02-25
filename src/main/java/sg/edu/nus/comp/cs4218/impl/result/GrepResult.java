package sg.edu.nus.comp.cs4218.impl.result;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.util.List;
import java.util.stream.Collectors;

public class GrepResult extends Result {
    private String label = "";
    private List<String> lines = List.of();

    public GrepResult(String label, List<String> lines) {
        super(false);

        if (label == null || lines == null) {
            throw new IllegalArgumentException(ERR_NULL_ARGS);
        }

        this.label = label;
        this.lines = lines;
    }

    public GrepResult(String errorMessage) {
        super(true, errorMessage);
    }

    public String formatToString(boolean isCountLines, boolean isPrefixFileName) {
        if (isError) {
            return STRING_EMPTY;
        }

        String result;

        if (isCountLines) {
            String stringCount = String.valueOf(lines.size());
            result = isPrefixFileName ? String.format(STRING_LABEL_VALUE_PAIR, label, stringCount) : stringCount;
        } else {
            List<String> formattedLines = lines.stream()
                    .map(line -> isPrefixFileName ? String.format(STRING_LABEL_VALUE_PAIR, label, line) : line)
                    .collect(Collectors.toList());
            result = String.join(STRING_NEWLINE, formattedLines);
        }

        return result;
    }
}
