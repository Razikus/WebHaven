package tech.razikus.headlesshaven;

import haven.Connection;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class WebHavenSession implements Runnable {
    private String username;
    private String password;

    private String host = "game.havenandhearth.com";
    private int mainPort = 1870;
    private int authPort = 1871;

    private Connection connection;
    private SimpleAuthResponse authResponse;
    private PlayerHandler handler;

    private boolean shouldClose = false;

    private CopyOnWriteArrayList<ChatCallback> initialChatCallbacks;
    private CopyOnWriteArrayList<ObjectChangeCallback> initialObjectChangeCallbacks;
    private CopyOnWriteArrayList<PseudoWidgetErrorCallback> errorCallbacks;
    private CopyOnWriteArrayList<PseudoWidgetCallback> widgetCallbacks;

    public WebHavenSession(String username, String password) {
        this.username = username;
        this.password = password;
        this.shouldClose = false;
        this.initialChatCallbacks = new CopyOnWriteArrayList<>();
        this.initialObjectChangeCallbacks = new CopyOnWriteArrayList<>();
        this.errorCallbacks = new CopyOnWriteArrayList<>();
        this.widgetCallbacks = new CopyOnWriteArrayList<>();
    }

    public WebHavenSession(String username, String password, CopyOnWriteArrayList<ChatCallback> initialChatCallbacks, CopyOnWriteArrayList<ObjectChangeCallback> initialObjectChangeCallbacks, CopyOnWriteArrayList<PseudoWidgetErrorCallback> errorCallbacks, CopyOnWriteArrayList<PseudoWidgetCallback> widgetCallbacks) {
        this.username = username;
        this.password = password;
        this.shouldClose = false;
        this.initialChatCallbacks = initialChatCallbacks;
        this.initialObjectChangeCallbacks = initialObjectChangeCallbacks;
        this.errorCallbacks = errorCallbacks;
        this.widgetCallbacks = widgetCallbacks;
    }

    public WebHavenSession(String username, String password, CopyOnWriteArrayList<ChatCallback> initialChatCallbacks, CopyOnWriteArrayList<ObjectChangeCallback> initialObjectChangeCallbacks) {
        this.username = username;
        this.password = password;
        this.shouldClose = false;
        this.initialChatCallbacks = initialChatCallbacks;
        this.initialObjectChangeCallbacks = initialObjectChangeCallbacks;
        this.errorCallbacks = new CopyOnWriteArrayList<>();
        this.widgetCallbacks = new CopyOnWriteArrayList<>();
    }

    public WebHavenSession(String username, String password, CopyOnWriteArrayList<ChatCallback> initialChatCallbacks) {
        this.username = username;
        this.password = password;
        this.shouldClose = false;
        this.initialChatCallbacks = initialChatCallbacks;
        this.initialObjectChangeCallbacks = new CopyOnWriteArrayList<>();
        this.errorCallbacks = new CopyOnWriteArrayList<>();
        this.widgetCallbacks = new CopyOnWriteArrayList<>();
    }





    public PseudoWidgetManager getWidgetManager() {
        if(handler == null) {
            return null;
        }
        return handler.getWidgetManager();
    }

    public WebHavenState getLastState() {
        if (handler == null) {
            return null;
        }
        return handler.getLastState();
    }

    public boolean addChatCallback(ChatCallback cb) {
        if(handler == null) {
            initialChatCallbacks.add(cb);
            return false;
        }

        handler.addChatCallback(cb);
        return true;

    }

    public boolean addObjectChangeCallback(ObjectChangeCallback cb) {
        if(handler == null) {
            initialObjectChangeCallbacks.add(cb);
            return false;
        }

        handler.addObjectChangeCallback(cb);
        return true;
    }

    public boolean addErrorCallback(PseudoWidgetErrorCallback cb) {
        if(handler == null) {
            errorCallbacks.add(cb);
            return false;
        }

        handler.addErrorCallback(cb);
        return true;
    }

    public PlayerHandler getHandler() {
        return handler;
    }

    public SimpleAuthResponse authenticate() throws InterruptedException {
        SimpleAuthClient client = new SimpleAuthClient(this.host, this.authPort);
        SimpleAuthResponse resp = null;
        resp = client.getCookie(username, password);
        this.authResponse = resp;
        return resp;
    }

    public boolean connectionCreated() {
        return this.connection != null;
    }

    public boolean isShouldClose() {
        return shouldClose;
    }



    public void setShouldClose(boolean shouldClose) {
        this.shouldClose = shouldClose;
    }

    public boolean isAlive() {
        return this.connection.alive() && !this.handler.isConnClosed();
    }

    @Override
    public void run() {
        System.out.println("CONNECTING OR RECONNECTING INTO HAVEN....");
        try {
            this.connection = new Connection(new InetSocketAddress(host, mainPort), username);
            this.handler = new PlayerHandler(connection);
            if(this.initialChatCallbacks != null && !this.initialChatCallbacks.isEmpty()) {
                for (ChatCallback cb: this.initialChatCallbacks) {
                    this.addChatCallback(cb);
                }
            }
            if (this.initialObjectChangeCallbacks != null && !this.initialObjectChangeCallbacks.isEmpty()) {
                for (ObjectChangeCallback cb: this.initialObjectChangeCallbacks) {
                    this.addObjectChangeCallback(cb);
                }
            }
            if (this.errorCallbacks != null && !this.errorCallbacks.isEmpty()) {
                for (PseudoWidgetErrorCallback cb: this.errorCallbacks) {
                    this.addErrorCallback(cb);
                }
            }
            if (this.widgetCallbacks != null && !this.widgetCallbacks.isEmpty()) {
                for (PseudoWidgetCallback cb: this.widgetCallbacks) {
                    this.addWidgetCallback(cb);
                }
            }
            connection.add(this.handler);
            connection.connect(this.authResponse.getCookie());
            new Thread(this.handler).start();
            while (connection.alive() && !isShouldClose()) {
                Thread.sleep(300);
            }
            connection.close();
        } catch (InterruptedException e) {
            shouldClose = true;
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            shouldClose = true;
        }
    }

    public void addWidgetCallback(PseudoWidgetCallback cb) {
        if(handler == null) {
            widgetCallbacks.add(cb);
            return;
        }
        handler.addWidgetCallback(cb);
    }


}
