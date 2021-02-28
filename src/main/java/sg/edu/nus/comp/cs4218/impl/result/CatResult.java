package sg.edu.nus.comp.cs4218.impl.result;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CatResult extends Result {
    private List<String> lines = List.of();

    public CatResult(List<String> lines) {
        super(false);

        if (lines == null) {
            throw new IllegalArgumentException(ERR_NULL_ARGS);
        }

        this.lines = List.copyOf(lines);
    }

    public CatResult(String errorMessage) {
        super(true, errorMessage);
    }

    public String formatToString(boolean isLineNumber) {
        if (isError) {
            return STRING_EMPTY;
        }

        List<String> result = lines;

        if (isLineNumber) {
            result = IntStream.rangeClosed(1, lines.size())
                    .mapToObj(index -> String.format("%s %s", index, lines.get(index - 1)))
                    .collect(Collectors.toList());
        }

        return String.join(STRING_NEWLINE, result);
    }
}
