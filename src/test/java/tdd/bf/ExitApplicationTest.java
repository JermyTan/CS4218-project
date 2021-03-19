package tdd.bf;

import java.security.Permission;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.impl.app.ExitApplication;


public class ExitApplicationTest {

    //Referenced from https://stackoverflow.com/questions/309396/java-how-to-test-methods-that-call-system-exit
    private static class NoExitSecurityManager extends SecurityManager {
        @Override
        public void checkPermission(Permission permission) {

        }

        @Override
        public void checkPermission(Permission permission, Object object) {

        }

        @Override
        public void checkExit(int status) {
            super.checkExit(status);
            throw new testExitException(status);
        }
    }

    private static class testExitException extends SecurityException {
        final int status;

        testExitException(int status) {
            super("0");
            this.status = status;
        }
    }

    @Test
    void run_NoArgument_ExitsSuccessfully() {
        System.setSecurityManager(new NoExitSecurityManager());

        try {
            String[] args = {};
            new ExitApplication().run(args, System.in, System.out);
            System.setSecurityManager(null);
        } catch (testExitException e) {
            assertEquals(0, e.status);
            System.setSecurityManager(null);
        }

        System.setSecurityManager(null);
    }

    @Test
    void run_WithArgument_ExitsSuccessfully() {
        System.setSecurityManager(new NoExitSecurityManager());

        try {
            String[] args = {"something"};
            new ExitApplication().run(args, System.in, System.out);
            System.setSecurityManager(null);
        } catch (testExitException e) {
            assertEquals(0, e.status);
            System.setSecurityManager(null);
        }

        System.setSecurityManager(null);
    }
}
