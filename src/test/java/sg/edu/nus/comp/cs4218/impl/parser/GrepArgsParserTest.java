package sg.edu.nus.comp.cs4218.impl.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

@SuppressWarnings("PMD.ExcessiveMethodLength")
class GrepArgsParserTest {
    private static final char FLAG_IS_CASE_INSENSITIVE = 'i';
    private static final char FLAG_IS_COUNT_LINES = 'c';
    private static final char FLAG_IS_PREFIX_FILE_NAME = 'H';
    private static final String VALID_OPTION_1 = String.format("%s%s", CHAR_FLAG_PREFIX, FLAG_IS_CASE_INSENSITIVE);
    private static final String VALID_OPTION_2 = String.format("%s%s", CHAR_FLAG_PREFIX, multiplyChar(FLAG_IS_COUNT_LINES, 3));
    private static final String VALID_OPTION_3 = String.format("%s%s%s", CHAR_FLAG_PREFIX, FLAG_IS_PREFIX_FILE_NAME, FLAG_IS_CASE_INSENSITIVE, FLAG_IS_COUNT_LINES);
    private static final String INVALID_OPTION_1 = multiplyChar(CHAR_FLAG_PREFIX, 2);
    private static final String INVALID_OPTION_2 = String.format("%s%s%s", CHAR_FLAG_PREFIX, FLAG_IS_CASE_INSENSITIVE, CHAR_FLAG_PREFIX);
    private static final String INVALID_OPTION_3 = String.format("%s%s%s", CHAR_FLAG_PREFIX, FLAG_IS_COUNT_LINES, FLAG_IS_PREFIX_FILE_NAME + 1);
    private static final String[] VALID_SINGLE_VALUE_ARGS_1 = new String[]{String.valueOf(CHAR_FLAG_PREFIX)};
    private static final String[] VALID_SINGLE_VALUE_ARGS_2 = new String[]{STRING_SPACE_FILE_TXT};
    private static final String[] VALID_MULTI_VALUES_ARGS_1 = FILE_LIST.toArray(String[]::new);
    private static final String[] VALID_MULTI_VALUES_ARGS_2 = new String[]{VALID_OPTION_1,
            STRING_UNICODE_NAME_FILE,
            STRING_SPACE_FILE_TXT};
    private static final String[] VALID_MULTI_VALUES_ARGS_3 = new String[]{STRING_SPACE_FILE_TXT,
            VALID_OPTION_3,
            STRING_UNICODE_NAME_FILE,
            VALID_OPTION_2};
    private static final String[] VALID_MULTI_VALUES_ARGS_4 = new String[]{String.valueOf(CHAR_FLAG_PREFIX),
            String.valueOf(CHAR_FLAG_PREFIX)};
    private static final String[] INVALID_ARGS_1 = new String[]{INVALID_OPTION_1};
    private static final String[] INVALID_ARGS_2 = new String[]{INVALID_OPTION_2, STRING_UNICODE_NAME_FILE};
    private static final String[] INVALID_ARGS_3 = new String[]{STRING_SPACE_FILE_TXT,
            INVALID_OPTION_3,
            STRING_UNICODE_NAME_FILE};
    private static final String[] INVALID_ARGS_4 = new String[]{STRING_SPACE_FILE_TXT, STRING_UNICODE_NAME_FILE, null};
    private static final String[] INVALID_ARGS_5 = new String[]{VALID_OPTION_1};

    private GrepArgsParser parser;

    @BeforeEach
    void setUp() {
        parser = new GrepArgsParser();
    }

    @AfterEach
    void tearDown() {
        parser = null;
    }

    @Test
    void superClass_Initialization_ArgsParser() {
        assertTrue(new GrepArgsParser() instanceof ArgsParser);
    }


    @Test
    void parse_NullArgs_ThrowsException() {
        assertThrows(InvalidArgsException.class, () -> parser.parse((String[]) null));
    }

    @Test
    void parse_EmptyArgs_ThrowsException() {
        assertThrows(InvalidArgsException.class, () -> parser.parse());
    }

    @Test
    void parse_ValidArgs_Success() {
        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_1);
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_2);
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_1);
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_2);
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_3);
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_4);
        });
    }

    @Test
    void parse_InvalidArgs_ThrowsException() {
        assertThrows(InvalidArgsException.class, () -> {
            parser = new GrepArgsParser();
            parser.parse(INVALID_ARGS_1);
        });

        assertThrows(InvalidArgsException.class, () -> {
            parser = new GrepArgsParser();
            parser.parse(INVALID_ARGS_2);
        });

        assertThrows(InvalidArgsException.class, () -> {
            parser = new GrepArgsParser();
            parser.parse(INVALID_ARGS_3);
        });

        assertThrows(InvalidArgsException.class, () -> {
            parser = new GrepArgsParser();
            parser.parse(INVALID_ARGS_4);
        });

        assertThrows(InvalidArgsException.class, () -> {
            parser = new GrepArgsParser();
            parser.parse(INVALID_ARGS_5);
        });
    }


    @Test
    void isCaseInsensitive_NoCaseInsensitiveOption_ReturnsFalse() {
        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_1);
            assertFalse(parser.isCaseInsensitive());
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_2);
            assertFalse(parser.isCaseInsensitive());
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_1);
            assertFalse(parser.isCaseInsensitive());
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_4);
            assertFalse(parser.isCaseInsensitive());
        });
    }

    @Test
    void isCaseInsensitive_WithCaseInsensitiveOption_ReturnsTrue() {
        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_2);
            assertTrue(parser.isCaseInsensitive());
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_3);
            assertTrue(parser.isCaseInsensitive());
        });
    }

    @Test
    void isCountLines_NoCountLinesOption_ReturnsFalse() {
        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_1);
            assertFalse(parser.isCountLines());
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_2);
            assertFalse(parser.isCountLines());
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_1);
            assertFalse(parser.isCountLines());
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_2);
            assertFalse(parser.isCountLines());
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_4);
            assertFalse(parser.isCountLines());
        });
    }

    @Test
    void isCountLines_WithCountLinesOption_ReturnsTrue() {
        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_3);
            assertTrue(parser.isCountLines());
        });
    }

    @Test
    void isPrefixFileName_NoPrefixFileName_ReturnsFalse() {
        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_1);
            assertFalse(parser.isPrefixFileName());
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_2);
            assertFalse(parser.isPrefixFileName());
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_1);
            assertFalse(parser.isPrefixFileName());
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_2);
            assertFalse(parser.isPrefixFileName());
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_4);
            assertFalse(parser.isPrefixFileName());
        });
    }

    @Test
    void isPrefixFileName_WithPrefixFileName_ReturnsTrue() {
        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_3);
            assertTrue(parser.isPrefixFileName());
        });
    }

    @Test
    void getPattern_ParsedWithValidArgs_ReturnsFirstNonOptionArg() {
        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_1);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_SINGLE_VALUE_ARGS_1).get(0),
                    parser.getPattern()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_2);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_SINGLE_VALUE_ARGS_2).get(0),
                    parser.getPattern()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_1);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_1).get(0),
                    parser.getPattern()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_2);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_2).get(0),
                    parser.getPattern()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_3);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_3).get(0),
                    parser.getPattern()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_4);
            assertEquals(
                    ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_4).get(0),
                    parser.getPattern()
            );
        });
    }

    @Test
    void getFileNames_ParsedWithValidArgs_ReturnsFileNamesWithoutOptions() {
        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_1);
            assertEquals(
                    ParserTestUtils.dropFirst(
                            ParserTestUtils.removeOptions(VALID_SINGLE_VALUE_ARGS_1),
                            1
                    ),
                    parser.getFileNames()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_SINGLE_VALUE_ARGS_2);
            assertEquals(
                    ParserTestUtils.dropFirst(
                            ParserTestUtils.removeOptions(VALID_SINGLE_VALUE_ARGS_2),
                            1
                    ),
                    parser.getFileNames()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_1);
            assertEquals(
                    ParserTestUtils.dropFirst(
                            ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_1),
                            1
                    ),
                    parser.getFileNames()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_2);
            assertEquals(
                    ParserTestUtils.dropFirst(
                            ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_2),
                            1
                    ),
                    parser.getFileNames()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_3);
            assertEquals(
                    ParserTestUtils.dropFirst(
                            ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_3),
                            1
                    ),
                    parser.getFileNames()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new GrepArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_4);
            assertEquals(
                    ParserTestUtils.dropFirst(
                            ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_4),
                            1
                    ),
                    parser.getFileNames()
            );
        });
    }
}