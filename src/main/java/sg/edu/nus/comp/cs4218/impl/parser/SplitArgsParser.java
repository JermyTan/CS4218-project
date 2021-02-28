package sg.edu.nus.comp.cs4218.impl.parser;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_OPTION_REQUIRES_ARGUMENT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOO_MANY_ARGS;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

public class SplitArgsParser extends ArgsParser {
    private final static char FLAG_SHOULD_SPLIT_BY_LINES = 'l';
    private final static char FLAG_SHOULD_SPLIT_BY_BYTES = 'b';
    private final static int INDEX_NO_OF_LINES_OR_BYTES = 0;
    private final static int INDEX_FILE = 0;
    private final static int INDEX_PREFIX = 1;

    public SplitArgsParser() {
        super();

        legalFlags.add(FLAG_SHOULD_SPLIT_BY_LINES);
        legalFlags.add(FLAG_SHOULD_SPLIT_BY_BYTES);
    }

    public Boolean shouldSplitByLines() {
        return flags.contains(FLAG_SHOULD_SPLIT_BY_LINES);
    }

    public Boolean shouldSplitByBytes() {
        return flags.contains(FLAG_SHOULD_SPLIT_BY_BYTES);
    }

    public Boolean hasOption() {
        return shouldSplitByBytes() || shouldSplitByLines();
    }

    public String getNoOfLinesOrBytes() {
        if (nonFlagArgs.isEmpty() || !hasOption()) {
            return null;
        }
        return nonFlagArgs.get(INDEX_NO_OF_LINES_OR_BYTES);
    }

    public Boolean hasNoOfLinesOrBytes() {
        return getNoOfLinesOrBytes() != null;
    }

    public String getFileName() {
        if (hasOption()) {
            if (nonFlagArgs.size() >= 2) {
                return nonFlagArgs.get(INDEX_FILE + 1);
            } else {
                return null;
            }
        } else {
            if (nonFlagArgs.size() >= 1) {
                return nonFlagArgs.get(INDEX_FILE);
            } else {
                return null;
            }
        }
    }

    public Boolean hasFileName() {
        return getFileName() != null;
    }

    public String getPrefix() {
        if (hasOption()) {
            if (nonFlagArgs.size() >= 3) {
                return nonFlagArgs.get(INDEX_PREFIX + 1);
            } else {
                return null;
            }
        } else {
            if (nonFlagArgs.size() >= 2) {
                return nonFlagArgs.get(INDEX_PREFIX);
            } else {
                return null;
            }
        }
    }

    public Boolean hasPrefix() {
        return getPrefix() != null;
    }

    @Override
    protected void validateArgs() throws InvalidArgsException {
        super.validateArgs();

        if (hasOption() && !hasNoOfLinesOrBytes()) {
            throw new InvalidArgsException(ERR_OPTION_REQUIRES_ARGUMENT);
        }

        if (hasOption() && nonFlagArgs.size() > 3) {
            throw new InvalidArgsException(ERR_TOO_MANY_ARGS);
        }

        if (!hasOption() && nonFlagArgs.size() > 2) {
            throw new InvalidArgsException(ERR_TOO_MANY_ARGS);
        }
    }
}
