package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_BLANK;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_CUSTOM_EXT_FILE;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_FILE_JPG;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_FILE_MD;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_FILE_TXT;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_LEADING_PERIOD_FILE_TXT;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_LEADING_PERIOD_NO_EXT_FILE;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_LEADING_TRAILING_SPACES;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_MULTI_WORDS;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_NO_EXT_FILE;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_SINGLE_WORD;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_SPACE_FILE_TXT;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_SPECIAL_CHARS;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_TRAILING_PERIOD_FILE;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_UNDERSCORE_FILE_TXT;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_UNICODE;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_UNICODE_EXT_FILE;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_UNICODE_NAME_FILE;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

class StringUtilsTest {
    private final static String TXT_EXT = "txt";
    private final static String JPG_EXT = "jpg";
    private final static String MD_EXT = "md";
    private final static String CUSTOM_EXT = "CusTom";
    private final static String UNICODE_EXT = "😈🌚";

    @Test
    void getFileExtension_NullString_ReturnsEmptyString() {
        assertEquals(STRING_EMPTY, StringUtils.getFileExtension(null));
    }

    @Test
    void getFileExtension_StringsWithoutExt_ReturnsEmptyString() {
        assertEquals(STRING_EMPTY, StringUtils.getFileExtension(STRING_NO_EXT_FILE));
        assertEquals(STRING_EMPTY, StringUtils.getFileExtension(STRING_LEADING_PERIOD_NO_EXT_FILE));
        assertEquals(STRING_EMPTY, StringUtils.getFileExtension(STRING_TRAILING_PERIOD_FILE));
    }

    @Test
    void getFileExtension_StringsWithExt_ReturnsExt() {
        List<String> stringsWithExt = List.of(
                STRING_FILE_TXT,
                STRING_UNDERSCORE_FILE_TXT,
                STRING_SPACE_FILE_TXT,
                STRING_FILE_JPG,
                STRING_FILE_MD,
                STRING_CUSTOM_EXT_FILE,
                STRING_LEADING_PERIOD_FILE_TXT,
                STRING_UNICODE_NAME_FILE,
                STRING_UNICODE_EXT_FILE
        );

        List<String> expectedExts = List.of(
                TXT_EXT,
                TXT_EXT,
                TXT_EXT,
                JPG_EXT,
                MD_EXT,
                CUSTOM_EXT,
                TXT_EXT,
                TXT_EXT,
                UNICODE_EXT
        );

        IntStream.range(0, stringsWithExt.size())
                .forEach(i -> {
                    assertEquals(expectedExts.get(i), StringUtils.getFileExtension(stringsWithExt.get(i)));
                });
    }

    @Test
    void isBlank_Null_ReturnsTrue() {
        assertTrue(StringUtils.isBlank(null));
    }

    @Test
    void isBlank_Empty_ReturnsTrue() {
        assertTrue(StringUtils.isBlank(STRING_EMPTY));
    }

    @Test
    void isBlank_WhitespacesOnly_ReturnsTrue() {
        assertTrue(StringUtils.isBlank(STRING_BLANK));
    }

    @Test
    void isBlank_WithoutWhitespaces_ReturnsFalse() {
        assertFalse(StringUtils.isBlank(STRING_SINGLE_WORD));
    }

    @Test
    void isBlank_WithWhitespaces_ReturnsFalse() {
        assertFalse(StringUtils.isBlank(STRING_LEADING_TRAILING_SPACES));
    }

    @Test
    void isBlank_Unicode_ReturnsFalse() {
        assertFalse(StringUtils.isBlank(STRING_UNICODE));
    }

    @Test
    void isBlank_SpecialChars_ReturnsFalse() {
        assertFalse(StringUtils.isBlank(STRING_SPECIAL_CHARS));
    }

    @Test
    void multiplyChar_MultiplyOnce_ReturnsSingleCharString() {
        assertEquals("a", StringUtils.multiplyChar('a', 1));
    }

    @Test
    void multiplyChar_MultiplyZero_ReturnsEmptyString() {
        assertEquals(STRING_EMPTY, StringUtils.multiplyChar('a', 0));
    }

    @Test
    void multiplyChar_MultipleNegativeValue_ReturnsEmptyString() {
        assertEquals(STRING_EMPTY, StringUtils.multiplyChar('a', -3));
    }

    @Test
    void multipleChar_MultiplePositiveValue_ReturnsCharMultipledString() {
        assertEquals("a".repeat(10), StringUtils.multiplyChar('a', 10));
    }

    @Test
    void multipleChar_WhitespaceChar_ReturnsCharMultipledString() {
        assertEquals(" ".repeat(4), StringUtils.multiplyChar(' ', 4));
    }

    @Test
    void tokenize_EmptyString_ReturnsEmptyArray() {
        assertArrayEquals(new String[0], StringUtils.tokenize(STRING_EMPTY));
    }

    @Test
    void tokenize_WhitespacesOnlyString_ReturnsEmptyArray() {
        assertArrayEquals(new String[0], StringUtils.tokenize(STRING_BLANK));
    }

    @Test
    void tokenize_SingleWordString_ReturnsNonEmptyArray() {
        assertArrayEquals(new String[]{STRING_SINGLE_WORD}, StringUtils.tokenize(STRING_SINGLE_WORD));
    }

    @Test
    void tokenize_MultiWordsString_ReturnsNonEmptyArray() {
        assertArrayEquals(new String[]{"This", "is", "a", "test", "string"}, StringUtils.tokenize(STRING_MULTI_WORDS));
    }

    @Test
    void tokenize_LeadingTrailingWhitespacesString_ReturnsNonEmptyArray() {
        assertArrayEquals(new String[]{"Test", "string"}, StringUtils.tokenize(STRING_LEADING_TRAILING_SPACES));
    }

    @Test
    void tokenize_UnicodeString_ReturnsNonEmptyArray() {
        assertArrayEquals(new String[]{"Test", "💩🌚😊👍🏻风和日丽"}, StringUtils.tokenize(STRING_UNICODE));
    }

    @Test
    void tokenize_SpecialCharsString_ReturnsNonEmptyArray() {
        assertArrayEquals(new String[]{"*;|/\\/%:-_", ".,'"}, StringUtils.tokenize(STRING_SPECIAL_CHARS));
    }
}