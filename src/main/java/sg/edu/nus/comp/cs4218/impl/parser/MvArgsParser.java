package sg.edu.nus.comp.cs4218.impl.parser;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_MISSING_ARG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;

public class MvArgsParser extends ArgsParser {
    private final static char FLAG_IS_OVERWRITE = 'n';

    public MvArgsParser() {
        super();

        legalFlags.add(FLAG_IS_OVERWRITE);
    }

    public Boolean isOverwrite() {
        return !flags.contains(FLAG_IS_OVERWRITE);
    }

    public String getTarget() {
        if (nonFlagArgs.size() < 2) {
            return null;
        }

        return nonFlagArgs.get(nonFlagArgs.size() - 1);
    }

    public Boolean isFormatOne() {
        File target = new File(getTarget());
        return !target.isDirectory();
    }

    public List<String> getSrcFiles() {
        if (nonFlagArgs.size() < 2) {
            return null;
        }

        LinkedList<String> copy = new LinkedList<>(nonFlagArgs);
        copy.removeLast();
        return copy;
    }

    @Override
    protected void validateArgs() throws InvalidArgsException {
        super.validateArgs();

        if (nonFlagArgs.size() < 2) {
            throw new InvalidArgsException(ERR_MISSING_ARG);
        }

        if (isFormatOne() && nonFlagArgs.size() != 2) {
            throw new InvalidArgsException(ERR_SYNTAX);
        }
    }
}
