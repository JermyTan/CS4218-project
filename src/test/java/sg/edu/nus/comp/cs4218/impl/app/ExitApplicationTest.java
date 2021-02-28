package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.impl.util.SystemExit;

import java.io.InputStream;
import java.io.OutputStream;

import static org.mockito.Mockito.*;


class ExitApplicationTest {

    private final SystemExit systemExit = mock(SystemExit.class);

    @Test
    public void run_MockClass_TerminateExecutionCalled() {
        ExitApplication app = spy(new ExitApplication(systemExit));
        InputStream inputStream = mock(InputStream.class);
        OutputStream outputStream = mock(OutputStream.class);

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