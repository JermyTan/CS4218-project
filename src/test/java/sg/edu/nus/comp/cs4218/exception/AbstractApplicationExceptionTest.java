package sg.edu.nus.comp.cs4218.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_LIST;

import org.junit.jupiter.api.Test;

@SuppressWarnings("PMD.AbstractNaming")
class AbstractApplicationExceptionTest {

    @Test
    void getMessage_AnyValidMessage_SuppliedMessage() {
        STRING_LIST.forEach(string -> {
            assertEquals(
                    string,
                    new AbstractApplicationException(string) {
                    }.getMessage()
            );
        });
    }

    @Test
    void getMessage_NullMessage_Null() {
        assertNull(new AbstractApplicationException(null) {
        }.getMessage());
    }
}