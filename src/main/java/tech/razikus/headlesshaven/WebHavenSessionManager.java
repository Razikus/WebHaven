package tech.razikus.headlesshaven;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.bundled.CorsPluginConfig;
import io.javalin.websocket.WsContext;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class WebHavenSessionManager {
    private final Map<String, WebHavenSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, WsContext> wsConnections = new ConcurrentHashMap<>();
    private final Gson gson;

    public WebHavenSessionManager() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(PseudoObject.class, new PseudoObjectSerializer())
                .create();

        // @todo do it differently
        WebHavenSessionInformer informer = new WebHavenSessionInformer(this);
        new Thread(informer).start();
    }

    public void startSession(String username, String password, String altname) throws InterruptedException {
        String sessName = username + "-" + altname;
        if(sessions.containsKey(sessName)) {
            return;
        }

        ChatCallback chatStreamer = new BrodcastingChatCallback(this, sessName);
        ArrayList<ChatCallback> callbacks = new ArrayList<>();
        callbacks.add(chatStreamer);
        WebHavenSession session = new WebHavenSession(username, password, altname, callbacks, true);
        session.authenticate();

        Thread sessionThread = new Thread(session);
        sessionThread.start();

        Thread queueChecker = new Thread(() -> {
            while (!session.connectionCreated()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println("WAITING FOR CONNECTION... ");

            }

            while (session.isAlive()) {
                WebHavenState state = null; // Get state from queue
                try {
                    while(session.isAlive() && session.getLastState() == null) {
                        Thread.sleep(100);
                        System.out.println("WAITING FOR FIRST STATE... ");
                    }
                    state = session.getLastState();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if(state != null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    broadcastState(sessName, state);
                }
            }
        });
        queueChecker.start();
        sessions.put(sessName, session);
    }

    public void stopSession(String altname) {
        WebHavenSession session = sessions.get(altname);
        if (session != null) {
            // Implement proper shutdown in WebHavenSession
            session.setShouldClose(true);
            sessions.remove(altname);
            broadcastMessage("Server", "Session " + altname + " stopped");
        }
    }

    private void broadcastState(String sessionId, WebHavenState state) {
        String jsonState = gson.toJson(state);
        String message = String.format("{\"type\": \"state\", \"session\": \"%s\", \"data\": %s}",
                sessionId, jsonState);
        wsConnections.values().forEach(ctx -> {
            try {
                ctx.send(message);

            } catch (Exception e){
                System.out.println(e);
            }
        });
    }

    private void broadcastChat(String sessionId, ChatMessage chatState) {
        String jsonState = gson.toJson(chatState);
        String message = String.format("{\"type\": \"message\", \"session\": \"%s\", \"data\": %s}",
                sessionId, jsonState);
        wsConnections.values().forEach(ctx -> {
            try {
                ctx.send(message);

            } catch (Exception e){
                System.out.println(e);
            }
        });
    }
    private void broadcastAvailableSessions( Set<String> sessions) {
        String jsonState = gson.toJson(sessions);
        String message = String.format("{\"type\": \"sessions\", \"data\": %s}",jsonState);
        wsConnections.values().forEach(ctx -> {
            try {
                ctx.send(message);

            } catch (Exception e){
                System.out.println(e);
            }
        });
    }

    private void broadcastMessage(String sender, String message) {
        String broadcastMsg = String.format("{\"type\": \"internal\", \"sender\": \"%s\", \"message\": \"%s\"}",
                sender, message);
        wsConnections.values().forEach(ctx -> {
            try {
                ctx.send(broadcastMsg);

            } catch (Exception e) {
                System.out.println(e);
            }
        });
    }

    // Modified main class
    public static void main(String[] args) {
        Map<String, String> env = System.getenv();

        String host = "localhost";
        String port = "7071";

        String initialUser = "";
        String initialPassword = "";
        String initialChar = "";

        if(env.containsKey("HOST")) {
            host = env.get("HOST");
        }
        if(env.containsKey("PORT")) {
            port = env.get("PORT");
        }

        if(env.containsKey("AUTOLOGIN_USER")){
            initialUser = env.get("AUTOLOGIN_USER");
            initialPassword = env.get("AUTOLOGIN_PASSWORD");
            initialChar = env.get("AUTOLOGIN_CHAR");
        }

        int parsedPort = Integer.parseInt(port);

        WebHavenSessionManager sessionManager = new WebHavenSessionManager();

        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> cors.addRule(CorsPluginConfig.CorsRule::anyHost));
            config.staticFiles.add("/public/dist", Location.CLASSPATH);

            config.jetty.modifyWebSocketServletFactory(wsFactory -> {
                wsFactory.setIdleTimeout(Duration.ofMinutes(5));
            });
        }).start(host, parsedPort);


        app.ws("/haven", ws -> {
            ws.onConnect(ctx -> {

                String userId = "user" + sessionManager.wsConnections.size();
                ctx.attribute("userId", userId);
                sessionManager.wsConnections.put(userId, ctx);
                sessionManager.broadcastMessage("Server", userId + " connected");

            });


            ws.onMessage(ctx -> {
                // Handle commands from websocket clients
                try {
                    JsonObject command = JsonParser.parseString(ctx.message()).getAsJsonObject();
                    String type = command.get("type").getAsString();
                    String userId = ctx.attribute("userId");

                    switch (type) {
                        case "start_session":
                            String username = command.get("username").getAsString();
                            String password = command.get("password").getAsString();
                            String altname = command.get("altname").getAsString();
                            sessionManager.startSession(username, password, altname);
                            break;

                        case "stop_session":
                            String sessionId = command.get("session").getAsString();
                            sessionManager.stopSession(sessionId);
                            break;

                        case "chat":
                            String chatSessionID = command.get("session").getAsString();
                            String channel = command.get("channel").getAsString();
                            String text = command.get("text").getAsString();
                            WebHavenSession sess = sessionManager.sessions.get(chatSessionID);
                            sess.getWidgetManager().getChatChannelByName(channel).sendMessage(text);
                            break;

                        default:
                            ctx.send("{\"error\": \"Unknown command type\"}");
                    }
                } catch (Exception e) {
                    ctx.send("{\"error\": \"" + e.getMessage() + "\"}");
                }
            });

            ws.onClose(ctx -> {
                String userId = ctx.attribute("userId");
                if (userId != null) {
                    sessionManager.wsConnections.remove(userId);
                    sessionManager.broadcastMessage("Server", userId + " disconnected");
                } else {
                    System.out.println("CLOSE: unknown (no userId found in context)");
                }
            });

            ws.onError(ctx -> {
                String userId = ctx.attribute("userId");
                if (userId != null) {
                    sessionManager.wsConnections.remove(userId);
                    sessionManager.broadcastMessage("Server", userId + " ERROR");
                } else {
                    System.out.println("ERROR: unknown (no userId found in context)");
                }
            });
        });
        System.out.println("INITIALIZATION COMPLETE");
        if(!initialUser.isEmpty()) {
            if(initialPassword.isEmpty() || initialChar.isEmpty()) {
                throw new RuntimeException("IF AUTOLOGIN_USER IS NOT EMPTY THEN AUTOLOGIN_PASSWORD AND  AUTOLOGIN_CHAR MUST BE NOT EMPTY");
            }
            try {
                sessionManager.startSession(initialUser, initialPassword, initialChar);
            } catch (InterruptedException e) {
                System.out.println("CANNOT AUTHENTICATE INITIAL USER OR SOMETHING: " + e);
                throw new RuntimeException(e);
            }
        } else {
                System.out.println(
                        "HINT: You can set AUTOLOGIN_USER, AUTOLOGIN_PASSWORD, and AUTOLOGIN_CHAR to autologin some session"
                );
        }
    }

    class BrodcastingChatCallback extends ChatCallback{
        private String sessionId;
        private WebHavenSessionManager manager;

        public BrodcastingChatCallback(WebHavenSessionManager manager, String sessionId) {
            this.manager = manager;
            this.sessionId = sessionId;
        }
        @Override
        public void onChatMessage(ChatMessage message) {
            this.manager.broadcastChat(this.sessionId, message);

        }
    }

    class WebHavenSessionInformer implements Runnable {
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
            while(isRunning) {
                try {
                    Set<String> sessions = manager.sessions.keySet();
                    manager.broadcastAvailableSessions(sessions);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    isRunning = false;
                }
            }

        }
    }
}
