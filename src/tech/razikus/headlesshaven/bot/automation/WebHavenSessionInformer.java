package tech.razikus.headlesshaven.bot.automation;

import tech.razikus.headlesshaven.WebHavenSessionManager;
import tech.razikus.headlesshaven.bot.ProgramInformation;

import java.util.Set;

public class WebHavenSessionInformer implements Runnable {
    private WebHavenSessionManager manager;
    private boolean isRunning = true;

    public WebHavenSessionInformer(WebHavenSessionManager manager) {
        this.manager = manager;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                Set<String> sessions = manager.getSessions().keySet();
                manager.broadcastAvailableSessions(sessions);
                Set<ProgramInformation> programs = manager.getProgramInformations();
                manager.broadcastAvailablePrograms(programs);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                isRunning = false;
            }
        }

    }
}
