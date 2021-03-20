package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

public class CatArgsParser extends ArgsParser {
    private static final char FLAG_IS_LINE_NUMBER = 'n';

    public CatArgsParser() {
        super();

        legalFlags.add(FLAG_IS_LINE_NUMBER);
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

    public boolean isLineNumber() {
        return flags.contains(FLAG_IS_LINE_NUMBER);
    }

    public List<String> getFileNames() {
        return List.copyOf(nonFlagArgs);
    }
}
