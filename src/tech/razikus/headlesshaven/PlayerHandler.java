package tech.razikus.headlesshaven;

import haven.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerHandler implements Connection.Callback, Runnable {

    private Connection connection;
    private PseudoWidgetManager widgetManager;
    private ResourceManager resourceManager;
    private ObjectManager objectManager;
    private AtomicReference<WebHavenState> latestState = new AtomicReference<>();
    private PseudoGlobManager pseudoGlobManager;
    private SimpleMapCache mapCache;

    private CopyOnWriteArrayList<ChatCallback> chatCallbacks = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<ObjectChangeCallback> objectChangeCallbacks = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<PseudoWidgetErrorCallback> errorCallbacks = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<PseudoWidgetCallback> widgetCallbacks = new CopyOnWriteArrayList<>();


//    private SynchronousQueue<WebHavenState> queue = new SynchronousQueue<>();

    private boolean firstMessageArrived = false;
    private boolean connClosed = false;

    private long started;

    public PlayerHandler(Connection connection) {
        this.connection = connection;
        this.resourceManager = new ResourceManager();
        this.mapCache =  new SimpleMapCache(resourceManager, this);
        this.pseudoGlobManager = new PseudoGlobManager(resourceManager);
        this.widgetManager = new PseudoWidgetManager(this.resourceManager, chatCallbacks, errorCallbacks, widgetCallbacks);
        this.objectManager = new ObjectManager(resourceManager, widgetManager, objectChangeCallbacks);

    }

    public SimpleMapCache getMapCache() {
        return mapCache;
    }

    public WebHavenState getLastState() {
        return latestState.get();
    }

    public PseudoWidgetManager getWidgetManager() {
        return widgetManager;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public ObjectManager getObjectManager() {
        return objectManager;
    }

    public PseudoGlobManager getPseudoGlobManager() {
        return pseudoGlobManager;
    }

    public void antiAFK() {
        if(this.connection != null && this.connection.alive()) {
            if(widgetManager.getInstantiatedBuddy() != null) {
                if(widgetManager.getInstantiatedBuddy().getMyPlayerName() != null) {
                    System.out.println("Sending anti afk");
                    widgetManager.getInstantiatedBuddy().WidgetMsg("pname", widgetManager.getInstantiatedBuddy().getMyPlayerName());
                }

            }
        }
    }

    public void sendMessageFromWidget(int id, String what, Object... args) {
        PMessage msg = new PMessage(RMessage.RMSG_WDGMSG);
        msg.addint32(id);
        msg.addstring(what);
        msg.addlist(args);
        connection.queuemsg(msg);
    }

    public void requestMap(Coord gc) {
        PMessage msg = new PMessage(Session.MSG_MAPREQ);
        msg.addcoord(gc);
        connection.send(msg);
    }

    private void handleuimessage(PMessage msg) {
        if (msg == null) {
            return;
        } else if (msg.type == RMessage.RMSG_NEWWDG) {
            int id = msg.int32();
            String type = msg.string();
            int parent = msg.int32();
            Object[] pargs = msg.list();
            Object[] cargs = msg.list();
            PseudoWidget ps = new PseudoWidget(this, id, type, parent, pargs, cargs);
            widgetManager.addNewWidget(ps);
        } else if (msg.type == RMessage.RMSG_WDGMSG) {
            int id = msg.int32();
            String name = msg.string();
            WidgetMessage message = new WidgetMessage(id, name, msg.list());
            widgetManager.dispatchMessage(message);
//            widgetManager.re
        } else if (msg.type == RMessage.RMSG_DSTWDG) {
            int id = msg.int32();
            widgetManager.removeWidgetAndChildrens(id);
//            System.out.println("WDG REMOVED: " + id);
        } else if (msg.type == RMessage.RMSG_ADDWDG) {
            int id = msg.int32();
            int parent = msg.int32();
            Object[] pargs = msg.list();
            PseudoWidget ws = new PseudoWidget(id, parent, pargs);
            widgetManager.widgetSetParent(id, ws);
//            System.out.println("WDG ADDED : " + id + " " + ws);
        } else if (msg.type == RMessage.RMSG_WDGBAR) {
            Collection<Integer> deps = new ArrayList<>();
            while (!msg.eom()) {
                int dep = msg.int32();
                if (dep == -1) {
                    break;
                }
                deps.add(dep);
            }
            Collection<Integer> bars = deps;
            if (!msg.eom()) {
                bars = new ArrayList<>();
                while (!msg.eom()) {
                    int bar = msg.int32();
                    if (bar == -1) {
                        break;
                    }
                    bars.add(bar);
                }
            }
            System.out.println("WDGBAR: " + deps + " " + bars);

        }
    }

    private void handlerel(PMessage msg) {
        if ((msg.type == RMessage.RMSG_NEWWDG) || (msg.type == RMessage.RMSG_WDGMSG)
                || (msg.type == RMessage.RMSG_DSTWDG) || (msg.type == RMessage.RMSG_ADDWDG)
                || (msg.type == RMessage.RMSG_WDGBAR)) {
            handleuimessage(msg);
        } else if (msg.type == RMessage.RMSG_MAPIV) {
            System.out.println("MAP IV");
        } else if (msg.type == RMessage.RMSG_GLOBLOB) {
            System.out.println("GLOBOB: " + msg.type + " " + Arrays.toString(msg.rbuf));
            pseudoGlobManager.handleGlobalObject(msg);
        } else if (msg.type == RMessage.RMSG_RESID) {
            int resid = msg.uint16();
            String resname = msg.string();
            int resver = msg.uint16();
            resourceManager.addResource(new ResourceInformation(resid, resname, resver));
        } else if (msg.type == RMessage.RMSG_SFX) {
            System.out.println("SFX");
        } else if (msg.type == RMessage.RMSG_MUSIC) {
            String resnm = msg.string();
            int resver = msg.uint16();
            System.out.println("MUSIC");
        } else if (msg.type == RMessage.RMSG_SESSKEY) {
            System.out.println("SESKEY");
        } else {
            throw (new RuntimeException("Unknown rmsg type: " + msg.type + " " + msg));
        }
    }



    @Override
    public void closed() {
        System.out.println("CONNECTION IS CLOSED");
        connClosed = true;
//        Connection.Callback.super.closed();
    }

    @Override
    public void handle(PMessage msg) {
        firstMessageArrived = true;
        this.handlerel(msg);

//        Connection.Callback.super.handle(msg);
    }

    @Override
    public void handle(OCache.ObjDelta delta) {
        firstMessageArrived = true;
        objectManager.handleDelta(delta);
//        MessageParser.parseObjDelta(delta);
    }

    @Override
    public void mapdata(Message msg) {
        firstMessageArrived = true;
        mapCache.mapdata(msg);
//        Connection.Callback.super.mapdata(msg);
    }

    public boolean isConnClosed() {
        return connClosed;
    }

    @Override
    public void run() {
        started = System.currentTimeMillis();
        while(!firstMessageArrived && !connClosed) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        int tick = 1;
        int requestTick = 1;
        boolean wasRequested = false;
        while(connection.alive() && !connClosed) {
            try {
                Thread.sleep(1000);
                WebHavenState currentState = getWebHavenState();
                latestState.set(currentState);
                tick++;
                requestTick++;
                if(tick % 10 == 0) {
                    tick = 0;
                    antiAFK();
                }
                if(!wasRequested && this.objectManager.getPlayer() != null && this.objectManager.getPlayer().getCoordinate() != null ) {
                    this.mapCache.reqAreaAround(this.objectManager.getPlayer().getCoordinate(), 2);
                    wasRequested = true;
                }
                if (requestTick % 60 == 0) {
                    requestTick = 0;
                    if(this.objectManager.getPlayer() != null) {
                        this.mapCache.reqAreaAround(this.objectManager.getPlayer().getCoordinate(), 2);
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("SESSION CLOSED AFTER: " + (System.currentTimeMillis()- started)/1000);
    }

    private WebHavenState getWebHavenState() {
        ArrayList<PseudoObject> objlist = new ArrayList<>();
        objectManager.getPseudoObjectHashMap().forEach((id, ps) -> {
            if(ps.isProbablyPlayer()) {
                objlist.add(ps);
            }
        });
        ArrayList<String> chatChannels = widgetManager.getChatChannels();
        AstronomyProcessed ast = null;
        if (pseudoGlobManager != null && pseudoGlobManager.getAst() != null) {
            ast = pseudoGlobManager.getAst().process();
        }
        return new WebHavenState(objlist, chatChannels, ast);

    }

    public void addChatCallback(ChatCallback cb) {
        synchronized (chatCallbacks) {
            chatCallbacks.add(cb);
        }
    }

    public void addObjectChangeCallback(ObjectChangeCallback cb) {
        synchronized (objectChangeCallbacks) {
            objectChangeCallbacks.add(cb);
        }
    }

    public void addErrorCallback(PseudoWidgetErrorCallback cb) {
        synchronized (errorCallbacks) {
            errorCallbacks.add(cb);
        }
    }

    public void addWidgetCallback(PseudoWidgetCallback cb) {
        synchronized (widgetCallbacks) {
            widgetCallbacks.add(cb);
        }
    }
}