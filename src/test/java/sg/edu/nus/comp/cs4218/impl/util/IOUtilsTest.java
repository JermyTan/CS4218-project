package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_FILE_SEP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutil.TestConstants.RESOURCES_PATH;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.EnvironmentUtil;
import sg.edu.nus.comp.cs4218.exception.ShellException;

class IOUtilsTest {

    private static final String ORIGINAL_DIR = EnvironmentUtil.currentDirectory;
    private static final String TEST_DIR = EnvironmentUtil.currentDirectory + STRING_FILE_SEP + RESOURCES_PATH + STRING_FILE_SEP + "IOUtilsTest";
    private static final String TEST_TXT = "test.txt";
    private static final String NON_EXISTENT_TEXT = "test" + STRING_FILE_SEP + "test1.txt";

    @BeforeAll
    static void setupBeforeAll() {
        EnvironmentUtil.currentDirectory = TEST_DIR;
    }

    @AfterAll
    static void tearDownAfterAll() {
        EnvironmentUtil.currentDirectory = ORIGINAL_DIR;
    }

    @Test
    void openInputStream_ExistingFile_Success() {
        assertDoesNotThrow(() ->
                IOUtils.openInputStream(
                        IOUtils.resolveAbsoluteFilePath(TEST_TXT).toString()
                )
        );
    }

    @Test
    void openInputStream_NonExistingFile_ThrowsException() {
        assertThrows(ShellException.class, () ->
                IOUtils.openInputStream(
                        IOUtils.resolveAbsoluteFilePath(NON_EXISTENT_TEXT).toString()
                )
        );
    }

    @Test
    void openOutputStream_ExistingFile_Success() {
        assertDoesNotThrow(() ->
                IOUtils.openOutputStream(
                        IOUtils.resolveAbsoluteFilePath(TEST_TXT).toString()
                )
        );
    }

    @Test
    void openOutputStream_NonExistingFile_ThrowsException() {
        assertThrows(ShellException.class, () ->
                IOUtils.openOutputStream(
                        IOUtils.resolveAbsoluteFilePath(NON_EXISTENT_TEXT).toString()
                )
        );
    }

    @Test
    void closeInputStream_SystemIn_SystemInNotClosed() {
        assertDoesNotThrow(() ->
                IOUtils.closeInputStream(System.in)
        );
    }

    @Test
    void closeInputStream_NullStream_DoesNothing() {
        assertDoesNotThrow(() ->
                IOUtils.closeInputStream(null)
        );
    }

    @Test
    void closeInputStream_NonNullStream_Success() {
        assertDoesNotThrow(() ->
                IOUtils.closeInputStream(
                        IOUtils.openInputStream(
                                IOUtils.resolveAbsoluteFilePath(TEST_TXT).toString()
                        )
                )
        );
    }

    @Test
    void closeInputStream_UnclosableStream_ThrowsException() {
        assertThrows(ShellException.class, () ->
                IOUtils.closeInputStream(
                        new UnclosableInputStream(
                                IOUtils.resolveAbsoluteFilePath(TEST_TXT).toString()
                        )
                )
        );
    }

    @Test
    void closeOutputStream_SystemOut_SystemOutNotClosed() {
        assertDoesNotThrow(() -> {
            IOUtils.closeOutputStream(System.out);
            System.out.print(STRING_EMPTY);
        });
    }

    @Test
    void closeOutputStream_NullStream_DoesNothing() {
        assertDoesNotThrow(() ->
                IOUtils.closeOutputStream(null)
        );
    }

    @Test
    void closeOutputStream_NonNullStream_Success() {
        assertDoesNotThrow(() ->
                IOUtils.closeOutputStream(
                        IOUtils.openOutputStream(
                                IOUtils.resolveAbsoluteFilePath(TEST_TXT).toString()
                        )
                )
        );
    }

    @Test
    void closeOutputStream_UnclosableStream_ThrowsException() {
        assertThrows(ShellException.class, () ->
                IOUtils.closeOutputStream(
                        new UnclosableOutputStream(
                                IOUtils.resolveAbsoluteFilePath(TEST_TXT).toString()
                        )
                )
        );
    }

    @Test
    void getLinesFromInputStream_ValidStream_ReturnsLinesFromStream() {
        String testString = "This is the start of the test file"
                + STRING_NEWLINE
                + STRING_NEWLINE
                + "testing 123     21212    12 12  ."
                + STRING_NEWLINE
                + "testing 121"
                + STRING_NEWLINE
                + STRING_NEWLINE
                +"This is the end of the test file";
        List<String> expected = List.of(
                "This is the start of the test file",
                STRING_EMPTY,
                "testing 123     21212    12 12  .",
                "testing 121",
                STRING_EMPTY,
                "This is the end of the test file"
        );

        assertDoesNotThrow(() -> {
            OutputStream out = IOUtils.openOutputStream(
                    IOUtils.resolveAbsoluteFilePath(TEST_TXT).toString()
            );
            out.write(testString.getBytes());
            out.close();

            assertIterableEquals(
                    expected,
                    IOUtils.getLinesFromInputStream(
                            IOUtils.openInputStream(
                                    IOUtils.resolveAbsoluteFilePath(TEST_TXT).toString()
                            )
                    )
            );
        });
    }

    @Test
    void getLinesFromInputStream_NullStream_ThrowsException() {
        assertThrows(Exception.class, () ->
                IOUtils.getLinesFromInputStream(null)
        );
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