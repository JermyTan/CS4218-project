package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

public class WcArgsParser extends ArgsParser {
    private static final char FLAG_IS_BYTES = 'c';
    private static final char FLAG_IS_LINES = 'l';
    private static final char FLAG_IS_WORDS = 'w';

    public WcArgsParser() {
        super();

        legalFlags.add(FLAG_IS_BYTES);
        legalFlags.add(FLAG_IS_LINES);
        legalFlags.add(FLAG_IS_WORDS);
    }

    // allows null ars
    @Override
    public void parse(String... args) throws InvalidArgsException {
        if (args == null) {
            super.parse();
        } else {
            super.parse(args);
        }
    }

    public boolean isBytes() {
        return flags.contains(FLAG_IS_BYTES);
    }

    public boolean isLines() {
        return flags.contains(FLAG_IS_LINES);
    }

    public boolean isWords() {
        return flags.contains(FLAG_IS_WORDS);
    }

    public boolean isDefault() {
        return flags.isEmpty();
    }

    public List<String> getFileNames() {
        return List.copyOf(nonFlagArgs);
    }
}
