package sg.edu.nus.comp.cs4218.impl.parser;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_OPTION_REQUIRES_ARGUMENT;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOO_MANY_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOO_MANY_OPTIONS;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

public class SplitArgsParser extends ArgsParser {
    private static final char FLAG_IS_SPLIT_BY_LINES = 'l';
    private static final char FLAG_IS_SPLIT_BY_BYTES = 'b';
    private static final int INDEX_OF_LINES_OR_BYTES = 0;
    private static final int INDEX_FILE = 0;
    private static final int INDEX_PREFIX = 1;

    public SplitArgsParser() {
        super();

        legalFlags.add(FLAG_IS_SPLIT_BY_LINES);
        legalFlags.add(FLAG_IS_SPLIT_BY_BYTES);
    }

    // allows null args
    @Override
    public void parse(String... args) throws InvalidArgsException {
        if (args == null) {
            super.parse();
        } else {
            super.parse(args);
        }
    }

    public boolean isSplitByLines() {
        return flags.contains(FLAG_IS_SPLIT_BY_LINES);
    }

    public boolean isSplitByBytes() {
        return flags.contains(FLAG_IS_SPLIT_BY_BYTES);
    }

    private boolean hasOption() {
        return isSplitByBytes() || isSplitByLines();
    }

    public String getNumOfLinesOrBytes() {
        if (nonFlagArgs.isEmpty() || !hasOption()) {
            return null;
        }
        return nonFlagArgs.get(INDEX_OF_LINES_OR_BYTES);
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

    @Override
    protected void validateArgs() throws InvalidArgsException {
        super.validateArgs();

        if (flags.size() > 1) {
            throw new InvalidArgsException(ERR_TOO_MANY_OPTIONS);
        }

        if (hasOption() && getNumOfLinesOrBytes() == null) {
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
