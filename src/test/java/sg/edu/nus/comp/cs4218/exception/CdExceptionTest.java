package sg.edu.nus.comp.cs4218.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_CD;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.EXCEPTION;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_LIST;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_SINGLE_WORD;

import org.junit.jupiter.api.Test;

class CdExceptionTest {
    @Test
    void abstractSuperClass_Initialization_ReturnsTrue() {
        assertTrue(new CdException(STRING_SINGLE_WORD) instanceof AbstractApplicationException);
    }

    @Test
    void getMessage_AnyValidMessage_AppNamePrependedMessage() {
        STRING_LIST.forEach(string -> {
            assertEquals(
                    new CdException(string).getMessage(),
                    String.format(STRING_LABEL_VALUE_PAIR, APP_CD, string)
            );
        });
    }

    @Test
    void getMessage_NullMessage_AppNamePrependedMessage() {
        assertEquals(new CdException(null).getMessage(), String.format(STRING_LABEL_VALUE_PAIR, APP_CD, null));
    }

    @Test
    void getCause_ValidCause_SuppliedCause() {
        assertEquals(
                new CdException(STRING_SINGLE_WORD, EXCEPTION).getCause(),
                EXCEPTION
        );
    }

    @Test
    void getCause_NullCause_Null() {
        assertNull(new CdException(STRING_SINGLE_WORD).getCause());
        assertNull(new CdException(STRING_SINGLE_WORD, null).getCause());
    }
}