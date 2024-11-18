package tech.razikus.headlesshaven.bot;

import com.google.gson.JsonObject;
import tech.razikus.headlesshaven.WebHavenSessionManager;

import java.util.HashMap;
import java.util.HashSet;

public abstract class AbstractProgram implements Runnable {
    private String progname;
    private WebHavenSessionManager manager;
    private Credential credential;
    private boolean shouldClose = false;

    public static HashMap<String, String> declaredArgs = new HashMap<>();
    private HashMap<String, String> runningArgs = new HashMap<>();


    public AbstractProgram(String progname, WebHavenSessionManager manager, Credential credential, HashMap<String, String> runningArgs) {
        this.progname = progname;
        this.manager = manager;
        this.credential = credential;
        this.runningArgs = runningArgs;
    }

    public HashMap<String, String> getRunningArgs() {
        return runningArgs;
    }

    public void setRunningArgs(HashMap<String, String> runningArgs) {
        this.runningArgs = runningArgs;
    }

    public String getProgname() {
        return progname;
    }


    public WebHavenSessionManager getManager() {
        return manager;
    }

    public Credential getCredential() {
        return credential;
    }

    public HashMap<String, String> getDeclaredArgs() {
        return declaredArgs;
    }

    public boolean isShouldClose() {
        return shouldClose;
    }

    public void setShouldClose(boolean shouldClose) {
        this.shouldClose = shouldClose;
    }

    public abstract void sessionHandler();

    public abstract HashSet<String> getRunningSessions();

    public ProgramInformation getProgramInformation() {

        return new ProgramInformation(this.progname, getClass().getName(), getRunningSessions());
    }

    public void handleInput(JsonObject command) {

    }
}
