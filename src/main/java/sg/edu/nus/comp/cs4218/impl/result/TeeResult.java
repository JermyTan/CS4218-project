package sg.edu.nus.comp.cs4218.impl.result;

public class TeeResult extends Result{

    public TeeResult() {
        super(false);
    }

    public TeeResult(String errorMessage) {
        super(true, errorMessage);
    }
}
