package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_BLANK;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_LEADING_TRAILING_SPACES;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_SINGLE_WORD;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_SPECIAL_CHARS;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_UNICODE;

import org.junit.jupiter.api.Test;

class StringUtilsTest {

    @Test
    public void isBlank_Null_ReturnsTrue() {
        assertTrue(StringUtils.isBlank(null));
    }

    @Test
    public void isBlank_Empty_ReturnsTrue() {
        assertTrue(StringUtils.isBlank(STRING_EMPTY));
    }

    @Test
    public void isBlank_WhitespacesOnly_ReturnsTrue() {
        assertTrue(StringUtils.isBlank(STRING_BLANK));
    }

    @Test
    public void isBlank_WithoutWhitespaces_ReturnsFalse() {
        assertFalse(StringUtils.isBlank(STRING_SINGLE_WORD));
    }

    @Test
    public void isBlank_WithWhitespaces_ReturnsFalse() {
        assertFalse(StringUtils.isBlank(STRING_LEADING_TRAILING_SPACES));
    }

    @Test
    public void isBlank_Unicode_ReturnsFalse() {
        assertFalse(StringUtils.isBlank(STRING_UNICODE));
    }

    @Test
    public void isBlank_SpecialChars_ReturnsFalse() {
        assertFalse(StringUtils.isBlank(STRING_SPECIAL_CHARS));
    }
}