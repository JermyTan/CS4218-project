package sg.edu.nus.comp.cs4218.impl.parser;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_MISSING_ARG;

import java.util.List;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

public class MvArgsParser extends ArgsParser {
    private final static char FLAG_IS_NOT_OVERWRITE = 'n';

    public MvArgsParser() {
        super();

        legalFlags.add(FLAG_IS_NOT_OVERWRITE);
    }

    public boolean isNotOverwrite() {
        return flags.contains(FLAG_IS_NOT_OVERWRITE);
    }

    public String getDestFile() {
        return nonFlagArgs.get(nonFlagArgs.size() - 1);
    }

    public List<String> getSrcFiles() {
        return List.copyOf(nonFlagArgs.subList(0, nonFlagArgs.size()-1));
    }

    @Override
    protected void validateArgs() throws InvalidArgsException {
        super.validateArgs();

        if (nonFlagArgs.size() < 2) {
            throw new InvalidArgsException(ERR_MISSING_ARG);
        }
    }
}
