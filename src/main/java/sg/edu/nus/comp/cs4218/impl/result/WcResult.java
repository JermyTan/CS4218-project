package sg.edu.nus.comp.cs4218.impl.result;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_TAB;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;

public class WcResult extends Result {
    private String label = STRING_EMPTY;
    private long numLines = 0;
    private long numWords = 0;
    private long numBytes = 0;

    public WcResult(String label, long numLines, long numWords, long numBytes) {
        super(false);

        this.label = label;
        this.numLines = numLines;
        this.numWords = numWords;
        this.numBytes = numBytes;
    }

    public WcResult(String errorMessage) {
        super(true, errorMessage);
    }

    public long getNumLines() {
        return numLines;
    }

    public long getNumWords() {
        return numWords;
    }

    public long getNumBytes() {
        return numBytes;
    }

    public String formatToString(boolean isBytes, boolean isLines, boolean isWords) {
        if (isError) {
            return STRING_EMPTY;
        }

        return String.format(
                "%s%s%s%s",
                isLines ? String.format("%s%s", numLines, CHAR_TAB) : STRING_EMPTY,
                isWords ? String.format("%s%s", numWords, CHAR_TAB) : STRING_EMPTY,
                isBytes ? String.format("%s%s", numBytes, CHAR_TAB) : STRING_EMPTY,
                label
        ).trim();
    }
}
