package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StringUtilsTest {

    @Test
    public void isBlank_NullStr_ReturnsTrue() {
        String str = null;
        assertTrue(StringUtils.isBlank(str));
    }

    @Test
    public void isBlank_EmptyStr_ReturnsTrue() {
        String str = "";
        assertTrue(StringUtils.isBlank(str));
    }

    @Test
    public void isBlank_StrWithWhitespacesOnly_ReturnsTrue() {
        String str = "     ";
        assertTrue(StringUtils.isBlank(str));
    }

    @Test
    public void isBlank_StrWithoutWhitespaces_ReturnsFalse() {
        String str = "abc";
        assertFalse(StringUtils.isBlank(str));
    }

    @Test
    public void isBlank_StrWithOneWhitespace_ReturnsFalse() {
        String str = "a bc";
        assertFalse(StringUtils.isBlank(str));
    }
}