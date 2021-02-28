package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

public class TeeArgsParser extends ArgsParser {
    private final static char FLAG_IS_APPEND = 'a';

    public TeeArgsParser() {
        super();

        legalFlags.add(FLAG_IS_APPEND);
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

    public boolean isAppend() {
        return flags.contains(FLAG_IS_APPEND);
    }

    public List<String> getFileNames() {
        return List.copyOf(nonFlagArgs);
    }
}
