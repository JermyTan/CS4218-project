package sg.edu.nus.comp.cs4218.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.APP_ECHO;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.EXCEPTION;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_LIST;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_SINGLE_WORD;

import org.junit.jupiter.api.Test;

class EchoExceptionTest {
    @Test
    void abstractSuperClass_Initialization_ReturnsTrue() {
        assertTrue(new EchoException(STRING_SINGLE_WORD) instanceof AbstractApplicationException);
    }

    @Test
    void getMessage_AnyValidMessage_AppNamePrependedMessage() {
        STRING_LIST.forEach(string -> {
            assertEquals(
                    new EchoException(string).getMessage(),
                    String.format(STRING_LABEL_VALUE_PAIR, APP_ECHO, string)
            );
        });
    }

    @Test
    void getMessage_NullMessage_AppNamePrependedMessage() {
        assertEquals(new EchoException(null).getMessage(), String.format(STRING_LABEL_VALUE_PAIR, APP_ECHO, null));
    }

    @Test
    void getCause_ValidCause_SuppliedCause() {
        assertEquals(
                new EchoException(STRING_SINGLE_WORD, EXCEPTION).getCause(),
                EXCEPTION
        );
    }

    @Test
    void getCause_NullCause_Null() {
        assertNull(new EchoException(STRING_SINGLE_WORD).getCause());
        assertNull(new EchoException(STRING_SINGLE_WORD, null).getCause());
    }
}