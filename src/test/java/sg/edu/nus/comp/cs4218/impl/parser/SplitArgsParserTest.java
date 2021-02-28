package sg.edu.nus.comp.cs4218.impl.parser;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FLAG_PREFIX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.multiplyChar;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_SPACE_FILE_TXT;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_UNDERSCORE_FILE_TXT;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_UNICODE_NAME_FILE;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.testutil.ParserTestUtils;

class SplitArgsParserTest {
    private final static char FLAG_IS_SPLIT_BY_LINES = 'l';
    private final static char FLAG_IS_SPLIT_BY_BYTES = 'b';
    private final static String VALID_OPTION_1 = String.format("%s%s", CHAR_FLAG_PREFIX, FLAG_IS_SPLIT_BY_LINES);
    private final static String VALID_OPTION_2 = String.format("%s%s", CHAR_FLAG_PREFIX, multiplyChar(FLAG_IS_SPLIT_BY_BYTES, 3));
    private final static String INVALID_OPTION_1 = multiplyChar(CHAR_FLAG_PREFIX, 2);
    private final static String INVALID_OPTION_2 = String.format("%s%s%s", CHAR_FLAG_PREFIX, FLAG_IS_SPLIT_BY_LINES, CHAR_FLAG_PREFIX);
    private final static String INVALID_OPTION_3 = String.format("%s%s%s", CHAR_FLAG_PREFIX, FLAG_IS_SPLIT_BY_LINES, FLAG_IS_SPLIT_BY_BYTES + 1);
    private final static String INVALID_OPTION_4 = String.format("%s%s%s", CHAR_FLAG_PREFIX, FLAG_IS_SPLIT_BY_LINES, FLAG_IS_SPLIT_BY_BYTES);
    private final static String[] VALID_NO_VALUE_ARGS = new String[] {};
    private final static String[] VALID_SINGLE_VALUE_ARGS_1 = new String[] {String.valueOf(CHAR_FLAG_PREFIX)};
    private final static String[] VALID_SINGLE_VALUE_ARGS_2 = new String[] {STRING_SPACE_FILE_TXT};
    private final static String[] VALID_MULTI_VALUES_ARGS_1 = new String[] {STRING_SPACE_FILE_TXT, STRING_UNDERSCORE_FILE_TXT};
    private final static String[] VALID_MULTI_VALUES_ARGS_2 = new String[] {VALID_OPTION_1, STRING_UNICODE_NAME_FILE, STRING_SPACE_FILE_TXT, STRING_UNDERSCORE_FILE_TXT};
    private final static String[] VALID_MULTI_VALUES_ARGS_3 = new String[] {STRING_SPACE_FILE_TXT, VALID_OPTION_2};
    private final static String[] VALID_MULTI_VALUES_ARGS_4 = new String[] {String.valueOf(CHAR_FLAG_PREFIX), String.valueOf(CHAR_FLAG_PREFIX)};
    private final static String[] INVALID_ARGS_1 = new String[] {INVALID_OPTION_1};
    private final static String[] INVALID_ARGS_2 = new String[] {INVALID_OPTION_2, STRING_UNICODE_NAME_FILE};
    private final static String[] INVALID_ARGS_3 = new String[] {STRING_SPACE_FILE_TXT, INVALID_OPTION_3, STRING_UNICODE_NAME_FILE};
    private final static String[] INVALID_ARGS_4 = new String[] {STRING_SPACE_FILE_TXT, STRING_UNICODE_NAME_FILE, null};
    private final static String[] INVALID_ARGS_5 = new String[] {String.valueOf(CHAR_FLAG_PREFIX), INVALID_OPTION_4};
    private final static String[] INVALID_ARGS_6 = new String[] {VALID_OPTION_1};
    private final static String[] INVALID_ARGS_7 = new String[] {VALID_OPTION_2};
    private final static String[] INVALID_ARGS_8 = new String[] {STRING_UNICODE_NAME_FILE, STRING_SPACE_FILE_TXT, STRING_UNDERSCORE_FILE_TXT};

    private SplitArgsParser parser;

    @BeforeEach
    void setUp() {
        parser = new SplitArgsParser();
    }

    @AfterEach
    void tearDown() {
        parser = null;
    }

    @Test
    void superClass_Initialization_ArgsParser() {
        assertTrue(new SplitArgsParser() instanceof ArgsParser);
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
            parser = new SplitArgsParser();
            parser.parse(VALID_NO_VALUE_ARGS);
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_1);
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_2);
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_1);
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_2);
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_3);
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_4);
        });
    }

    @Test
    void parse_InvalidArgs_ThrowsException() {
        assertThrows(InvalidArgsException.class, () -> {
            parser = new SplitArgsParser();
            parser.parse(INVALID_ARGS_1);
        });

        assertThrows(InvalidArgsException.class, () -> {
            parser = new SplitArgsParser();
            parser.parse(INVALID_ARGS_2);
        });

        assertThrows(InvalidArgsException.class, () -> {
            parser = new SplitArgsParser();
            parser.parse(INVALID_ARGS_3);
        });

        assertThrows(InvalidArgsException.class, () -> {
            parser = new SplitArgsParser();
            parser.parse(INVALID_ARGS_4);
        });

        assertThrows(InvalidArgsException.class, () -> {
            parser = new SplitArgsParser();
            parser.parse(INVALID_ARGS_5);
        });

        assertThrows(InvalidArgsException.class, () -> {
            parser = new SplitArgsParser();
            parser.parse(INVALID_ARGS_6);
        });

        assertThrows(InvalidArgsException.class, () -> {
            parser = new SplitArgsParser();
            parser.parse(INVALID_ARGS_7);
        });

        assertThrows(InvalidArgsException.class, () -> {
            parser = new SplitArgsParser();
            parser.parse(INVALID_ARGS_8);
        });
    }


    @Test
    void isSplitByLines_NoSplitByLinesOption_ReturnsFalse() {
        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_NO_VALUE_ARGS);
            assertFalse(parser.isSplitByLines());
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_1);
            assertFalse(parser.isSplitByLines());
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_2);
            assertFalse(parser.isSplitByLines());
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_1);
            assertFalse(parser.isSplitByLines());
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_3);
            assertFalse(parser.isSplitByLines());
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_4);
            assertFalse(parser.isSplitByLines());
        });
    }

    @Test
    void isSplitByLines_WithSplitByLinesOption_ReturnsTrue() {
        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_2);
            assertTrue(parser.isSplitByLines());
        });
    }

    @Test
    void isSplitByBytes_NoSplitByBytesOption_ReturnsFalse() {
        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_NO_VALUE_ARGS);
            assertFalse(parser.isSplitByBytes());
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_1);
            assertFalse(parser.isSplitByBytes());
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_2);
            assertFalse(parser.isSplitByBytes());
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_1);
            assertFalse(parser.isSplitByBytes());
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_2);
            assertFalse(parser.isSplitByBytes());
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_4);
            assertFalse(parser.isSplitByBytes());
        });
    }

    @Test
    void isSplitByBytes_WithSplitByBytesOption_ReturnsTrue() {
        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_3);
            assertTrue(parser.isSplitByBytes());
        });
    }

    @Test
    void getNumOfLinesOrBytes_ParsedWithoutOptions_ReturnsNull() {
        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_NO_VALUE_ARGS);
            assertNull(parser.getNumOfLinesOrBytes());
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_1);
            assertNull(parser.getNumOfLinesOrBytes());
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_2);
            assertNull(parser.getNumOfLinesOrBytes());
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_1);
            assertNull(parser.getNumOfLinesOrBytes());
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_4);
            assertNull(parser.getNumOfLinesOrBytes());
        });
    }

    @Test
    void getNumOfLinesOrBytes_ParsedWithOptions_ReturnsFirstNonOptionArg() {
        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_2);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_2).get(0),
                    parser.getNumOfLinesOrBytes()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_3);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_3).get(0),
                    parser.getNumOfLinesOrBytes()
            );
        });
    }

    @Test
    void getFileName_ParsedWithoutOptions_ReturnsFirstNonOptionArg() {
        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_NO_VALUE_ARGS);
            assertEquals(
                    null,
                    parser.getFileName()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_1);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_SINGLE_VALUE_ARGS_1).get(0),
                    parser.getFileName()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_2);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_SINGLE_VALUE_ARGS_2).get(0),
                    parser.getFileName()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_1);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_1).get(0),
                    parser.getFileName()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_4);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_4).get(0),
                    parser.getFileName()
            );
        });
    }

    @Test
    void getFileName_ParsedWithOptions_ReturnsSecondNonOptionArg() {
        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_2);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_2).get(1),
                    parser.getFileName()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_3);
            assertEquals(
                    null,
                    parser.getFileName()
            );
        });
    }

    @Test
    void getPrefix_ParsedWithoutOptions_ReturnsSecondNonOptionArg() {
        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_NO_VALUE_ARGS);
            assertEquals(
                    null,
                    parser.getPrefix()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_1);
            assertEquals(
                    null,
                    parser.getPrefix()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_2);
            assertEquals(
                    null,
                    parser.getPrefix()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_1);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_1).get(1),
                    parser.getPrefix()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_4);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_4).get(1),
                    parser.getPrefix()
            );
        });
    }

    @Test
    void getPrefix_ParsedWithOptions_ReturnsThirdNonOptionArg() {
        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_2);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_2).get(2),
                    parser.getPrefix()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new SplitArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_3);
            assertEquals(
                    null,
                    parser.getPrefix()
            );
        });
    }
}
