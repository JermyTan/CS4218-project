package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArgumentResolverTest {
    private ArgumentResolver argumentResolver;

    @BeforeEach
    void setup() {
        argumentResolver = new ArgumentResolver();
    }

    @Test
    public void resolveOneArgument_Unquoted_Unmodified() {
        String arg = "echo";
        assertDoesNotThrow(() -> {
            List<String> args = argumentResolver.resolveOneArgument(arg);
            assertEquals(1, args.size());
            assertEquals("echo", args.get(0));
        });
    }

    @Test
    public void resolveOneArgument_SingleQuote_QuoteUnwrapped() {
        String arg = "'hello world'";
        assertDoesNotThrow(() -> {
            List<String> args = argumentResolver.resolveOneArgument(arg);
            assertEquals(1, args.size());
            assertEquals("hello world", args.get(0));
        });
    }

    @Test
    public void resolveOneArgument_DoubleQuote_QuoteUnwrapped() {
        String arg = "\"hello world\"";
        assertDoesNotThrow(() -> {
            List<String> args = argumentResolver.resolveOneArgument(arg);
            assertEquals(1, args.size());
            assertEquals("hello world", args.get(0));
        });
    }

    @Test
    public void resolveOneArgument_DoubleQuoteWithinSingleQuote_DoubleQuoteRemains() {
        String arg = "'hello \"world\"'";
        assertDoesNotThrow(() -> {
            List<String> args = argumentResolver.resolveOneArgument(arg);
            assertEquals(1, args.size());
            assertEquals("hello \"world\"", args.get(0));
        });
    }

    @Test
    public void resolveOneArgument_BackQuoteWithinSingleQuote_BackQuoteRemains() {
        String arg = "'hello `world`'";
        assertDoesNotThrow(() -> {
            List<String> args = argumentResolver.resolveOneArgument(arg);
            assertEquals(1, args.size());
            assertEquals("hello `world`", args.get(0));
        });
    }

    @Test
    public void resolveOneArgument_BackQuoteWithinDoubleQuoteWithinSingleQuote_BothQuotesRemains() {
        String arg = "'h\"el`lo wo`rl\"d'";
        assertDoesNotThrow(() -> {
            List<String> args = argumentResolver.resolveOneArgument(arg);
            assertEquals(1, args.size());
            assertEquals("h\"el`lo wo`rl\"d", args.get(0));
        });
    }

    @Test
    public void resolveOneArgument_SingleQuoteWithinDoubleQuote_SingleQuoteRemains() {
        String arg = "\"hello 'world'\"";
        assertDoesNotThrow(() -> {
            List<String> args = argumentResolver.resolveOneArgument(arg);
            assertEquals(1, args.size());
            assertEquals("hello 'world'", args.get(0));
        });
    }
}