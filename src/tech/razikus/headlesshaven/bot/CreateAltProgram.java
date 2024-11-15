package tech.razikus.headlesshaven.bot;

import haven.Coord;
import haven.Coord2d;
import tech.razikus.headlesshaven.*;
import tech.razikus.headlesshaven.bot.automation.BrodcastingChatCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static haven.MCache.tilesz;
import static haven.OCache.posres;

public class CreateAltProgram extends AbstractProgram{


    public static HashMap<String, String> declaredArgs = new HashMap<>(Map.of("BEACON_PASS", "Beacon password to set - bot will wait for it"));

    public CreateAltProgram(String progname, WebHavenSessionManager manager, Credential credential, HashMap<String, String> runningArgs) {
        super(progname, manager, credential, runningArgs);
    }


    private WebHavenSession session;
    private String sessName;


    private MapRenderer mapRenderer;

    public void sendMessageProg(String what) {
        this.getManager().brodcastFromProgram(this.getProgname(), new CommandTypeWrapper(
                "message",
                what
        ));
    }

    public void sendStateProg(String what) {
        this.getManager().brodcastFromProgram(this.getProgname(), new CommandTypeWrapper(
                "state",
                what
        ));
    }

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

            WebHavenSession session = new WebHavenSession(username, password);
            try {
                session.authenticate();
            } catch (InterruptedException e) {
                setShouldClose(true);
                return;
            }
            session.addWidgetCallback(new CreateCharButtonCallback(session));

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

            try {
                // WAIT 10 SECONDS BEFORE RECONNECT
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                setShouldClose(true);
            }

        }
    }

    private int state = 1;

    @Override
    public void sessionHandler() {
        String botname = this.getCredential().getCharname();
        String beacon_pass = this.getDeclaredArgs().getOrDefault("BEACON_PASS", "");


        sendStateProg("STARTING");
        sendMessageProg("Starting program");
        while (!session.connectionCreated() && !this.isShouldClose()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("WAITING FOR CONNECTION... ");
        }

        Coord2d originalCoord = null;

        double initialXAfterRiver = -9749.7490234375;
        double initialYAfterRiver = -10904.265625;

        double xWizard = -9749.7490234375  - 50*5;
        double yWizard = -10904.265625 - 100*5;;

        boolean isMoving = false;
        int isMovingThreshold = 0;
        int tick = 1;


        Coord2d oldSub = new Coord2d(0, 0);
        final PseudoWidget[] charterWidget = {null}; // effective final hack
        while (session.isAlive() && !this.isShouldClose()) {
            try {
                if(originalCoord == null && session.getHandler().getObjectManager().getPlayer() != null) {
                    originalCoord = session.getHandler().getObjectManager().getPlayer().getCoordinate();
                }
                if(session.getWidgetManager().getMapView().isPresent()) {
                    MapViewPseudoWidget mapView = session.getWidgetManager().getMapView().get();
                    if(state == 1) {
                        sendStateProg("CROSSING RIVER");
                        Coord2d toClick = new Coord2d(initialXAfterRiver, initialYAfterRiver);
                        Coord mapCoord = toClick.floor(posres);
                        Coord clickPx = new Coord((int)toClick.x, (int)toClick.y);
                        mapView.mapClick(clickPx, mapCoord, 1, 0);
                        isMoving = true;
                        state = 2;
                    } else if (state == 2 && !isMoving) {
                        sendStateProg("GOING TO WIZARD");
                        Coord2d toClick = new Coord2d(xWizard, yWizard);
                        Coord mapCoord = toClick.floor(posres);
                        Coord clickPx = new Coord((int)toClick.x, (int)toClick.y);
                        mapView.mapClick(clickPx, mapCoord, 1, 0);
                        isMoving = true;
                        state = 3;
                    } else if (state == 3 && !isMoving) {
                        sendStateProg("RIGHTCLICKING WIZARD");
                        PseudoObject player = session.getHandler().getObjectManager().getPlayer();
                        HashMap<Long, PseudoObject> objs = session.getHandler().getObjectManager().getPseudoObjectHashMapTHSafe();
                        PseudoObject wizard = null;
                        for (PseudoObject obj : objs.values()) {
                            if(obj.getId() != player.getId() && obj.getCompositeModifications() != null) {

                                for (CompositeModification compo: obj.getCompositeModifications()) {
                                    for (ResourceInformationLazyProxy prox: compo.getResources()) {
                                        if (prox.getResource() != null & prox.getResource().getInformation() != null) {
                                            if (prox.getResource().getInformation().getName().contains("wizard")) {
                                                wizard = obj;
                                                break;
                                            }
                                        }
                                    }
                                }

                            }
                        }
                        if(wizard != null ) {
                            System.out.println(wizard);
                            mapView.gobClickR(wizard);
                            session.getWidgetManager().addWidgetCallback(new PseudoWidgetCallback() {
                                @Override
                                public void onWidgetCreated(PseudoWidget widget) {
                                    if(widget.getType().contains("ui/chnm")) {
                                        widget.WidgetMsg("nm", botname);
                                        session.getWidgetManager().removeWidgetCallback(this);
                                        state = 5;
                                    }
                                }

                                @Override
                                public void onWidgetDestroyed(int id) {
                                }
                            });
                            state = 4;
                        }
                    } else if(state == 4) {
                        sendStateProg("CHANGING NAME");
                        System.out.println("Waiting for widget to send name change");
                    } else if (state == 5 ){
                        if(beacon_pass.isEmpty()) {
                            state = 7;
                        } else {
                            sendStateProg("RIGHTCLICKING CHARTER POLE");
                            HashMap<Long, PseudoObject> objs = session.getHandler().getObjectManager().getPseudoObjectHashMapTHSafe();
                            PseudoObject charterPole = null;
                            for (PseudoObject obj : objs.values()) {

                                for (ResourceInformationLazyProxy prox: obj.getResourceInformationLazyProxies()) {
                                    if (prox.getResource() != null & prox.getResource().getInformation() != null) {
                                        if (prox.getResource().getInformation().getName().contains("gfx/terobjs/charterpole")) {
                                            charterPole = obj;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (charterPole != null) {
                                System.out.println(charterPole);
                                mapView.gobClickR(charterPole);
                                state = 6;
                                session.getWidgetManager().addWidgetCallback(new PseudoWidgetCallback() {
                                    private int widgetToWatchToRemove = -1;

                                    @Override
                                    public void onWidgetCreated(PseudoWidget widget) {
                                        if(widget.getType().equals("text")) {
                                            widgetToWatchToRemove = widget.getId();
                                            widget.WidgetMsg("activate", beacon_pass);
                                            charterWidget[0] = widget;
                                        }
                                    }

                                    @Override
                                    public void onWidgetDestroyed(int id) {
                                        if(widgetToWatchToRemove == id) {
                                            charterWidget[0] = null;
                                            state = 7;
                                            session.getWidgetManager().removeWidgetCallback(this);
                                        }

                                    }
                                });

                            }
                        }

                    } else if (state == 6) {
                        sendStateProg("WAITING FOR BEACON INPUT");
                        System.out.println("WAITING FOR CHARTER POLE");
                    } else if (state == 7 ) {
                        System.out.println("NAME CHANGED");
                        sendStateProg("BURNING IN FIRE");
                        HashMap<Long, PseudoObject> objs = session.getHandler().getObjectManager().getPseudoObjectHashMapTHSafe();
                        PseudoObject fire = null;
                        for (PseudoObject obj : objs.values()) {

                            for (ResourceInformationLazyProxy prox: obj.getResourceInformationLazyProxies()) {
                                if (prox.getResource() != null & prox.getResource().getInformation() != null) {
                                    if (prox.getResource().getInformation().getName().contains("gfx/terobjs/pow")) {
                                        fire = obj;
                                        break;
                                    }
                                }
                            }
                        }
                        if (fire != null) {
                            mapView.gobClickR(fire);
                            session.getWidgetManager().addWidgetCallback(new PseudoWidgetCallback() {
                                @Override
                                public void onWidgetCreated(PseudoWidget widget) {
                                    if(widget.getType().contains("ui/province")) {
                                        state = 9;
                                        session.getWidgetManager().removeWidgetCallback(this);
                                    }
                                }

                                @Override
                                public void onWidgetDestroyed(int id) {

                                }
                            });
                            state = 8;
                        }
                    } else if (state == 8 ) {
                        sendStateProg("WAITING FOR BURNING");
                        System.out.println("WAITING FOR BURNING");
                    } else if(state == 9) {
                        System.out.println("======================================");
                        System.out.println("======================================");
                        System.out.println("THANK YOU FOR CALLING OUR SERVICE");
                        System.out.println("HAVE A NICE DAY");
                        System.out.println("======================================");
                        System.out.println("======================================");
                        sendStateProg("FINISHED");
                        sendMessageProg("Finished creating character. THANK YOU FOR CALLING OUR SERVICE, HAVE A NICE DAY");
                        setShouldClose(true);
                    } else if(isMoving) {
                        System.out.println("Currently moving to next location.");
                        sendMessageProg("Currently moving to next location.");
                    }
                }
                if(tick % 10 == 0 && charterWidget[0] != null) {
                    sendMessageProg("Waiting for beacon input... " + beacon_pass);
                    charterWidget[0].WidgetMsg("activate", beacon_pass);
                }
                Thread.sleep(300);
                tick++;
                PseudoObject player = session.getHandler().getObjectManager().getPlayer();

                if(player != null && originalCoord != null && player.getCoordinate() != null) {
                    Coord2d sub = player.getCoordinate().sub(originalCoord);
                    if(oldSub.x == sub.x && oldSub.y == sub.y) {
                        if (isMovingThreshold > 5) {
                            isMoving = false;
                            System.out.println("NOT MOVING");
                            sendMessageProg("Detected not moving");
                            isMovingThreshold = 0;
                        } else {
                            isMovingThreshold++;
                        }
                    } else {
                        sendMessageProg("Detected moving");
                        isMoving = true;
                    }

                    oldSub = sub;

                }
            } catch (InterruptedException e) {
                this.setShouldClose(true);
            }
//            catch (IOException e) {
//                throw new RuntimeException(e);
//            }
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

class CreateCharButtonCallback extends PseudoWidgetCallback {

    private WebHavenSession session;


    public CreateCharButtonCallback(WebHavenSession session) {
        this.session = session;
    }

    @Override
    public void onWidgetCreated(PseudoWidget widget) {
        if(widget.getId() == 6) {
            widget.WidgetMsg("activate");
            session.getWidgetManager().removeWidgetCallback(this);
        }
    }

    @Override
    public void onWidgetDestroyed(int id) {
        System.out.println("Widget destroyed: " + id);
    }
}