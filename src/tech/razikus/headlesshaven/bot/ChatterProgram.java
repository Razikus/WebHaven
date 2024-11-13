package tech.razikus.headlesshaven.bot;

import tech.razikus.headlesshaven.ChatCallback;
import tech.razikus.headlesshaven.WebHavenSession;
import tech.razikus.headlesshaven.WebHavenSessionManager;
import tech.razikus.headlesshaven.WebHavenState;
import tech.razikus.headlesshaven.bot.automation.BrodcastingChatCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ChatterProgram extends AbstractProgram{


    public ChatterProgram(String progname, WebHavenSessionManager manager, Credential credential, HashMap<String, String> runningArgs) {
        super(progname, manager, credential, runningArgs);
    }


    private WebHavenSession session;
    private String sessName;

    @Override
    public void run() {
        while (!this.isShouldClose()) {
            WebHavenSessionManager manager = this.getManager();
            String username = this.getCredential().getUsername();
            String password = this.getCredential().getPassword();
            String altname = this.getCredential().getCharname();

            String sessName = username + "-" + altname;
            this.sessName = sessName;
            if(manager.getSessions().containsKey(sessName)) {
                return;
            }

            ChatCallback chatStreamer = new BrodcastingChatCallback(this.getManager(), sessName);
            ArrayList<ChatCallback> callbacks = new ArrayList<>();
            callbacks.add(chatStreamer);
            WebHavenSession session = new WebHavenSession(username, password, altname, callbacks);
            try {
                session.authenticate();
            } catch (InterruptedException e) {
                setShouldClose(true);
                return;
            }

            Thread sessionThread = new Thread(session);
            sessionThread.start();

            this.session = session;
            Thread programThread = new Thread(this::sessionHandler);
            programThread.start();

            this.getManager().getSessions().put(sessName, session);
            try {
                programThread.join();
            } catch (InterruptedException e) {
                this.setShouldClose(true);
            }
            this.sessName = null;
            this.getManager().getSessions().remove(sessName);

        }
    }

    @Override
    public void sessionHandler() {

        while (!session.connectionCreated() && !this.isShouldClose()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("WAITING FOR CONNECTION... ");
        }

        while (session.isAlive() && !this.isShouldClose()) {
            WebHavenState state = null; // Get state from queue
            try {
                while(session.isAlive() && session.getLastState() == null && !this.isShouldClose()) {
                    Thread.sleep(100);
                    System.out.println("WAITING FOR FIRST STATE... ");
                }
                state = session.getLastState();
            } catch (InterruptedException e) {
                this.setShouldClose(true);
            }
            if(state != null && !this.isShouldClose()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    this.setShouldClose(true);
                }

                this.getManager().broadcastState(sessName, getProgramInformation(), state);
            }
        }
    }

    @Override
    public HashSet<String> getRunningSessions() {
        HashSet<String> newSessions = new HashSet<>();
        if(this.sessName != null) {
            newSessions.add(sessName);
        }
        return newSessions;
    }

    @Override
    public void setShouldClose(boolean shouldClose) {
        if(this.session != null) {
            this.session.setShouldClose(true);
        }
        super.setShouldClose(shouldClose);
    }
}
