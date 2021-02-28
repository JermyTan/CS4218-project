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

class CatArgsParserTest {
    private final static char FLAG_IS_LINE_NUMBER = 'n';
    private final static String VALID_OPTION_1 = String.format("%s%s", CHAR_FLAG_PREFIX, FLAG_IS_LINE_NUMBER);
    private final static String VALID_OPTION_2 = String.format("%s%s", CHAR_FLAG_PREFIX, multiplyChar(FLAG_IS_LINE_NUMBER, 3));
    private final static String INVALID_OPTION_1 = multiplyChar(CHAR_FLAG_PREFIX, 2);
    private final static String INVALID_OPTION_2 = String.format("%s%s%s", CHAR_FLAG_PREFIX, FLAG_IS_LINE_NUMBER, CHAR_FLAG_PREFIX);
    private final static String INVALID_OPTION_3 = String.format("%s%s%s", CHAR_FLAG_PREFIX, FLAG_IS_LINE_NUMBER, FLAG_IS_LINE_NUMBER + 1);
    private final static String[] VALID_SINGLE_VALUE_ARGS_1 = new String[] {String.valueOf(CHAR_FLAG_PREFIX)};
    private final static String[] VALID_SINGLE_VALUE_ARGS_2 = new String[] {STRING_SPACE_FILE_TXT};
    private final static String[] VALID_SINGLE_VALUE_ARGS_3 = new String[] {VALID_OPTION_2};
    private final static String[] VALID_MULTI_VALUES_ARGS_1 = FILE_LIST.toArray(String[]::new);
    private final static String[] VALID_MULTI_VALUES_ARGS_2 = new String[] {VALID_OPTION_1, STRING_UNICODE_NAME_FILE, STRING_SPACE_FILE_TXT};
    private final static String[] VALID_MULTI_VALUES_ARGS_3 = new String[] {STRING_SPACE_FILE_TXT, VALID_OPTION_1, STRING_UNICODE_NAME_FILE, VALID_OPTION_2};
    private final static String[] VALID_MULTI_VALUES_ARGS_4 = new String[] {String.valueOf(CHAR_FLAG_PREFIX), String.valueOf(CHAR_FLAG_PREFIX)};
    private final static String[] INVALID_ARGS_1 = new String[] {INVALID_OPTION_1};
    private final static String[] INVALID_ARGS_2 = new String[] {INVALID_OPTION_2, STRING_UNICODE_NAME_FILE};
    private final static String[] INVALID_ARGS_3 = new String[] {STRING_SPACE_FILE_TXT, INVALID_OPTION_3, STRING_UNICODE_NAME_FILE};
    private final static String[] INVALID_ARGS_4 = new String[] {STRING_SPACE_FILE_TXT, STRING_UNICODE_NAME_FILE, null};

    private CatArgsParser parser;

    @BeforeEach
    void setUp() {
        parser = new CatArgsParser();
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
            parser = new CatArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_1);
        });

        assertDoesNotThrow(() -> {
            parser = new CatArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_2);
        });

        assertDoesNotThrow(() -> {
            parser = new CatArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_3);
        });

        assertDoesNotThrow(() -> {
            parser = new CatArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_1);
        });

        assertDoesNotThrow(() -> {
            parser = new CatArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_2);
        });

        assertDoesNotThrow(() -> {
            parser = new CatArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_3);
        });

        assertDoesNotThrow(() -> {
            parser = new CatArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_4);
        });
    }

    @Test
    void parse_InvalidArgs_ThrowsException() {
        assertThrows(InvalidArgsException.class, () -> {
            parser = new CatArgsParser();
            parser.parse(INVALID_ARGS_1);
        });

        assertThrows(InvalidArgsException.class, () -> {
            parser = new CatArgsParser();
            parser.parse(INVALID_ARGS_2);
        });

        assertThrows(InvalidArgsException.class, () -> {
            parser = new CatArgsParser();
            parser.parse(INVALID_ARGS_3);
        });

        assertThrows(InvalidArgsException.class, () -> {
            parser = new CatArgsParser();
            parser.parse(INVALID_ARGS_4);
        });
    }


    @Test
    void isLineNumber_NoLineNumberOption_ReturnsFalse() {
        assertDoesNotThrow(() -> {
            parser = new CatArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_1);
            assertFalse(parser.isLineNumber());
        });

        assertDoesNotThrow(() -> {
            parser = new CatArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_2);
            assertFalse(parser.isLineNumber());
        });

        assertDoesNotThrow(() -> {
            parser = new CatArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_1);
            assertFalse(parser.isLineNumber());
        });

        assertDoesNotThrow(() -> {
            parser = new CatArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_4);
            assertFalse(parser.isLineNumber());
        });
    }

    @Test
    void isLineNumber_WithLineNumberOption_ReturnsTrue() {
        assertDoesNotThrow(() -> {
            parser = new CatArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_3);
            assertTrue(parser.isLineNumber());
        });

        assertDoesNotThrow(() -> {
            parser = new CatArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_2);
            assertTrue(parser.isLineNumber());
        });

        assertDoesNotThrow(() -> {
            parser = new CatArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_3);
            assertTrue(parser.isLineNumber());
        });
    }

    @Test
    void getFileNames_ParsedWithValidArgs_ReturnsFileNamesWithoutOptions() {
        assertDoesNotThrow(() -> {
            parser = new CatArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_1);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_SINGLE_VALUE_ARGS_1),
                    parser.getFileNames()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new CatArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_2);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_SINGLE_VALUE_ARGS_2),
                    parser.getFileNames()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new CatArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_3);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_SINGLE_VALUE_ARGS_3),
                    parser.getFileNames()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new CatArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_1);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_1),
                    parser.getFileNames()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new CatArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_2);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_2),
                    parser.getFileNames()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new CatArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_3);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_3),
                    parser.getFileNames()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new CatArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_4);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_4),
                    parser.getFileNames()
            );
        });
    }
}