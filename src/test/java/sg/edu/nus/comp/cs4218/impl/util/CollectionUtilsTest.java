package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CollectionUtilsTest {

    @Test
    void isAnyNull_NullArgs_ReturnsTrue() {
        assertTrue(CollectionUtils.isAnyNull((Object[]) null));
    }

    @Test
    void isAnyNull_SomeNullArgs_ReturnsTrue() {
        assertTrue(CollectionUtils.isAnyNull(1, "test", null, new Object()));
    }

    @Test
    void isAnyNull_EmptyArgs_ReturnsFalse() {
        assertFalse(CollectionUtils.isAnyNull());
    }

    @Test
    void isAnyNull_NoNullArgs_ReturnsTrue() {
        assertFalse(CollectionUtils.isAnyNull("test", new Object(), 10));
    }
}