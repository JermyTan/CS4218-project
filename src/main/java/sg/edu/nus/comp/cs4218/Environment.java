package sg.edu.nus.comp.cs4218;

@SuppressWarnings("PMD.ClassNamingConventions")
public final class Environment {

    /**
     * Java VM does not support changing the current working directory.
     * For this reason, we use Environment.currentDirectory instead.
     */
    public static volatile String currentDirectory = System.getProperty("user.dir");


    private Environment() {
    }

}
