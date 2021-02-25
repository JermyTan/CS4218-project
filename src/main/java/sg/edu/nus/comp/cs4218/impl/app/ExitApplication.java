package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.ExitInterface;
import sg.edu.nus.comp.cs4218.exception.ExitException;

import java.io.InputStream;
import java.io.OutputStream;

public class ExitApplication implements ExitInterface {

    /**
     * Runs the exit application.
     *
     * @param args array of arguments for the application, not used.
     * @param stdin an InputStream, not used.
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
        System.exit(0);
    }
}
