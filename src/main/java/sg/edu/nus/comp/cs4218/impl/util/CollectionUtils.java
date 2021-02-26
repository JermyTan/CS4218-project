package sg.edu.nus.comp.cs4218.impl.util;

import java.util.Arrays;
import java.util.Objects;

public final class CollectionUtils {

    private CollectionUtils() {
    }

    /**
     * Checks if any items are null.
     *
     * @param items elements to be checked.
     * @return true if items is null or contain any elements that are null.
     */
    public static boolean isAnyNull(Object... items) {
        return items == null || Arrays.stream(items).anyMatch(Objects::isNull);
    }
}
