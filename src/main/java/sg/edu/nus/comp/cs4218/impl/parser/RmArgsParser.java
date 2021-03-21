package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.*;

public class RmArgsParser extends ArgsParser {
    private final static char FLAG_IS_RECURSIVE = 'r';
    private final static char FLAG_IS_REMOVE_EMPTY_DIR = 'd';

    public RmArgsParser() {
        super();
        legalFlags.add(FLAG_IS_RECURSIVE);
        legalFlags.add(FLAG_IS_REMOVE_EMPTY_DIR);
    }

    public boolean isRecursive() {
        return flags.contains(FLAG_IS_RECURSIVE);
    }

    public boolean isRemoveEmptyDir() {
        return flags.contains(FLAG_IS_REMOVE_EMPTY_DIR);
    }

    public List<String> getFileNames() {
        return List.copyOf(nonFlagArgs);
    }
}
