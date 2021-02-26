package sg.edu.nus.comp.cs4218.impl.parser;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_MISSING_ARG;

import java.util.List;
import java.util.stream.Collectors;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

public class GrepArgsParser extends ArgsParser {
    private final static char FLAG_IS_CASE_INSENSITIVE = 'i';
    private final static char FLAG_IS_COUNT_LINES = 'c';
    private final static char FLAG_IS_PREFIX_FILE_NAME = 'H';
    private final static int INDEX_PATTERN = 0;
    private final static int INDEX_FILES = 1;

    public GrepArgsParser() {
        super();

        legalFlags.add(FLAG_IS_CASE_INSENSITIVE);
        legalFlags.add(FLAG_IS_COUNT_LINES);
        legalFlags.add(FLAG_IS_PREFIX_FILE_NAME);
    }

    public boolean isCaseInsensitive() {
        return flags.contains(FLAG_IS_CASE_INSENSITIVE);
    }

    public boolean isCountLines() {
        return flags.contains(FLAG_IS_COUNT_LINES);
    }

    public boolean isPrefixFileName() {
        return flags.contains(FLAG_IS_PREFIX_FILE_NAME);
    }

    public String getPattern() {
        return nonFlagArgs.isEmpty() ? null : nonFlagArgs.get(INDEX_PATTERN);
    }

    public List<String> getFileNames() {
        return List.copyOf(nonFlagArgs.subList(INDEX_FILES, nonFlagArgs.size()));
    }

    @Override
    protected void validateArgs() throws InvalidArgsException {
        super.validateArgs();

        if (getPattern() == null) {
            throw new InvalidArgsException(ERR_MISSING_ARG);
        }
    }
}
