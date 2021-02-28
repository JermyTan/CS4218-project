package sg.edu.nus.comp.cs4218.impl.parser;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public boolean isOverwrite() {
        return !flags.contains(FLAG_IS_OVERWRITE);
    }

    public String getTarget() {
        if (nonFlagArgs.size() < 2) {
            return null;
        }

        return nonFlagArgs.get(nonFlagArgs.size() - 1);
    }

    public boolean isFormatOne() throws ShellException {
        Path target = IOUtils.resolveAbsoluteFilePath(getTarget());
        return !Files.isDirectory(target);
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

        try {
            if (isFormatOne() && nonFlagArgs.size() != 2) {
                throw new InvalidArgsException(ERR_SYNTAX);
            }
        } catch (ShellException e) {
            throw new InvalidArgsException(ERR_SYNTAX);
        }
    }
}
