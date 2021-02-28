package sg.edu.nus.comp.cs4218.testutil;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FLAG_PREFIX;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class ParserTestUtils {
    private static boolean isOptions(String string) {
        return string.length() > 1 && string.charAt(0) == CHAR_FLAG_PREFIX;
    }

    public static List<String> removeOptions(String[] args) {
        return Arrays.stream(args)
                .filter(Predicate.not(ParserTestUtils::isOptions))
                .collect(Collectors.toList());
    }

    public static List<String> dropFirst(List<String> args, int n) {
        int start = Math.min(args.size(), Math.max(n, 0));
        return List.copyOf(args.subList(start, args.size()));
    }

    public static List<String> dropLast(List<String> args, int n) {
        int end = Math.max(0, args.size() - Math.max(n, 0));
        return List.copyOf(args.subList(0, end));
    }
}
