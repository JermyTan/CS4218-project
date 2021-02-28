package sg.edu.nus.comp.cs4218.impl.util;

public final class ErrorConstants {
    // Streams related
    public static final String ERR_CREATE_STREAM = "Could not create stream";
    public static final String ERR_WRITE_STREAM = "Could not write to output stream";
    public static final String ERR_READ_STREAM = "Could not read from input stream";
    public static final String ERR_CLOSING_STREAM = "Unable to close stream";
    public static final String ERR_MULTIPLE_STREAMS = "Multiple streams provided";
    public static final String ERR_STREAM_CLOSED = "Stream is closed";
    public static final String ERR_NO_OSTREAM = "OutputStream not provided";
    public static final String ERR_NO_ISTREAM = "InputStream not provided";
    public static final String ERR_NO_INPUT = "No inputStream and no filename(s)";
    public static final String ERR_NO_FILE_ARGS = "No file(s) provided";

    // Arguments related
    public static final String ERR_MISSING_ARG = "Missing argument(s)";
    public static final String ERR_NULL_ARGS = "Null argument(s)";
    public static final String ERR_TOO_MANY_ARGS = "Too many arguments";
    public static final String ERR_INVALID_ARG = "Invalid argument";

    // Options related
    public static final String ERR_OPTION_REQUIRES_ARGUMENT = "Option requires an argument";
    public static final String ERR_TOO_MANY_OPTIONS = "Too many options";

    // Files and folders related
    public static final String ERR_FILE_NOT_FOUND = "No such file or directory";
    public static final String ERR_READING_FILE = "Could not read file";
    public static final String ERR_IS_DIR = "Is a directory";
    public static final String ERR_IS_NOT_DIR = "Not a directory";
    public static final String ERR_NO_PERM = "Permission denied";

    // `date` related
    public static final String ERR_INVALID_FORMAT_PREFIX = "Invalid format. Date format must start with '+'";
    public static final String ERR_INVALID_FORMAT_FIELD = "Invalid format. Missing or unknown character after '%'";
    public static final String ERR_MISSING_FIELD = "Invalid format";

    // `find` related
    public static final String ERR_INVALID_FILES = "Invalid file(s)";
    public static final String ERR_NAME_FLAG = "Paths must precede -name";

    // `sed` related
    public static final String ERR_NO_REP_RULE = "No replacement rule supplied";
    public static final String ERR_INVALID_REP_RULE = "Invalid replacement rule";
    public static final String ERR_INVALID_REP_X = "X needs to be a number greater than 0";
    public static final String ERR_INVALID_REGEX = "Invalid regex pattern";
    public static final String ERR_EMPTY_REGEX = "Regular expression cannot be empty";

    // `grep` related
    public static final String ERR_NO_REGEX = "No regex pattern supplied";

    // `mkdir` related
    public static final String ERR_NO_FOLDERS = "No folder names are supplied";
    public static final String ERR_FILE_EXISTS = "File or directory already exists";
    public static final String ERR_TOP_LEVEL_MISSING = "Top level folders do not exist";

    // `split` related
    public static final String ERR_ILLEGAL_LINE_COUNT = "Illegal line count";
    public static final String ERR_ILLEGAL_BYTE_COUNT = "Illegal byte count";

    // `mv` related
    public static final String ERR_CANNOT_RENAME = "Failed to rename";

    // General constants
    public static final String ERR_INVALID_APP = "Invalid app";
    public static final String ERR_NOT_SUPPORTED = "Not supported yet";
    public static final String ERR_SYNTAX = "Invalid syntax";
    public static final String ERR_GENERAL = "Exception caught";
    public static final String ERR_IO_EXCEPTION = "IOException";
    public static final String ERR_UNEXPECTED = "Unexpected error occurred";

    private ErrorConstants() {
    }
}
