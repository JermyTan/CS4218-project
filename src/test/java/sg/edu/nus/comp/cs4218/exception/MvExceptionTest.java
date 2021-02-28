package sg.edu.nus.comp.cs4218.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_MV;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.EXCEPTION;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_LIST;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_SINGLE_WORD;

import org.junit.jupiter.api.Test;

class MvExceptionTest {
    @Test
    void abstractSuperClass_Initialization_ReturnsTrue() {
        assertTrue(new MvException(STRING_SINGLE_WORD) instanceof AbstractApplicationException);
    }

    @Test
    void getMessage_AnyValidMessage_AppNamePrependedMessage() {
        STRING_LIST.forEach(string -> {
            assertEquals(
                    new MvException(string).getMessage(),
                    String.format(STRING_LABEL_VALUE_PAIR, APP_MV, string)
            );
        });
    }

    @Test
    void getMessage_NullMessage_AppNamePrependedMessage() {
        assertEquals(new MvException(null).getMessage(), String.format(STRING_LABEL_VALUE_PAIR, APP_MV, null));
    }

    @Test
    void getCause_ValidCause_SuppliedCause() {
        assertEquals(
                new MvException(STRING_SINGLE_WORD, EXCEPTION).getCause(),
                EXCEPTION
        );
    }

    @Test
    void getCause_NullCause_Null() {
        assertNull(new MvException(STRING_SINGLE_WORD).getCause());
        assertNull(new MvException(STRING_SINGLE_WORD, null).getCause());
    }
}