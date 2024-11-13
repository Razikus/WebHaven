package tech.razikus.headlesshaven.bot;

import com.google.gson.JsonObject;
import haven.Coord;
import haven.Coord2d;
import tech.razikus.headlesshaven.PseudoObject;
import tech.razikus.headlesshaven.PseudoWidget;
import tech.razikus.headlesshaven.WebHavenSession;
import tech.razikus.headlesshaven.WebHavenSessionManager;
import tech.razikus.headlesshaven.bot.automation.DiscordWebhook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static haven.OCache.posres;

public class AroundVisionProgram extends AbstractProgram{


    public AroundVisionProgram(String progname, WebHavenSessionManager manager, Credential credential, HashMap<String, String> runningArgs) {
        super(progname, manager, credential, runningArgs);
    }

    public static HashMap<String, String> declaredArgs = new HashMap<>();


    private WebHavenSession session;
    private String sessName;

    private DiscordWebhook webhook;

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

            WebHavenSession session = new WebHavenSession(username, password, altname, new ArrayList<>());
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

            try {
                Thread.sleep(1000 * 60);
            } catch (InterruptedException e) {
                setShouldClose(true);
            }

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



        while ((session.isAlive() && !this.isShouldClose())) {
            HashMap<Long, PseudoObject> map = session.getHandler().getObjectManager().getPseudoObjectHashMapTHSafe();
            this.getManager().brodcastFromProgram(this.getProgname(), map);
//            map.forEach((k, v) -> {
//                System.out.println(v);
//            });
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


    @Override
    public void handleInput(JsonObject command) {
        String cmdType = command.get("cmdType").getAsString();
        switch (cmdType) {
            case "click":
                float rawX = command.get("x").getAsFloat();
                float rawY = command.get("y").getAsFloat();
                int button = command.get("button").getAsInt();
                int modifiers = command.get("modifiers").getAsInt();

                Coord2d gameCoord = new Coord2d(rawX, rawY);
                Coord clickCoord = gameCoord.floor(posres);
                Object[] args = new Object[] {
                        new Coord((int)rawX, (int)rawY),  // pixel coordinates
                        clickCoord,                        // game world coordinates
                        button,
                        modifiers
                };
                ArrayList<PseudoWidget> wgs = session.getWidgetManager().getWidgetsByType("mapview");
                if(!wgs.isEmpty()) {
                    PseudoWidget wg = wgs.getFirst();
                    wg.WidgetMsg("click", args);
                }
                break;
            case "gobclick":
                float rawX1 = command.get("x").getAsFloat();
                float rawY1 = command.get("y").getAsFloat();
                int button1 = command.get("button").getAsInt();
                int modifiers1 = command.get("modifiers").getAsInt();
                Long gobId = command.get("gobId").getAsLong();

                PseudoObject gob = session.getHandler().getObjectManager().getPseudoObjectHashMap().get(gobId);
                System.out.println("Raw click coordinates: " + rawX1 + ", " + rawY1);
                System.out.println("Gob position: " + gob.getCoordinate());
                System.out.println("Gob angle: " + gob.getAngle());
                Coord2d gobCoord = gob.getCoordinate();
                Coord2d relativeClick = new Coord2d(38.0, 0.0);
                System.out.println("Before rotation relative click: " + relativeClick);
                if (gob.getAngle() != 0) {
                    double cos = Math.cos(gob.getAngle());
                    double sin = Math.sin(gob.getAngle());
                    relativeClick = new Coord2d(
                            relativeClick.x * cos - relativeClick.y * sin,
                            relativeClick.x * sin + relativeClick.y * cos
                    );
                    System.out.println("After rotation relative click: " + relativeClick);
                }
                Coord2d worldClickPos = gobCoord.add(relativeClick);  // Only add once!
                System.out.println("World click position: " + worldClickPos);

                Coord gobCoordPosRes = gobCoord.floor(posres);
                Coord clickCoord1 = worldClickPos.floor(posres);

                System.out.println("Floored gob coord: " + gobCoordPosRes);
                System.out.println("Floored click coord: " + clickCoord1);

                Coord pc = new Coord((int)rawX1, (int)rawY1);
                // @todo currently NOT supporting overlays
                System.out.println(pc);
                Object[] args1 = new Object[] {
                        pc,                 // screen coordinate
                        clickCoord1,        // clicked map coordinate
                        button1,            // button pressed
                        modifiers1,         // modifier keys
                        0, // clickargs of GobClick extends Clickabl in Gob.java
                        gobId,              // clicked gob ID
                        gobCoordPosRes,
                        0,
                        -1
                };
//                click: [(733, 407), (-965701, -975892), 3, 0, 0, 1648077781, (-965632, -978944), 0, 16] in
 //               click: [(725, 615), (-970284, -972670), 3, 0, 0, 1868781591, (-969728, -972800), 0, -1] out
                /* ORIGINICAL CLICKARGS:
                public Object[] clickargs(ClickData cd) {
                    Object[] ret = {0, (int)gob.id, gob.rc.floor(OCache.posres), 0, -1};
                    for(Object node : cd.array()) {
                    if(node instanceof Gob.Overlay) {
                        ret[0] = 1;
                        ret[3] = ((Gob.Overlay)node).id;
                    }
                    if(node instanceof FastMesh.ResourceMesh)
                        ret[4] = ((FastMesh.ResourceMesh)node).id;
                    }
                    return(ret);
                }
                 */
                ArrayList<PseudoWidget> wgs1 = session.getWidgetManager().getWidgetsByType("mapview");
                if(!wgs1.isEmpty()) {
                    PseudoWidget wg = wgs1.getFirst();
                    wg.WidgetMsg("click", args1);
                }
                break;
            default:
                System.out.println("NOT SUPPORTED YET: " + cmdType);
                break;
        }
    }
}
