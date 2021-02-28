package sg.edu.nus.comp.cs4218.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.EXCEPTION;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_LIST;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_SINGLE_WORD;

import org.junit.jupiter.api.Test;

class InvalidArgsExceptionTest {

    @Test
    void getMessage_AnyValidMessage_SuppliedMessage() {
        STRING_LIST.forEach(string -> {
            assertEquals(
                    string,
                    new InvalidArgsException(string).getMessage()
            );
        });
    }

    @Test
    void getMessage_NullMessage_Null() {
        assertNull(new InvalidArgsException(null).getMessage());
    }

    @Test
    void getCause_ValidCause_SuppliedCause() {
        assertEquals(
                EXCEPTION,
                new InvalidArgsException(STRING_SINGLE_WORD, EXCEPTION).getCause()
        );
    }

    @Test
    void getCause_NullCause_Null() {
        assertNull(new InvalidArgsException(STRING_SINGLE_WORD).getCause());
        assertNull(new InvalidArgsException(STRING_SINGLE_WORD, null).getCause());
    }
}