package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.STRING_EMPTY;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.ShellException;

class IOUtilsTest {

    private static final String ORIGINAL_DIR = Environment.currentDirectory;
    private static final String TEST_DIR = Environment.currentDirectory + File.separator + RESOURCES_PATH + File.separator + "IOUtilsTest";
    private final static String TEST_TXT = "test.txt";
    private final static String NON_EXISTENT_TEXT = "test" + File.separator + "test1.txt";

    @BeforeAll
    static void setupBeforeAll() {
        Environment.currentDirectory = TEST_DIR;
    }

    @AfterAll
    static void tearDownAfterAll() {
        Environment.currentDirectory = ORIGINAL_DIR;
    }

    @Test
    void openInputStream_ExistingFile_Success() {
        assertDoesNotThrow(() -> {
            IOUtils.openInputStream(TEST_TXT);
        });
    }

    @Test
    void openInputStream_NonExistingFile_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            IOUtils.openInputStream(NON_EXISTENT_TEXT);
        });
    }

    @Test
    void openOutputStream_ExistingFile_Success() {
        assertDoesNotThrow(() -> {
            IOUtils.openOutputStream(TEST_TXT);
        });
    }

    @Test
    void openOutputStream_NonExistingFile_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            IOUtils.openOutputStream(NON_EXISTENT_TEXT);
        });
    }

    @Test
    void closeInputStream_SystemIn_SystemInNotClosed() {
        assertDoesNotThrow(() -> {
            IOUtils.closeInputStream(System.in);
        });
    }

    @Test
    void closeInputStream_NullStream_DoNothing() {
        assertDoesNotThrow(() -> {
            IOUtils.closeInputStream(null);
        });
    }

    @Test
    void closeInputStream_NonNullStream_Success() {
        assertDoesNotThrow(() -> {
            IOUtils.closeInputStream(IOUtils.openInputStream(TEST_TXT));
        });
    }

    @Test
    void closeInputStream_UnclosableStream_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            IOUtils.closeInputStream(new UnclosableInputStream(TEST_TXT));
        });
    }

    @Test
    void closeOutputStream_SystemOut_SystemOutNotClosed() {
        assertDoesNotThrow(() -> {
            IOUtils.closeOutputStream(System.out);
            System.out.print(STRING_EMPTY);
        });
    }

    @Test
    void closeOutputStream_NullStream_DoNothing() {
        assertDoesNotThrow(() -> {
            IOUtils.closeOutputStream(null);
        });
    }

    @Test
    void closeOutputStream_NonNullStream_Success() {
        assertDoesNotThrow(() -> {
            IOUtils.closeOutputStream(IOUtils.openOutputStream(TEST_TXT));
        });
    }

    @Test
    void closeOutputStream_UnclosableStream_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            IOUtils.closeOutputStream(new UnclosableOutputStream(TEST_TXT));
        });
    }

    @Test
    void getLinesFromInputStream_ValidStream_ReturnsLinesFromStream() {
        String testString = "This is the start of the test file\n\ntesting 123     21212    12 12  .\ntesting 121\n\nThis is the end of the test file";
        List<String> expected = List.of(
                "This is the start of the test file",
                "",
                "testing 123     21212    12 12  .",
                "testing 121",
                "",
                "This is the end of the test file"
        );

        assertDoesNotThrow(() -> {
            OutputStream out = IOUtils.openOutputStream(TEST_TXT);
            out.write(testString.getBytes());
            out.close();

            assertIterableEquals(
                    expected,
                    IOUtils.getLinesFromInputStream(IOUtils.openInputStream(TEST_TXT))
            );
        });
    }

    @Test
    void getLinesFromInputStream_NullStream_ThrowsException() {
        assertThrows(ShellException.class, () -> {
            IOUtils.getLinesFromInputStream(null);
        });
    }

    private static class UnclosableInputStream extends FileInputStream {

        public UnclosableInputStream(String name) throws FileNotFoundException {
            super(name);
        }

        @Override
        public void close() throws IOException {
            throw new IOException("Unclosable");
        }
    }

    private static class UnclosableOutputStream extends FileOutputStream {

        public UnclosableOutputStream(String name) throws FileNotFoundException {
            super(name);
        }

        @Override
        public void close() throws IOException {
            throw new IOException("Unclosable");
        }
    }
}