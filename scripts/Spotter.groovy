import tech.razikus.headlesshaven.bot.AbstractProgram
import tech.razikus.headlesshaven.WebHavenSessionManager
import tech.razikus.headlesshaven.bot.Credential

class MyScript extends AbstractProgram {
    MyScript(String progname, WebHavenSessionManager manager, Credential credential, HashMap<String, String> runningArgs) {
        super(progname, manager, credential, runningArgs)
    }

    @Override
    void sessionHandler() {

    }

    @Override
    HashSet<String> getRunningSessions() {
        return new HashSet<String>()
    }

    @Override
    void run() {
        println "TEST"
    }
}