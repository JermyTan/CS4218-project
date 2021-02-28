package sg.edu.nus.comp.cs4218.impl.app;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.InputStream;
import java.io.OutputStream;

import org.junit.jupiter.api.Test;

import sg.edu.nus.comp.cs4218.impl.util.SystemExit;


class ExitApplicationTest {

    private final SystemExit systemExit = mock(SystemExit.class);

    @Test
    public void run_MockClass_TerminateExecutionCalled() {
        ExitApplication app = spy(new ExitApplication(systemExit));
        InputStream inputStream = mock(InputStream.class);//NOPMD
        OutputStream outputStream = mock(OutputStream.class);//NOPMD

        app.run(new String[0], inputStream, outputStream);

        verify(app).terminateExecution();
        verifyNoInteractions(inputStream);
        verifyNoInteractions(outputStream);
    }

    @Test
    public void terminateExecution_MockClass_SystemExitWithCorrectStatus() {
        ExitApplication app = new ExitApplication(systemExit);

        app.terminateExecution();

        verify(systemExit).exit(0);
    }
}