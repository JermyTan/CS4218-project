package sg.edu.nus.comp.cs4218.testutil;

import java.util.List;

public final class TestConstants {
    public static final String STRING_SINGLE_WORD = "Test";
    public static final String STRING_MULTI_WORDS = "This is a   test string";
    public static final String STRING_EMPTY = "";
    public static final String STRING_BLANK = "     ";
    public static final String STRING_LEADING_TRAILING_SPACES = "   Test  string  ";
    public static final String STRING_UNICODE = " Test 💩🌚😊👍🏻风和日丽 ";
    public static final String STRING_SPECIAL_CHARS = "*;|/\\/%:-_\n\t\r.,'";

    public static final List<String> STRING_LIST = List.of(
            STRING_SINGLE_WORD,
            STRING_MULTI_WORDS,
            STRING_EMPTY,
            STRING_BLANK,
            STRING_LEADING_TRAILING_SPACES,
            STRING_UNICODE,
            STRING_SPECIAL_CHARS
    );

    // file names
    public static final String STRING_FILE_TXT = "Test.txt";
    public static final String STRING_UNDERSCORE_FILE_TXT = "Hello_world_test_file.txt";
    public static final String STRING_SPACE_FILE_TXT = " Test file  .txt  ";
    public static final String STRING_NO_EXT_FILE = " build  ";
    public static final String STRING_FILE_JPG = "image.jpg";
    public static final String STRING_FILE_MD = "markdown.md";
    public static final String STRING_CUSTOM_EXT_FILE = "custom.custom";
    public static final String STRING_LEADING_PERIOD_FILE_TXT = ".test.txt";
    public static final String STRING_LEADING_PERIOD_NO_EXT_FILE = ".file";
    public static final String STRING_TRAILING_PERIOD_FILE = "test.";
    public static final String STRING_UNICODE_NAME_FILE = "Test 💩😅.txt";
    public static final String STRING_UNICODE_EXT_FILE = "Test.😈🌚";

    public static final List<String> FILE_LIST = List.of(
            STRING_FILE_TXT,
            STRING_UNDERSCORE_FILE_TXT,
            STRING_SPACE_FILE_TXT,
            STRING_NO_EXT_FILE,
            STRING_FILE_JPG,
            STRING_FILE_MD,
            STRING_CUSTOM_EXT_FILE,
            STRING_LEADING_PERIOD_FILE_TXT,
            STRING_LEADING_PERIOD_NO_EXT_FILE,
            STRING_TRAILING_PERIOD_FILE,
            STRING_UNICODE_NAME_FILE,
            STRING_UNICODE_EXT_FILE
    );

    public static final Exception EXCEPTION = new Exception(STRING_SINGLE_WORD);

    public static final String RESOURCES_PATH = "src/test/resources";
}
