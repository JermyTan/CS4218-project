package sg.edu.nus.comp.cs4218.impl.result;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UniqResult extends Result {
    private List<String> lines = List.of();

    public UniqResult(List<String> lines) {
        super(false);

        if (lines == null) {
            throw new IllegalArgumentException(ERR_NULL_ARGS);
        }

        this.lines = List.copyOf(lines);
    }

    public UniqResult(String errorMessage) {
        super(true, errorMessage);
    }

    private void retrieveLinesProperties(List<Integer> adjCounts, List<String> distinctAdjLines) {
        String previousLine = null;
        int count = 0;

        for (String line : lines) {
            if (!line.equals(previousLine)) {
                if (previousLine != null) {
                    adjCounts.add(count);
                    distinctAdjLines.add(previousLine);
                }

                count = 0;
                previousLine = line;
            }

            count++;
        }

        if (previousLine != null && count > 0) {
            adjCounts.add(count);
            distinctAdjLines.add(previousLine);
        }
    }

    public String formatToString(boolean isCount, boolean isRepeated, boolean isAllRepeated) {
        if (isError) {
            return "";
        }

        List<Integer> adjCounts = new ArrayList<>();
        List<String> distinctAdjLines = new ArrayList<>();
        retrieveLinesProperties(adjCounts, distinctAdjLines);

        List<String> result = distinctAdjLines;

        if (isAllRepeated) {
            result = IntStream.range(0, distinctAdjLines.size())
                    .flatMap(index -> {
                        int count = adjCounts.get(index);
                        return count > 1
                                ? IntStream.iterate(index, IntUnaryOperator.identity()).limit(count)
                                : IntStream.empty();
                    })
                    .mapToObj(index -> distinctAdjLines.get(index))
                    .collect(Collectors.toList());

        } else if (isRepeated) {
            result = IntStream.range(0, distinctAdjLines.size())
                    .filter(index -> adjCounts.get(index) > 1)
                    .mapToObj(index -> isCount
                            ? String.format("%s %s", adjCounts.get(index), distinctAdjLines.get(index))
                            : distinctAdjLines.get(index))
                    .collect(Collectors.toList());

        } else if (isCount) {
            result = IntStream.range(0, distinctAdjLines.size())
                    .mapToObj(index -> String.format("%s %s", adjCounts.get(index), distinctAdjLines.get(index)))
                    .collect(Collectors.toList());
        }

        return String.join(STRING_NEWLINE, result);
    }
}
