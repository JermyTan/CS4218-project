package sg.edu.nus.comp.cs4218.impl.app;

import java.io.InputStream;
import java.io.OutputStream;

import sg.edu.nus.comp.cs4218.app.ExitInterface;
import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.impl.util.SystemExit;

public class ExitApplication implements ExitInterface {

    private final SystemExit systemExit;

    public ExitApplication() {
        this(new SystemExit());
    }

    public ExitApplication(SystemExit systemExit) {
        this.systemExit = systemExit;
    }

    /**
     * Runs the exit application.
     *
     * @param args   array of arguments for the application, not used.
     * @param stdin  an InputStream, not used.
     * @param stdout an OutputStream, not used.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) {
        // Format: exit
        terminateExecution();
    }

    /**
     * Terminates shell.
     *
     * @throws ExitException
     */
    @Override
    public void terminateExecution() {
        systemExit.exit(0);
    }
}
