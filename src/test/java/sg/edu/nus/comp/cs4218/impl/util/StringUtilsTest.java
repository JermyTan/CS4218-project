package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    public void isBlank_nullStr_returnsTrue() {
        String str = null;
        assertTrue(StringUtils.isBlank(str));
    }

    @Test
    public void isBlank_emptyStr_returnsTrue() {
        String str = "";
        assertTrue(StringUtils.isBlank(str));
    }

    @Test
    public void isBlank_strWithWhitespacesOnly_returnsTrue() {
        String str = "     ";
        assertTrue(StringUtils.isBlank(str));
    }

    @Test
    public void isBlank_strWithoutWhitespaces_returnsFalse() {
        String str = "abc";
        assertFalse(StringUtils.isBlank(str));
    }

    @Test
    public void isBlank_strWithOneWhitespace_returnsFalse() {
        String str = "a bc";
        assertFalse(StringUtils.isBlank(str));
    }
}