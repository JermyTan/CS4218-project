package sg.edu.nus.comp.cs4218.impl.util;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_MULTIPLE_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_REDIR_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_REDIR_OUTPUT;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

public class IORedirectionHandler {
    private final List<String> argsList;
    private final InputStream origInputStream;
    private final OutputStream origOutputStream;
    private List<String> noRedirArgsList = new ArrayList<>();
    private InputStream inputStream;
    private OutputStream outputStream;

    public IORedirectionHandler(List<String> argsList, InputStream origInputStream, OutputStream origOutputStream) throws ShellException {
        if (CollectionUtils.isAnyNull(argsList, origInputStream, origOutputStream) || argsList.isEmpty()) {
            throw new ShellException(ERR_SYNTAX);
        }

        this.argsList = argsList;
        this.inputStream = origInputStream;
        this.origInputStream = origInputStream;
        this.outputStream = origOutputStream;
        this.origOutputStream = origOutputStream;
    }

    public void extractRedirOptions() throws AbstractApplicationException, ShellException { //NOPMD
        noRedirArgsList = new ArrayList<>();

        // extract redirection operators (with their corresponding files) from argsList
        ListIterator<String> argsIterator = argsList.listIterator();
        while (argsIterator.hasNext()) {
            String arg = argsIterator.next();

            // leave the other args untouched
            if (!isRedirOperator(arg)) {
                noRedirArgsList.add(arg);
                continue;
            }

            // no file specified
            if (!argsIterator.hasNext()) {
                throw new ShellException(ERR_SYNTAX);
            }

            // if current arg is < or >, fast-forward to the next arg to extract the specified file
            String file = argsIterator.next();

            // consecutive redir operator
            if (isRedirOperator(file)) {
                throw new ShellException(ERR_SYNTAX);
            }

            // handle quoting + globing + command substitution in file arg
            List<String> fileSegment = ArgumentResolverUtil.resolveOneArgument(file);
            if (fileSegment.size() > 1) {
                // ambiguous redirect if file resolves to more than one parsed arg
                throw new ShellException(ERR_SYNTAX);
            }
            file = fileSegment.get(0);

            // replace existing inputStream / outputStream
            if (arg.equals(STRING_REDIR_INPUT)) {
                IOUtils.closeInputStream(inputStream);
                if (!inputStream.equals(origInputStream)) { // Already have a stream
                    throw new ShellException(ERR_MULTIPLE_STREAMS);
                }
                inputStream = IOUtils.openInputStream(file);
            } else if (arg.equals(STRING_REDIR_OUTPUT)) {
                IOUtils.closeOutputStream(outputStream);
                if (!outputStream.equals(origOutputStream)) { // Already have a stream
                    throw new ShellException(ERR_MULTIPLE_STREAMS);
                }
                outputStream = IOUtils.openOutputStream(file);
            }

            // check if there are multiple files specified for redirection
            if (argsIterator.hasNext()) {
                String nextArg = argsIterator.next();
                if (isRedirOperator(nextArg)) {
                    argsIterator.previous();
                } else {
                    throw new ShellException(ERR_SYNTAX);
                }
            }
        }
    }

    public List<String> getNoRedirArgsList() {
        return noRedirArgsList;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    private boolean isRedirOperator(String str) {
        return str.equals(STRING_REDIR_INPUT) || str.equals(STRING_REDIR_OUTPUT);
    }
}
