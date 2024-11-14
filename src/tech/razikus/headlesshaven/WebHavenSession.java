package tech.razikus.headlesshaven;

import haven.Connection;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public class WebHavenSession implements Runnable {
    private String username;
    private String password;
    private String characterName; // @TODO Set to null to trigger new character creation

    private String host = "game.havenandhearth.com";
    private int mainPort = 1870;
    private int authPort = 1871;

    private Connection connection;
    private SimpleAuthResponse authResponse;
    private PlayerHandler handler;

    private boolean shouldClose = false;

    private ArrayList<ChatCallback> initialChatCallbacks;
    private ArrayList<ObjectChangeCallback> initialObjectChangeCallbacks;
    private ArrayList<PseudoWidgetErrorCallback> errorCallbacks = new ArrayList<>();

    public WebHavenSession(String username, String password, String characterName) {
        this.username = username;
        this.password = password;
        this.characterName = characterName;
        this.shouldClose = false;
        this.initialChatCallbacks = new ArrayList<>();
        this.initialObjectChangeCallbacks = new ArrayList<>();
        this.errorCallbacks = new ArrayList<>();
    }

    public WebHavenSession(String username, String password, String characterName, ArrayList<ChatCallback> initialChatCallbacks, ArrayList<ObjectChangeCallback> initialObjectChangeCallbacks, ArrayList<PseudoWidgetErrorCallback> errorCallbacks) {
        this.username = username;
        this.password = password;
        this.characterName = characterName;
        this.shouldClose = false;
        this.initialChatCallbacks = initialChatCallbacks;
        this.initialObjectChangeCallbacks = initialObjectChangeCallbacks;
        this.errorCallbacks = errorCallbacks;
    }

    public WebHavenSession(String username, String password, String characterName, ArrayList<ChatCallback> initialChatCallbacks, ArrayList<ObjectChangeCallback> initialObjectChangeCallbacks) {
        this.username = username;
        this.password = password;
        this.characterName = characterName;
        this.shouldClose = false;
        this.initialChatCallbacks = initialChatCallbacks;
        this.initialObjectChangeCallbacks = initialObjectChangeCallbacks;
        this.errorCallbacks = new ArrayList<>();
    }

    public WebHavenSession(String username, String password, String characterName, ArrayList<ChatCallback> initialChatCallbacks) {
        this.username = username;
        this.password = password;
        this.characterName = characterName;
        this.shouldClose = false;
        this.initialChatCallbacks = initialChatCallbacks;
        this.initialObjectChangeCallbacks = new ArrayList<>();
        this.errorCallbacks = new ArrayList<>();
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
            return false;
        }

        handler.addChatCallback(cb);
        return true;

    }

    public boolean addObjectChangeCallback(ObjectChangeCallback cb) {
        if(handler == null) {
            return false;
        }

        handler.addObjectChangeCallback(cb);
        return true;
    }

    public boolean addErrorCallback(PseudoWidgetErrorCallback cb) {
        if(handler == null) {
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
            this.handler = new PlayerHandler(connection, this.characterName);
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
}
