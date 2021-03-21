package sg.edu.nus.comp.cs4218.impl.util;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_ASTERISK;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_BACK_QUOTE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_DOUBLE_QUOTE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_SINGLE_QUOTE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_SPACE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_EMPTY;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

public final class ArgumentResolverUtil {
    private ArgumentResolverUtil() {
    }

    /**
     * Handle quoting + globing + command substitution for a list of arguments.
     *
     * @param argsList The original list of arguments.
     * @return The list of parsed arguments.
     * @throws ShellException If any of the arguments have an invalid syntax.
     */
    public static List<String> parseArguments(List<String> argsList) throws AbstractApplicationException, ShellException {
        List<String> parsedArgsList = new ArrayList<>();
        for (String arg : argsList) {
            parsedArgsList.addAll(resolveOneArgument(arg));
        }
        return parsedArgsList;
    }

    /**
     * Unwraps single and double quotes from one argument.
     * Performs globing when there are unquoted asterisks.
     * Performs command substitution.
     * <p>
     * Single quotes disable the interpretation of all special characters.
     * Double quotes disable the interpretation of all special characters, except for back quotes.
     *
     * @param arg String containing one argument.
     * @return A list containing one or more parsed args, depending on the outcome of the parsing.
     */
    public static List<String> resolveOneArgument(String arg) throws AbstractApplicationException, ShellException { //NOPMD
        Stack<Character> unmatchedQuotes = new Stack<>();
        ArrayList<RegexArgument> parsedArgsSegment = new ArrayList<>();
        RegexArgument parsedArg = new RegexArgument();
        StringBuilder subCommand = new StringBuilder();
/*
        for (char chr : arg.toCharArray()) {
            switch (chr) {
            case CHAR_BACK_QUOTE:
                if (unmatchedQuotes.isEmpty() || unmatchedQuotes.peek() == CHAR_DOUBLE_QUOTE) {
                    // start of command substitution

                }
            case CHAR_SINGLE_QUOTE:
            case CHAR_DOUBLE_QUOTE:
            case CHAR_ASTERISK:
            default:
            }
        }
*/
        for (int i = 0; i < arg.length(); i++) {
            char chr = arg.charAt(i);

            if (chr == CHAR_BACK_QUOTE) {
                if (unmatchedQuotes.isEmpty() || unmatchedQuotes.peek() == CHAR_DOUBLE_QUOTE) {
                    // start of command substitution
                    if (!parsedArg.isEmpty()) {
                        appendParsedArgIntoSegment(parsedArgsSegment, parsedArg);
                        parsedArg = new RegexArgument();
                    }

                    unmatchedQuotes.add(chr);

                } else if (unmatchedQuotes.peek() == chr) {
                    // end of command substitution
                    unmatchedQuotes.pop();

                    // evaluate subCommand and get the output
                    String subCommandOutput = evaluateSubCommand(subCommand.toString());
                    subCommand.setLength(0); // Clear the previous subCommand registered

                    // check if back quotes are nested
                    if (unmatchedQuotes.isEmpty()) {
                        List<RegexArgument> subOutputSegment = Stream
                                .of(StringUtils.tokenize(subCommandOutput))
                                .map(str -> new RegexArgument(str))
                                .collect(Collectors.toList());

                        // append the first token to the previous parsedArg
                        // e.g. arg: abc`1 2 3`xyz`4 5 6` (contents in `` is after command sub)
                        // expected: [abc1, 2, 3xyz4, 5, 6]
                        if (!subOutputSegment.isEmpty()) {
                            RegexArgument firstOutputArg = subOutputSegment.remove(0);
                            appendParsedArgIntoSegment(parsedArgsSegment, firstOutputArg);
                        }

                    } else {
                        // don't tokenize subCommand output
                        appendParsedArgIntoSegment(parsedArgsSegment, new RegexArgument(subCommandOutput));
                    }
                } else {
                    // ongoing single quote
                    parsedArg.append(chr);
                }
            } else if (chr == CHAR_SINGLE_QUOTE || chr == CHAR_DOUBLE_QUOTE) {
                if (unmatchedQuotes.isEmpty()) {
                    // start of quote
                    unmatchedQuotes.add(chr);
                } else if (unmatchedQuotes.peek() == chr) {
                    // end of quote
                    unmatchedQuotes.pop();

                    // make sure parsedArgsSegment is not empty
                    appendParsedArgIntoSegment(parsedArgsSegment, new RegexArgument());
                } else if (unmatchedQuotes.peek() == CHAR_BACK_QUOTE) {
                    // ongoing back quote: add chr to subCommand
                    subCommand.append(chr);
                } else {
                    // ongoing single/double quote
                    parsedArg.append(chr);
                }
            } else if (chr == CHAR_ASTERISK) {
                if (unmatchedQuotes.isEmpty()) {
                    // each unquoted * matches a (possibly empty) sequence of non-slash chars
                    parsedArg.appendAsterisk();
                } else if (unmatchedQuotes.peek() == CHAR_BACK_QUOTE) {
                    // ongoing back quote: add chr to subCommand
                    subCommand.append(chr);
                } else {
                    // ongoing single/double quote
                    parsedArg.append(chr);
                }
            } else {
                if (unmatchedQuotes.isEmpty()) {
                    // not a special character
                    parsedArg.append(chr);
                } else if (unmatchedQuotes.peek() == CHAR_BACK_QUOTE) {
                    // ongoing back quote: add chr to subCommand
                    subCommand.append(chr);
                } else {
                    // ongoing single/double quote
                    parsedArg.append(chr);
                }
            }
        }

        if (!parsedArg.isEmpty()) {
            appendParsedArgIntoSegment(parsedArgsSegment, parsedArg);
        }

        // perform globing
        return parsedArgsSegment.stream()
                .flatMap(regexArgument -> regexArgument.globFiles().stream())
                .collect(Collectors.toList());
    }

    private static String evaluateSubCommand(String commandString) throws AbstractApplicationException, ShellException {
        if (StringUtils.isBlank(commandString)) {
            return STRING_EMPTY;
        }

        OutputStream outputStream = new ByteArrayOutputStream();
        Command command = CommandBuilderUtil.parseCommand(commandString, new ApplicationRunner());
        command.evaluate(System.in, outputStream);

        // replace newlines with spaces
        return outputStream.toString().replace(STRING_NEWLINE, String.valueOf(CHAR_SPACE));
    }

    /**
     * Append current parsedArg to the last parsedArg in parsedArgsSegment.
     * If parsedArgsSegment is empty, then just add current parsedArg.
     */
    private static void appendParsedArgIntoSegment(List<RegexArgument> parsedArgsSegment,
                                                   RegexArgument parsedArg) {
        if (parsedArgsSegment.isEmpty()) {
            parsedArgsSegment.add(parsedArg);
        } else {
            RegexArgument lastParsedArg = parsedArgsSegment.remove(parsedArgsSegment.size() - 1);
            parsedArgsSegment.add(lastParsedArg);
            lastParsedArg.merge(parsedArg);
        }
    }
}
