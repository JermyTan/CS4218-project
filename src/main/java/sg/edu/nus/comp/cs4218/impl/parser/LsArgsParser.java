package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

public class LsArgsParser extends ArgsParser {
    private final static char FLAG_IS_RECURSIVE = 'R';
    private final static char FLAG_IS_FOLDERS = 'd';
    private final static char FLAG_IS_SORT_BY_EXT = 'X';

    public LsArgsParser() {
        super();

        legalFlags.add(FLAG_IS_FOLDERS);
        legalFlags.add(FLAG_IS_RECURSIVE);
        legalFlags.add(FLAG_IS_SORT_BY_EXT);
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

    public boolean isFoldersOnly() {
        return flags.contains(FLAG_IS_FOLDERS);
    }

    public boolean isRecursive() {
        return flags.contains(FLAG_IS_RECURSIVE);
    }

    public boolean isSortByExt() {
        return flags.contains(FLAG_IS_SORT_BY_EXT);
    }

    public List<String> getFolderNames() {
        return List.copyOf(nonFlagArgs);
    }
}
