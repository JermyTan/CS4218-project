package sg.edu.nus.comp.cs4218.impl.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class ArgumentResolverUtilTest {
    @Test
    void parseArguments_SingleQuote_Success() {
        assertDoesNotThrow(() -> {
            List<String> parsedArgsList = ArgumentResolverUtil.parseArguments(List.of("'awesome'"));
            assertEquals(List.of("awesome"), parsedArgsList);
        });
    }

    @Test
    void parseArguments_DoubleQuote_Success() {
        assertDoesNotThrow(() -> {
            List<String> parsedArgsList = ArgumentResolverUtil.parseArguments(List.of("\"test\""));
            assertEquals(List.of("test"), parsedArgsList);
        });
    }

    @Test
    void parseArguments_BackQuote_Success() {
        assertDoesNotThrow(() -> {
            List<String> parsedArgsList = ArgumentResolverUtil.parseArguments(List.of("`echo 'hello'`"));
            assertEquals(List.of("hello"), parsedArgsList);
        });
    }

    @Test
    void parseArguments_BackQuoteWithDoubleQuote_Success() {
        assertDoesNotThrow(() -> {
            List<String> parsedArgsList = ArgumentResolverUtil.parseArguments(List.of("\"`echo 'bye'`\""));
            assertEquals(Arrays.asList("bye"), parsedArgsList);
        });
    }

    @Test
    void parseArguments_BackQuoteWithSingleQuote_Success() {
        assertDoesNotThrow(() -> {
            List<String> parsedArgsList = ArgumentResolverUtil.parseArguments(List.of("'`echo 'world'`'"));
            assertEquals(List.of("`echo world`"), parsedArgsList);
        });
    }

    // assume to return empty
    @Test
    void parseArguments_SpecialSymbolWithDoubleQuote_ReturnsEmpty() {
        assertDoesNotThrow(() -> {
            List<String> parsedArgsList = ArgumentResolverUtil.parseArguments(List.of("\"`|>_<;\""));
            assertEquals(List.of(), parsedArgsList);
        });
    }

    @Test
    void parseArguments_SpecialSymbolWithSingleQuote_Success() {
        assertDoesNotThrow(() -> {
            List<String> parsedArgsList = ArgumentResolverUtil.parseArguments(List.of("'`|>_<;'"));
            assertEquals(List.of("`|>_<;"), parsedArgsList);
        });
    }

    @Test
    void parseArguments_MixSingleDoubleQuotes_Success() {
        assertDoesNotThrow(() -> {
            List<String> parsedArgsList = ArgumentResolverUtil.parseArguments(List.of("\"bye'hello\"world'"));
            assertEquals(List.of("bye'helloworld"), parsedArgsList);
        });
    }

    @Test
    void parseArguments_AllThreeQuotes1_Success() {
        assertDoesNotThrow(() -> {
            List<String> parsedArgsList = ArgumentResolverUtil.parseArguments(List.of("\"'hello `echo \"world\"`'\""));
            assertEquals(List.of("'hello world'"), parsedArgsList);
        });
    }

    @Test
    void parseArguments_AllThreeQuotes2_Success() {
        assertDoesNotThrow(() -> {
            List<String> parsedArgsList = ArgumentResolverUtil.parseArguments(List.of("'\"hello `echo \"world\"`\"'"));
            assertEquals(List.of("\"hello `echo \"world\"`\""), parsedArgsList);
        });
    }

    @Test
    void parseArguments_AllThreeQuotes3_Success() {
        assertDoesNotThrow(() -> {
            List<String> parsedArgsList = ArgumentResolverUtil.parseArguments(List.of("\"hello `echo '\"world'`"));
            assertEquals(List.of("hello \"world"), parsedArgsList);
        });
    }

    @Test
    public void resolveOneArgument_Unquoted_Unmodified() {
        String arg = "echo";
        assertDoesNotThrow(() -> {
            List<String> args = ArgumentResolverUtil.resolveOneArgument(arg);
            assertEquals(1, args.size());
            assertEquals("echo", args.get(0));
        });
    }

    @Test
    public void resolveOneArgument_SingleQuote_QuoteUnwrapped() {
        String arg = "'hello world'";
        assertDoesNotThrow(() -> {
            List<String> args = ArgumentResolverUtil.resolveOneArgument(arg);
            assertEquals(1, args.size());
            assertEquals("hello world", args.get(0));
        });
    }

    @Test
    public void resolveOneArgument_DoubleQuote_QuoteUnwrapped() {
        String arg = "\"hello world\"";
        assertDoesNotThrow(() -> {
            List<String> args = ArgumentResolverUtil.resolveOneArgument(arg);
            assertEquals(1, args.size());
            assertEquals("hello world", args.get(0));
        });
    }

    @Test
    public void resolveOneArgument_DoubleQuoteWithinSingleQuote_DoubleQuoteRemains() {
        String arg = "'hello \"world\"'";
        assertDoesNotThrow(() -> {
            List<String> args = ArgumentResolverUtil.resolveOneArgument(arg);
            assertEquals(1, args.size());
            assertEquals("hello \"world\"", args.get(0));
        });
    }

    @Test
    public void resolveOneArgument_BackQuoteWithinSingleQuote_BackQuoteRemains() {
        String arg = "'hello `world`'";
        assertDoesNotThrow(() -> {
            List<String> args = ArgumentResolverUtil.resolveOneArgument(arg);
            assertEquals(1, args.size());
            assertEquals("hello `world`", args.get(0));
        });
    }

    @Test
    public void resolveOneArgument_BackQuoteWithinDoubleQuoteWithinSingleQuote_BothQuotesRemain() {
        String arg = "'h\"el`lo wo`rl\"d'";
        assertDoesNotThrow(() -> {
            List<String> args = ArgumentResolverUtil.resolveOneArgument(arg);
            assertEquals(1, args.size());
            assertEquals("h\"el`lo wo`rl\"d", args.get(0));
        });
    }

    @Test
    public void resolveOneArgument_SingleQuoteWithinDoubleQuote_SingleQuoteRemains() {
        String arg = "\"hello 'world'\"";
        assertDoesNotThrow(() -> {
            List<String> args = ArgumentResolverUtil.resolveOneArgument(arg);
            assertEquals(1, args.size());
            assertEquals("hello 'world'", args.get(0));
        });
    }

    @Test
    public void resolveOneArgument_QuotedAsterisk_AsteriskRemains() {
        String arg = "\"hello *\"";
        assertDoesNotThrow(() -> {
            List<String> args = ArgumentResolverUtil.resolveOneArgument(arg);
            assertEquals(1, args.size());
            assertEquals("hello *", args.get(0));
        });
    }
}