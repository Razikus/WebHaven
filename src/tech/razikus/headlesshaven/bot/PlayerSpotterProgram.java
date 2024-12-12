package tech.razikus.headlesshaven.bot;

import tech.razikus.headlesshaven.*;
import tech.razikus.headlesshaven.bot.automation.AutoLoginCharCallback;
import tech.razikus.headlesshaven.bot.automation.DiscordWebhook;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class PlayerSpotterProgram extends AbstractProgram{


    public PlayerSpotterProgram(String progname, WebHavenSessionManager manager, Credential credential, HashMap<String, String> runningArgs) {
        super(progname, manager, credential, runningArgs);
    }


    public static HashMap<String, String> declaredArgs = new HashMap<>(Map.of("discord_key", "Discord api key to push notifications to", "notify_start", "Notify about start"));


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

            WebHavenSession session = new WebHavenSession(username, password, new CopyOnWriteArrayList<>());
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

    public void sendMessageWithStandardException(String message) {
        DiscordWebhook.Embed embed = new DiscordWebhook.Embed()
                .setTitle(message)
                .setColor(0x00ff00);
        try {
            this.webhook.sendEmbed(embed);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Override
    public void sessionHandler() {
        String toFind = "gfx/borka/body";
        boolean shouldNotifyStart = this.getRunningArgs().get("notify_start") != null && this.getRunningArgs().get("notify_start").equals("1");

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


        int initialSecondsToLoadResources = 5;
        System.out.println("WAITING FOR RESOURCES TO LOAD... ");
        while (counter < initialSecondsToLoadResources && !this.isShouldClose()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                this.setShouldClose(true);
            }
            counter++;
        }

        if (shouldNotifyStart) {
            this.sendMessageWithStandardException("STARTING PLAYER SPOTTER SESSION: " + sessName);
        }

        while ((session.isAlive() && !this.isShouldClose())) {
            this.getManager().brodcastFromProgram(this.getProgname(), new CommandTypeWrapper(
                    "state",
                    "SEARCHING"
            ));
            found = false;
            foundCount = 0;
            Set<Long> alreadyProcessed = new HashSet<>();

            StringBuilder EQ = new StringBuilder();
            for (PseudoObject obj: session.getHandler().getObjectManager().getPseudoObjectHashMap().values()) {
                for (ResourceInformationLazyProxy proxy: obj.getResourceInformationLazyProxies()) {
                    if (proxy.getResource().getInformation().getName().equals(toFind) && obj.getId() != session.getWidgetManager().getMyGOBId()) {
                        if(!obj.isVillageBuddy()) {
                            if(alreadyProcessed.contains(obj.getId())) {
                                continue;
                            }
                            found = true;
                            foundCount++;
                            alreadyProcessed.add(obj.getId());
                            if(!obj.getCompositeModifications().isEmpty()) {
                                for (CompositeModification modification: obj.getCompositeModifications()) {
                                    if(modification.getResources() != null && !modification.getResources().isEmpty()) {
                                        for (ResourceInformationLazyProxy res : modification.getResources()) {
                                            if(res.getResource().getInformation() != null) {
                                                EQ.append(" | ").append(res.getResource().getInformation().getName().replace("gfx/borka/", ""));
                                            }
                                        }

                                    }

                                }
                            }
                            if(obj.getBuddyState() != null && obj.getBuddyState().getBuddyState() != null) {
                                EQ.append(" | KNOWN AS: ").append(obj.getBuddyState().getBuddyState().getName());
                            }
                        }
                        EQ.append("\n\n");
                    }
                }

            }
            if(infoSend && foundCountOld != foundCount) {
                String mess = "CHANGE " + toFind + " COUNT: " + foundCount + " | SESSION: " + sessName;
                System.out.println(mess);

                DiscordWebhook.Embed embed = new DiscordWebhook.Embed()
                        .setTitle(mess)
                        .setDescription(EQ.toString())
                        .setColor(0x00ff00);

                this.getManager().brodcastFromProgram(this.getProgname(), new CommandTypeWrapper(
                        "message",
                        mess
                ));
                try {
                    this.webhook.sendEmbed(embed);
                } catch (Exception e) {
                    System.out.println(e);
                    continue;
                }
                foundCountOld = foundCount;
            }
            if(found && !infoSend) {
                foundCountOld = foundCount;
                String mess = "FOUND " + toFind + " COUNT: " + foundCount + " | SESSION: " + sessName;
                System.out.println(mess);

                DiscordWebhook.Embed embed = new DiscordWebhook.Embed()
                        .setTitle(mess)
                        .setDescription(EQ.toString())
                        .setColor(0x00ff00);

                try {
                    this.webhook.sendEmbed(embed);
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
