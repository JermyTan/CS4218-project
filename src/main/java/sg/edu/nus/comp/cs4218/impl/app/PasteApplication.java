package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.PasteInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;

import java.io.InputStream;
import java.io.OutputStream;

public class PasteApplication implements PasteInterface {
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {

    }

    @Override
    public String mergeStdin(Boolean isSerial, InputStream stdin) throws Exception {
        return null;
    }

    @Override
    public String mergeFile(Boolean isSerial, String... fileNames) throws Exception {
        return null;
    }

    @Override
    public String mergeFileAndStdin(Boolean isSerial, InputStream stdin, String... fileNames) throws Exception {
        return null;
    }
}
