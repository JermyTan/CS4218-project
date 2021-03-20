package sg.edu.nus.comp.cs4218.impl.parser;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_TOO_MANY_ARGS;


import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

public class UniqArgsParser extends ArgsParser {
    private static final char FLAG_IS_COUNT = 'c';
    private static final char FLAG_IS_REPEATED = 'd';
    private static final char FLAG_IS_ALL_REPEATED = 'D';
    private static final int INDEX_INPUT_FILE = 0;
    private static final int INDEX_OUTPUT_FILE = 1;

    public UniqArgsParser() {
        super();

        legalFlags.add(FLAG_IS_COUNT);
        legalFlags.add(FLAG_IS_REPEATED);
        legalFlags.add(FLAG_IS_ALL_REPEATED);
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

    public boolean isCount() {
        return flags.contains(FLAG_IS_COUNT);
    }

    public boolean isRepeated() {
        return flags.contains(FLAG_IS_REPEATED);
    }

    public boolean isAllRepeated() {
        return flags.contains(FLAG_IS_ALL_REPEATED);
    }

    public String getInputFileName() {
        return nonFlagArgs.size() > 0 ? nonFlagArgs.get(INDEX_INPUT_FILE) : null;
    }

    public String getOutputFilename() {
        return nonFlagArgs.size() > 1 ? nonFlagArgs.get(INDEX_OUTPUT_FILE) : null;
    }

    @Override
    protected void validateArgs() throws InvalidArgsException {
        super.validateArgs();

        if (nonFlagArgs.size() > 2) {
            throw new InvalidArgsException(ERR_TOO_MANY_ARGS);
        }
    }
}
