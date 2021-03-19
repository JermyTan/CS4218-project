package sg.edu.nus.comp.cs4218.impl.util;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_PIPE;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_REDIR_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_REDIR_OUTPUT;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_SEMICOLON;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.SequenceCommand;

public final class CommandBuilderHelper {
    /**
     * Regular expression for extracting valid arguments from the command string:
     * (NO_QUOTE | SINGLE_QUOTE | NESTED_BACK_QUOTE | DOUBLE_QUOTE | BACK_QUOTE)+
     * <p>
     * The order matters because it affects the matching priority.
     * <p>
     * NO_QUOTE: [^'\"`|<>;\\s]+
     * SINGLE_QUOTE: '[^']*'
     * NESTED_BACK_QUOTE: \"([^\"`]*`.*?`[^\"`]*)+\"
     * DOUBLE_QUOTE: \"[^\"]*\"
     * BACK_QUOTE: `[^`]*`
     */
    private static final Pattern ARGUMENT_REGEX = Pattern
            .compile("([^'\"`|<>;\\s]+|'[^']*'|\"([^\"`]*`.*?`[^\"`]*)+\"|\"[^\"]*\"|`[^`]*`)+");

    private CommandBuilderHelper() {
    }

    private static void handleRedirChar(char redirChar, List<String> tokens) throws ShellException {
        if (tokens.isEmpty()) {
            // cannot start a new command with redirection
            throw new ShellException(ERR_SYNTAX);
        } else {
            // add as a separate token on its own
            tokens.add(String.valueOf(redirChar));
        }
    }

    private static void handlePipeChar(
            ApplicationRunner appRunner,
            ArgumentResolver argumentResolver,
            List<CallCommand> callCmdsForPipe,
            List<String> tokens
    ) throws ShellException {
        if (tokens.isEmpty()) {
            // cannot start a new command with pipe
            throw new ShellException(ERR_SYNTAX);
        } else {
            // add CallCommand as part of a PipeCommand
            callCmdsForPipe.add(new CallCommand(tokens, appRunner, argumentResolver));
            tokens.clear();
        }
    }

    private static void handleSemicolon(
            ApplicationRunner appRunner,
            ArgumentResolver argumentResolver,
            List<Command> cmdsForSequence,
            List<CallCommand> callCmdsForPipe,
            List<String> tokens
    ) throws ShellException {
        if (tokens.isEmpty()) {
            // cannot start a new command with semicolon
            throw new ShellException(ERR_SYNTAX);
        } else if (callCmdsForPipe.isEmpty()) {
            // add CallCommand as part of a SequenceCommand
            cmdsForSequence.add(new CallCommand(tokens, appRunner, argumentResolver));
            tokens.clear();
        } else {
            // add CallCommand as part of ongoing PipeCommand
            callCmdsForPipe.add(new CallCommand(tokens, appRunner, argumentResolver));
            tokens.clear();

            // add PipeCommand as part of a SequenceCommand
            cmdsForSequence.add(new PipeCommand(callCmdsForPipe));
            callCmdsForPipe.clear();
        }
    }

    private static void processSpecialChar(
            char firstChar,
            ApplicationRunner appRunner,
            ArgumentResolver argumentResolver,
            List<Command> cmdsForSequence,
            List<CallCommand> callCmdsForPipe,
            List<String> tokens
    ) throws ShellException {
        switch (firstChar) {
        case CHAR_REDIR_INPUT:
        case CHAR_REDIR_OUTPUT:
            handleRedirChar(firstChar, tokens);
            break;
        case CHAR_PIPE:
            handlePipeChar(appRunner, argumentResolver, callCmdsForPipe, tokens);
            break;
        case CHAR_SEMICOLON:
            handleSemicolon(appRunner, argumentResolver, cmdsForSequence, callCmdsForPipe, tokens);
            break;
        default:
            // encountered a mismatched quote
            throw new ShellException(ERR_SYNTAX);
        }
    }

    /**
     * Parses and tokenizes the provided command string into command(s) and arguments.
     * <p>
     * CallCommand takes in a list of tokens, PipeCommand takes in a list of CallCommands,
     * and SequenceCommand takes in a list of CallCommands / PipeCommands.
     *
     * @return Final command to be evaluated.
     * @throws ShellException If the provided command string has an invalid syntax.
     */
    public static Command parseCommand(String commandString, ApplicationRunner appRunner)
            throws ShellException {
        if (StringUtils.isBlank(commandString) || commandString.contains(STRING_NEWLINE)) {
            throw new ShellException(ERR_SYNTAX);
        }

        ArgumentResolver argumentResolver = new ArgumentResolver();
        List<Command> cmdsForSequence = new ArrayList<>();
        List<CallCommand> callCmdsForPipe = new ArrayList<>();
        List<String> tokens = new ArrayList<>();

        String commandSubstring = commandString;
        while (!StringUtils.isBlank(commandSubstring)) {
            commandSubstring = commandSubstring.trim();
            Matcher matcher = ARGUMENT_REGEX.matcher(commandSubstring);

            // no valid arguments found
            if (!matcher.find()) {
                throw new ShellException(ERR_SYNTAX);
            }

            // found a valid argument at the start of the command substring
            if (matcher.start() == 0) {
                tokens.add(matcher.group());
                commandSubstring = commandSubstring.substring(matcher.end());
                continue;
            }

            // found a valid argument but not at the start of the command substring
            char firstChar = commandSubstring.charAt(0);
            commandSubstring = commandSubstring.substring(1);

            processSpecialChar(firstChar, appRunner, argumentResolver, cmdsForSequence, callCmdsForPipe, tokens);
        }

        Command finalCommand = new CallCommand(tokens, appRunner, argumentResolver);
        if (!callCmdsForPipe.isEmpty()) {
            // add CallCommand as part of ongoing PipeCommand
            callCmdsForPipe.add((CallCommand) finalCommand);
            finalCommand = new PipeCommand(callCmdsForPipe);
        }
        if (!cmdsForSequence.isEmpty()) {
            // add CallCommand / PipeCommand as part of ongoing SequenceCommand
            cmdsForSequence.add(finalCommand);
            finalCommand = new SequenceCommand(cmdsForSequence);
        }

        return finalCommand;
    }
}
