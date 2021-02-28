package sg.edu.nus.comp.cs4218.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_EXIT;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.EXCEPTION;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_LIST;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_SINGLE_WORD;

import org.junit.jupiter.api.Test;

class ExitExceptionTest {
    @Test
    void abstractSuperClass_Initialization_AbstractApplicationException() {
        assertTrue(new ExitException(STRING_SINGLE_WORD) instanceof AbstractApplicationException);
    }

    @Test
    void getMessage_AnyValidMessage_AppNamePrependedMessage() {
        STRING_LIST.forEach(string -> {
            assertEquals(
                    String.format(STRING_LABEL_VALUE_PAIR, APP_EXIT, string),
                    new ExitException(string).getMessage()
            );
        });
    }

    @Test
    void getMessage_NullMessage_AppNamePrependedMessage() {
        assertEquals(String.format(STRING_LABEL_VALUE_PAIR, APP_EXIT, null), new ExitException(null).getMessage());
    }

    @Test
    void getCause_ValidCause_SuppliedCause() {
        assertEquals(
                EXCEPTION,
                new ExitException(STRING_SINGLE_WORD, EXCEPTION).getCause()
        );
    }

    @Test
    void getCause_NullCause_Null() {
        assertNull(new ExitException(STRING_SINGLE_WORD).getCause());
        assertNull(new ExitException(STRING_SINGLE_WORD, null).getCause());
    }
}