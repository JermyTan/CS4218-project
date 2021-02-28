package sg.edu.nus.comp.cs4218.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_LABEL_VALUE_PAIR;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.EXCEPTION;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_LIST;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_SINGLE_WORD;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_SPACE_FILE_TXT;

import org.junit.jupiter.api.Test;

class InvalidDirectoryExceptionTest {

    @Test
    void getMessage_ValidStringDirectoryWithAnyValidMessage_DirectoryPrependedMessage() {
        STRING_LIST.forEach(string -> {
            assertEquals(
                    new InvalidDirectoryException(STRING_SPACE_FILE_TXT, string).getMessage(),
                    String.format(STRING_LABEL_VALUE_PAIR, STRING_SPACE_FILE_TXT, string)
            );
        });
    }

    @Test
    void getMessage_NullDirectoryWithAnyValidMessage_DirectoryPrependedMessage() {
        STRING_LIST.forEach(string -> {
            assertEquals(
                    new InvalidDirectoryException(null, string).getMessage(),
                    String.format(STRING_LABEL_VALUE_PAIR, null, string)
            );
        });
    }

    @Test
    void getMessage_ValidStringDirectoryWithNullMessage_DirectoryPrependedMessage() {
        assertEquals(
                new InvalidDirectoryException(STRING_SPACE_FILE_TXT, null).getMessage(),
                String.format(STRING_LABEL_VALUE_PAIR, STRING_SPACE_FILE_TXT, null));
    }

    @Test
    void getMessage_NullDirectoryWithNullMessage_DirectoryPrependedMessage() {
        assertEquals(
                new InvalidDirectoryException(null, null).getMessage(),
                String.format(STRING_LABEL_VALUE_PAIR, null, null));
    }

    @Test
    void getCause_ValidCause_SuppliedCause() {
        assertEquals(
                new InvalidDirectoryException(STRING_SPACE_FILE_TXT, STRING_SINGLE_WORD, EXCEPTION).getCause(),
                EXCEPTION
        );
    }

    @Test
    void getCause_NullCause_Null() {
        assertNull(new InvalidDirectoryException(STRING_SPACE_FILE_TXT, STRING_SINGLE_WORD).getCause());
        assertNull(new InvalidDirectoryException(STRING_SPACE_FILE_TXT, STRING_SINGLE_WORD, null).getCause());
    }
}