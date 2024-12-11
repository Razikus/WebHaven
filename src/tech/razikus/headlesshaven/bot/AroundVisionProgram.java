package tech.razikus.headlesshaven.bot;

import com.google.gson.JsonObject;
import haven.Coord;
import haven.Coord2d;
import haven.Resource;
import tech.razikus.headlesshaven.*;
import tech.razikus.headlesshaven.bot.automation.AutoLoginCharCallback;
import tech.razikus.headlesshaven.bot.automation.DiscordWebhook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

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

            ErrorSenderCallback errorSenderCallback = new ErrorSenderCallback(manager, this.getProgname());
            CopyOnWriteArrayList<PseudoWidgetErrorCallback> errorCallbacks = new CopyOnWriteArrayList<>();
            errorCallbacks.add(errorSenderCallback);

            ObjectChangeCallback callback = new ObjectSenderCallback(manager, this.getProgname());
            CopyOnWriteArrayList<ObjectChangeCallback> callbacks = new CopyOnWriteArrayList<>();
            callbacks.add(callback);
            WebHavenSession session = new WebHavenSession(username, password, new CopyOnWriteArrayList<>(), callbacks, errorCallbacks, new CopyOnWriteArrayList<>());
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

            try {
                Thread.sleep(10000);
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
            session.getWidgetManager().getVisibleFlowerMenu().ifPresentOrElse(fm -> {
                System.out.println(fm);
                this.getManager().brodcastFromProgram(this.getProgname(), new CommandTypeWrapper("flowermenu", fm));
            }, () -> {
                this.getManager().brodcastFromProgram(this.getProgname(), new CommandTypeWrapper("flowermenu", null));
            });
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
        System.out.println("COMMAND RECEIVED: " + command);
        String cmdType = command.get("cmdType").getAsString();
        System.out.println("COMMAND RECEIVED: " + cmdType);
        switch (cmdType) {
            case "click":
                float rawX = command.get("x").getAsFloat();
                float rawY = command.get("y").getAsFloat();
                int button = command.get("button").getAsInt();
                int modifiers = command.get("modifiers").getAsInt();

                Coord2d gameCoord = new Coord2d(rawX, rawY);
                Coord clickCoord = gameCoord.floor(posres);

                session.getWidgetManager().getMapView().ifPresent(mv -> mv.mapClick(new Coord((int)rawX, (int)rawY), clickCoord, button, modifiers));
                break;
            case "gobclick":
                float rawX1 = command.get("x").getAsFloat();
                float rawY1 = command.get("y").getAsFloat();
                int button1 = command.get("button").getAsInt();
                int modifiers1 = command.get("modifiers").getAsInt();
                Long gobId = command.get("gobId").getAsLong();
                int meshid = command.get("meshId").getAsInt();

                PseudoObject gob = session.getHandler().getObjectManager().getPseudoObjectHashMap().get(gobId);
                Coord2d gobCoord = gob.getCoordinate();


                Coord gobCoordPosRes = gobCoord.floor(posres);

                Coord pc = new Coord((int)rawX1, (int)rawY1);
                System.out.println("GOB CLICKED: " + pc + gobCoord + " " + gobCoordPosRes + " " + gobId + " " + meshid);
                session.getWidgetManager().getMapView().ifPresent(mv -> mv.gobClick(
                        pc, gobCoordPosRes, button1, modifiers1, false, gobId, gobCoordPosRes, 0, meshid
                ));
                break;
            case "requestfullobj":
                HashMap<Long, PseudoObject> map = session.getHandler().getObjectManager().getPseudoObjectHashMapTHSafe();
                this.getManager().brodcastFromProgram(this.getProgname(), new CommandTypeWrapper("fullobj", map));
                break;
            case "requestresource":
                int id = command.get("idOf").getAsInt();
                Resource res = session.getHandler().getResourceManager().getRealResource(id);
                if(res != null) {
                    this.getManager().brodcastFromProgram(this.getProgname(), new CommandTypeWrapper("resource", new IdResource(id, res)));
                }
                break;
            case "flowermenu":
                int option = command.get("option").getAsInt();
                session.getWidgetManager().getVisibleFlowerMenu().ifPresentOrElse(fm -> {
                    if(option == -1) {
                        fm.close();
                    } else {
                        fm.click(option);
                    }
                }, () -> {
                    System.out.println("NO FLOWER MENU");
                });
                break;

            default:
                System.out.println("NOT SUPPORTED YET: " + cmdType);
                break;
        }
    }
}

class ObjectSenderCallback extends ObjectChangeCallback {

    private WebHavenSessionManager manager;
    private String progName;

    public ObjectSenderCallback(WebHavenSessionManager manager, String progName) {
        this.manager = manager;
        this.progName = progName;
    }

    @Override
    public void objectRemoved(long id) {
        manager.brodcastFromProgram(this.progName, new CommandTypeWrapper("objectremoved", id));
    }

    @Override
    public void objectAdded(PseudoObject obj) {
        manager.brodcastFromProgram(this.progName, new CommandTypeWrapper("objectadded", obj));
    }

    @Override
    public void objectChanged(PseudoObject obj) {
        manager.brodcastFromProgram(this.progName, new CommandTypeWrapper("objectchanged", obj));
    }
}

class ErrorSenderCallback extends PseudoWidgetErrorCallback {

    private WebHavenSessionManager manager;
    private String progName;

    public ErrorSenderCallback(WebHavenSessionManager manager, String progName) {
        this.manager = manager;
        this.progName = progName;
    }

    @Override
    public void onError(String message) {
        manager.brodcastFromProgram(this.progName, new CommandTypeWrapper("error", message));
    }
}

class CommandTypeWrapper {
    private String cmdType;
    private Object data;

    public CommandTypeWrapper(String cmdType, Object data) {
        this.cmdType = cmdType;
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandTypeWrapper that = (CommandTypeWrapper) o;
        return Objects.equals(cmdType, that.cmdType) && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cmdType, data);
    }

    @Override
    public String toString() {
        return "CommandTypeWrapper{" +
                "cmdType='" + cmdType + '\'' +
                ", data=" + data +
                '}';
    }

    public String getCmdType() {
        return cmdType;
    }

    public Object getData() {
        return data;
    }
}