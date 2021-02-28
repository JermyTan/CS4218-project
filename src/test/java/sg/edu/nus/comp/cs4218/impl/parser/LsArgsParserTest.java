package sg.edu.nus.comp.cs4218.impl.parser;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FLAG_PREFIX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.multiplyChar;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.FILE_LIST;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_SPACE_FILE_TXT;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_UNICODE_NAME_FILE;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.testutil.ParserTestUtils;

class LsArgsParserTest {
    private final static char FLAG_IS_RECURSIVE = 'R';
    private final static char FLAG_IS_FOLDERS = 'd';
    private final static char FLAG_IS_SORT_BY_EXT = 'X';
    private final static String VALID_OPTION_1 = String.format("%s%s", CHAR_FLAG_PREFIX, FLAG_IS_RECURSIVE);
    private final static String VALID_OPTION_2 = String.format("%s%s", CHAR_FLAG_PREFIX, multiplyChar(FLAG_IS_FOLDERS, 3));
    private final static String VALID_OPTION_3 = String.format("%s%s%s%s", CHAR_FLAG_PREFIX, FLAG_IS_FOLDERS, FLAG_IS_RECURSIVE, FLAG_IS_SORT_BY_EXT);
    private final static String INVALID_OPTION_1 = multiplyChar(CHAR_FLAG_PREFIX, 2);
    private final static String INVALID_OPTION_2 = String.format("%s%s%s", CHAR_FLAG_PREFIX, FLAG_IS_RECURSIVE, CHAR_FLAG_PREFIX);
    private final static String INVALID_OPTION_3 = String.format("%s%s%s", CHAR_FLAG_PREFIX, FLAG_IS_FOLDERS, FLAG_IS_SORT_BY_EXT + 1);
    private final static String[] VALID_SINGLE_VALUE_ARGS_1 = new String[] {String.valueOf(CHAR_FLAG_PREFIX)};
    private final static String[] VALID_SINGLE_VALUE_ARGS_2 = new String[] {STRING_SPACE_FILE_TXT};
    private final static String[] VALID_SINGLE_VALUE_ARGS_3 = new String[] {VALID_OPTION_2};
    private final static String[] VALID_MULTI_VALUES_ARGS_1 = FILE_LIST.toArray(String[]::new);
    private final static String[] VALID_MULTI_VALUES_ARGS_2 = new String[] {VALID_OPTION_1, STRING_UNICODE_NAME_FILE, STRING_SPACE_FILE_TXT};
    private final static String[] VALID_MULTI_VALUES_ARGS_3 = new String[] {STRING_SPACE_FILE_TXT, VALID_OPTION_3, STRING_UNICODE_NAME_FILE, VALID_OPTION_2};
    private final static String[] VALID_MULTI_VALUES_ARGS_4 = new String[] {String.valueOf(CHAR_FLAG_PREFIX), String.valueOf(CHAR_FLAG_PREFIX)};
    private final static String[] INVALID_ARGS_1 = new String[] {INVALID_OPTION_1};
    private final static String[] INVALID_ARGS_2 = new String[] {INVALID_OPTION_2, STRING_UNICODE_NAME_FILE};
    private final static String[] INVALID_ARGS_3 = new String[] {STRING_SPACE_FILE_TXT, INVALID_OPTION_3, STRING_UNICODE_NAME_FILE};
    private final static String[] INVALID_ARGS_4 = new String[] {STRING_SPACE_FILE_TXT, STRING_UNICODE_NAME_FILE, null};

    private LsArgsParser parser;

    @BeforeEach
    void setUp() {
        parser = new LsArgsParser();
    }

    @AfterEach
    void tearDown() {
        parser = null;
    }

    @Test
    void superClass_Initialization_ArgsParser() {
        assertTrue(parser instanceof ArgsParser);
    }

    @Test
    void parse_NullArgs_Success() {
        assertDoesNotThrow(() -> parser.parse((String[]) null));
    }

    @Test
    void parse_EmptyArgs_Success() {
        assertDoesNotThrow(() -> parser.parse());
    }

    @Test
    void parse_ValidArgs_Success() {
        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_1);
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_2);
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_3);
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_1);
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_2);
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_3);
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_4);
        });
    }

    @Test
    void parse_InvalidArgs_ThrowsException() {
        assertThrows(InvalidArgsException.class, () -> {
            parser = new LsArgsParser();
            parser.parse(INVALID_ARGS_1);
        });

        assertThrows(InvalidArgsException.class, () -> {
            parser = new LsArgsParser();
            parser.parse(INVALID_ARGS_2);
        });

        assertThrows(InvalidArgsException.class, () -> {
            parser = new LsArgsParser();
            parser.parse(INVALID_ARGS_3);
        });

        assertThrows(InvalidArgsException.class, () -> {
            parser = new LsArgsParser();
            parser.parse(INVALID_ARGS_4);
        });
    }


    @Test
    void isRecursive_NoRecursiveOption_ReturnsFalse() {
        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_1);
            assertFalse(parser.isRecursive());
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_2);
            assertFalse(parser.isRecursive());
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_3);
            assertFalse(parser.isRecursive());
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_1);
            assertFalse(parser.isRecursive());
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_4);
            assertFalse(parser.isRecursive());
        });
    }

    @Test
    void isRecursive_WithRecursiveOption_ReturnsTrue() {
        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_2);
            assertTrue(parser.isRecursive());
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_3);
            assertTrue(parser.isRecursive());
        });
    }

    @Test
    void isFoldersOnly_NoFoldersOnlyOption_ReturnsFalse() {
        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_1);
            assertFalse(parser.isFoldersOnly());
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_2);
            assertFalse(parser.isFoldersOnly());
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_1);
            assertFalse(parser.isFoldersOnly());
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_2);
            assertFalse(parser.isFoldersOnly());
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_4);
            assertFalse(parser.isFoldersOnly());
        });
    }

    @Test
    void isFoldersOnly_WithFoldersOnlyOption_ReturnsTrue() {
        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_3);
            assertTrue(parser.isFoldersOnly());
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_3);
            assertTrue(parser.isRecursive());
        });
    }

    @Test
    void isSortByExt_NoSortByExtOption_ReturnsFalse() {
        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_1);
            assertFalse(parser.isSortByExt());
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_2);
            assertFalse(parser.isSortByExt());
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_3);
            assertFalse(parser.isSortByExt());
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_1);
            assertFalse(parser.isSortByExt());
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_2);
            assertFalse(parser.isSortByExt());
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_4);
            assertFalse(parser.isSortByExt());
        });
    }

    @Test
    void isSortByExt_WithSortByExtOption_ReturnsTrue() {
        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_3);
            assertTrue(parser.isSortByExt());
        });
    }

    @Test
    void getFolderNames_ParsedWithValidArgs_ReturnsFolderNamesWithoutOptions() {
        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_1);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_SINGLE_VALUE_ARGS_1),
                    parser.getFolderNames()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_2);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_SINGLE_VALUE_ARGS_2),
                    parser.getFolderNames()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_3);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_SINGLE_VALUE_ARGS_3),
                    parser.getFolderNames()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_1);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_1),
                    parser.getFolderNames()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_2);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_2),
                    parser.getFolderNames()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_3);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_3),
                    parser.getFolderNames()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new LsArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_4);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_4),
                    parser.getFolderNames()
            );
        });
    }
}