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
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_UNDERSCORE_FILE_TXT;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_UNICODE_NAME_FILE;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.testutil.ParserTestUtils;

class MvArgsParserTest {
    private final static char FLAG_IS_NOT_OVERWRITE = 'n';
    private final static String VALID_OPTION_1 = String.format("%s%s", CHAR_FLAG_PREFIX, FLAG_IS_NOT_OVERWRITE);
    private final static String VALID_OPTION_2 = String.format("%s%s", CHAR_FLAG_PREFIX, multiplyChar(FLAG_IS_NOT_OVERWRITE, 3));
    private final static String INVALID_OPTION_1 = multiplyChar(CHAR_FLAG_PREFIX, 2);
    private final static String INVALID_OPTION_2 = String.format("%s%s%s", CHAR_FLAG_PREFIX, FLAG_IS_NOT_OVERWRITE, CHAR_FLAG_PREFIX);
    private final static String INVALID_OPTION_3 = String.format("%s%s%s", CHAR_FLAG_PREFIX, FLAG_IS_NOT_OVERWRITE, FLAG_IS_NOT_OVERWRITE + 1);
    private final static String[] VALID_MULTI_VALUES_ARGS_1 = FILE_LIST.toArray(String[]::new);
    private final static String[] VALID_MULTI_VALUES_ARGS_2 = new String[]{VALID_OPTION_1,
            STRING_UNICODE_NAME_FILE,
            STRING_SPACE_FILE_TXT};
    private final static String[] VALID_MULTI_VALUES_ARGS_3 = new String[]{STRING_SPACE_FILE_TXT,
            VALID_OPTION_1,
            STRING_UNICODE_NAME_FILE,
            VALID_OPTION_2};
    private final static String[] INVALID_ARGS_1 = new String[]{INVALID_OPTION_1};
    private final static String[] INVALID_ARGS_2 = new String[]{INVALID_OPTION_2, STRING_UNICODE_NAME_FILE};
    private final static String[] INVALID_ARGS_3 = new String[]{STRING_SPACE_FILE_TXT,
            INVALID_OPTION_3,
            STRING_UNICODE_NAME_FILE};
    private final static String[] INVALID_ARGS_4 = new String[]{STRING_SPACE_FILE_TXT, STRING_UNICODE_NAME_FILE, null};
    private final static String[] INVALID_ARGS_5 = new String[]{String.valueOf(CHAR_FLAG_PREFIX)};
    private final static String[] INVALID_ARGS_6 = new String[]{STRING_SPACE_FILE_TXT};
    private final static String[] INVALID_ARGS_7 = new String[]{VALID_OPTION_2};
    private final static String[] INVALID_ARGS_8 = new String[]{VALID_OPTION_2, STRING_UNDERSCORE_FILE_TXT};

    private MvArgsParser parser;

    @BeforeEach
    void setUp() {
        parser = new MvArgsParser();
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
            parser = new MvArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_1);
        });

        assertDoesNotThrow(() -> {
            parser = new MvArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_2);
        });

        assertDoesNotThrow(() -> {
            parser = new MvArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_3);
        });
    }

    @Test
    void parse_InvalidArgs_ThrowsException() {
        assertThrows(InvalidArgsException.class, () -> {
            parser = new MvArgsParser();
            parser.parse(INVALID_ARGS_1);
        });

        assertThrows(InvalidArgsException.class, () -> {
            parser = new MvArgsParser();
            parser.parse(INVALID_ARGS_2);
        });

        assertThrows(InvalidArgsException.class, () -> {
            parser = new MvArgsParser();
            parser.parse(INVALID_ARGS_3);
        });

        assertThrows(InvalidArgsException.class, () -> {
            parser = new MvArgsParser();
            parser.parse(INVALID_ARGS_4);
        });

        assertThrows(InvalidArgsException.class, () -> {
            parser = new MvArgsParser();
            parser.parse(INVALID_ARGS_5);
        });

        assertThrows(InvalidArgsException.class, () -> {
            parser = new MvArgsParser();
            parser.parse(INVALID_ARGS_6);
        });

        assertThrows(InvalidArgsException.class, () -> {
            parser = new MvArgsParser();
            parser.parse(INVALID_ARGS_7);
        });

        assertThrows(InvalidArgsException.class, () -> {
            parser = new MvArgsParser();
            parser.parse(INVALID_ARGS_8);
        });
    }

    @Test
    void isNotOverwrite_NoOverwriteOption_ReturnsFalse() {
        assertDoesNotThrow(() -> {
            parser = new MvArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_1);
            assertFalse(parser.isNotOverwrite());
        });
    }

    @Test
    void isNotOverwrite_WithOverwriteOption_ReturnsTrue() {
        assertDoesNotThrow(() -> {
            parser = new MvArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_2);
            assertTrue(parser.isNotOverwrite());
        });

        assertDoesNotThrow(() -> {
            parser = new MvArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_3);
            assertTrue(parser.isNotOverwrite());
        });
    }

    @Test
    void getDestFile_ParsedWithValidArgs_ReturnsDestFile() {
        assertDoesNotThrow(() -> {
            parser = new MvArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_1);
            List<String> filteredValues = ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_1);
            assertEquals(
                    filteredValues.get(filteredValues.size() - 1),
                    parser.getDestFile()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new MvArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_2);
            List<String> filteredValues = ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_2);
            assertEquals(
                    filteredValues.get(filteredValues.size() - 1),
                    parser.getDestFile()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new MvArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_3);
            List<String> filteredValues = ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_3);
            assertEquals(
                    filteredValues.get(filteredValues.size() - 1),
                    parser.getDestFile()
            );
        });
    }

    @Test
    void getSrcFiles_ParsedWithValidArgs_ReturnsSrcFilesWithoutOptions() {
        assertDoesNotThrow(() -> {
            parser = new MvArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_1);
            assertEquals(
                    ParserTestUtils.dropLast(
                            ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_1),
                            1
                    ),
                    parser.getSrcFiles()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new MvArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_2);
            assertEquals(
                    ParserTestUtils.dropLast(
                            ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_2),
                            1
                    ),
                    parser.getSrcFiles()
            );
        });

        assertDoesNotThrow(() -> {
            parser = new MvArgsParser();
            parser.parse(VALID_MULTI_VALUES_ARGS_3);
            assertEquals(
                    ParserTestUtils.dropLast(
                            ParserTestUtils.removeOptions(VALID_MULTI_VALUES_ARGS_3),
                            1
                    ),
                    parser.getSrcFiles()
            );
        });
    }
}