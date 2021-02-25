package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;
import java.util.stream.Collectors;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

public class CatArgsParser extends ArgsParser {
    private final static char FLAG_IS_LINE_NUMBER = 'n';

    public CatArgsParser() {
        super();

        legalFlags.add(FLAG_IS_LINE_NUMBER);
    }

    @Override
    public void parse(String... args) throws InvalidArgsException {
        if (args == null) {
            super.parse();
        } else {
            super.parse(args);
        }
    }

    public Boolean isLineNumber() {
        return flags.contains(FLAG_IS_LINE_NUMBER);
    }

    public List<String> getFileNames() {
        return nonFlagArgs.stream().collect(Collectors.toList());
    }
}