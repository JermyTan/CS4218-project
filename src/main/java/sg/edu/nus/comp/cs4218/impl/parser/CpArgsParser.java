package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class CpArgsParser extends ArgsParser {
    private final static char FLAG_IS_RECURSIVE_LOWERCASE = 'r';
    private final static char FLAG_IS_RECURSIVE_UPPERCASE = 'R';

    public CpArgsParser() {
        super();
        legalFlags.add(FLAG_IS_RECURSIVE_LOWERCASE);
        legalFlags.add(FLAG_IS_RECURSIVE_UPPERCASE);
    }

    public boolean isRecursive() {
        return flags.contains(FLAG_IS_RECURSIVE_LOWERCASE) || flags.contains(FLAG_IS_RECURSIVE_UPPERCASE);
    }

    public List<String> getFileOrFolderNames() {
        return List.copyOf(nonFlagArgs);
    }
}
