package sg.edu.nus.comp.cs4218.impl;

import sg.edu.nus.comp.cs4218.*;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.util.*;

import java.io.*;

public class ShellImpl implements Shell {

    /**
     * Main method for the Shell Interpreter program.
     *
     * @param args List of strings arguments, unused.
     */
    public static void main(String... args) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));//NOPMD
        Shell shell = new ShellImpl();

        while (true) {
            try {
                String commandString;

                try {
                    try {
                        // small delay to let system.err finish output to console
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }

                    System.out.print("$ ");
                    commandString = reader.readLine();
                } catch (IOException e) {
                    reader.close();
                    break; // Streams are closed, terminate process
                }

                if (!StringUtils.isBlank(commandString)) {
                    shell.parseAndEvaluate(commandString, System.out);
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    @Override
    public void parseAndEvaluate(String commandString, OutputStream stdout)
            throws AbstractApplicationException, ShellException {
        Command command = CommandBuilderUtil.parseCommand(commandString, new ApplicationRunner());
        command.evaluate(System.in, stdout);
    }
}
