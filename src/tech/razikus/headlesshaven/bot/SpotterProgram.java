package tech.razikus.headlesshaven.bot;

import tech.razikus.headlesshaven.PseudoObject;
import tech.razikus.headlesshaven.ResourceInformationLazyProxy;
import tech.razikus.headlesshaven.WebHavenSession;
import tech.razikus.headlesshaven.WebHavenSessionManager;
import tech.razikus.headlesshaven.bot.automation.AutoLoginCharCallback;
import tech.razikus.headlesshaven.bot.automation.DiscordWebhook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class SpotterProgram extends AbstractProgram{


    public SpotterProgram(String progname, WebHavenSessionManager manager, Credential credential, HashMap<String, String> runningArgs) {
        super(progname, manager, credential, runningArgs);
    }

    public static HashMap<String, String> declaredArgs = new HashMap<>(Map.of("resource_to_spot", "Resource name to spot", "discord_key", "Discord api key to push notifications to"));


    private WebHavenSession session;
    private String sessName;

    private DiscordWebhook webhook;

    @Override
    public void run() {
        this.webhook = new DiscordWebhook(getRunningArgs().get("discord_key"));
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

            WebHavenSession session = new WebHavenSession(username, password, altname, new CopyOnWriteArrayList<>());
            try {
                session.authenticate();
            } catch (InterruptedException e) {
                setShouldClose(true);
                return;
            }

            session.addWidgetCallback(new AutoLoginCharCallback(altname, session));

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

            this.getManager().brodcastFromProgram(this.getProgname(), new CommandTypeWrapper(
                    "state",
                    "SLEEPING"
            ));
            this.getManager().brodcastFromProgram(this.getProgname(), new CommandTypeWrapper(
                    "message",
                    "Sleeping for 60 seconds before finding again"
            ));


            try {
                Thread.sleep(1000 * 60);
            } catch (InterruptedException e) {
                setShouldClose(true);
            }

        }
    }

    @Override
    public void sessionHandler() {
        String toFind = this.getRunningArgs().get("resource_to_spot");

        while (!session.connectionCreated() && !this.isShouldClose()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("WAITING FOR CONNECTION... ");
        }

        boolean found = false;
        int foundCount = 0;
        boolean infoSend = false;

        int counter = 0;
        int foundCountOld = 0;

        while ((session.isAlive() && !this.isShouldClose())) {
            this.getManager().brodcastFromProgram(this.getProgname(), new CommandTypeWrapper(
                    "state",
                    "SEARCHING"
            ));
            System.out.println("SEARCHING FOR " + toFind + " IN SESSION: " + sessName);
            found = false;
            foundCount = 0;

            for (PseudoObject obj: session.getHandler().getObjectManager().getPseudoObjectHashMap().values()) {
                for (ResourceInformationLazyProxy proxy: obj.getResourceInformationLazyProxies()) {
                    if (proxy.getResource().getInformation().getName().equals(toFind) && obj.getId() != session.getWidgetManager().getMyGOBId()) {
                        found = true;
                        foundCount++;
                        counter = 0;
                    }
                }

            }
            if(infoSend && foundCountOld != foundCount) {
                String mess = "FOUND " + toFind + " COUNT: " + foundCount + " | SESSION: " + sessName;
                this.getManager().brodcastFromProgram(this.getProgname(), new CommandTypeWrapper(
                        "message",
                        mess
                ));
                try {
                    this.webhook.sendMessage(mess);
                } catch (Exception e) {
                    continue;
                }
                foundCountOld = foundCount;
            }
            if(found && !infoSend) {
                foundCountOld = foundCount;
                String mess = "FOUND " + toFind + " COUNT: " + foundCount + " | SESSION: " + sessName;
                try {
                    this.webhook.sendMessage(mess);
                    infoSend = true;
                } catch (Exception e) {
                    continue;
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                this.setShouldClose(true);
            }

            counter++;

            if(!found && counter > 60) {

                this.getManager().brodcastFromProgram(this.getProgname(), new CommandTypeWrapper(
                        "state",
                        "SESSION DISCONNECTED"
                ));
                this.session.setShouldClose(true);
            }
        }
        if(infoSend) {
            String mess = "DISCONNECTING  SESSION: " + sessName;
            this.getManager().brodcastFromProgram(this.getProgname(), new CommandTypeWrapper(
                    "message",
                    mess
            ));
            this.getManager().brodcastFromProgram(this.getProgname(), new CommandTypeWrapper(
                    "state",
                    "DISCONNECTING"
            ));
            try {
                webhook.sendMessage(mess);
            } catch (Exception e) {
                System.out.println(e);
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
