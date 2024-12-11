package tech.razikus.headlesshaven;

import com.google.gson.*;
import haven.Resource;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.json.JsonMapper;
import io.javalin.plugin.bundled.CorsPluginConfig;
import io.javalin.websocket.WsContext;
import org.jetbrains.annotations.NotNull;
import tech.razikus.headlesshaven.bot.*;
import tech.razikus.headlesshaven.bot.automation.WebHavenSessionInformer;
import tech.razikus.headlesshaven.script.GroovyScriptEngine;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;



public class WebHavenSessionManager {
    private final Map<String, WebHavenSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, AbstractProgram> programs = new ConcurrentHashMap<>();
    private final Map<String, WsContext> wsConnections = new ConcurrentHashMap<>();
    private final Gson gson;

    private static GroovyScriptEngine engine = new GroovyScriptEngine("./scripts");

    public WebHavenSessionManager() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(PseudoObject.class, new PseudoObjectSerializer())
                .registerTypeAdapter(Resource.class, new ResourceSerializer())
                .registerTypeAdapter(FlowerMenuPseudoWidget.class, new FlowerMenuPseudoWidgetSerializer())
                .create();

        // @todo do it differently
        WebHavenSessionInformer informer = new WebHavenSessionInformer(this);
        new Thread(informer).start();

    }

    public void startProgram(AbstractProgram program) throws InterruptedException {
        new Thread(program).start();
        this.programs.put(program.getProgname(), program);
    }

    public Map<String, WebHavenSession> getSessions() {
        return sessions;
    }


    public Map<String, AbstractProgram> getPrograms() {
        return this.programs;
    }

    public Set<ProgramInformation> getProgramInformations() {
        Set<ProgramInformation> infos = new HashSet<>();
        for (Map.Entry<String, AbstractProgram> abstractProgramMap: getPrograms().entrySet()) {
            infos.add(abstractProgramMap.getValue().getProgramInformation());
        }
        return infos;
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

    private void stopProgram(String programName) {
        AbstractProgram program = this.programs.get(programName);
        if(program != null) {
            program.setShouldClose(true);
            programs.remove(programName);
            broadcastMessage("Server", "Program " + programName + " stopped");
        }
    }

    public void brodcastFromProgram(String programID, Object what) {
        String toJson = gson.toJson(what);
        String message = String.format("{\"type\": \"programdata\", \"program\": \"%s\", \"data\": %s}", programID, toJson);
        wsConnections.values().forEach(ctx -> {
            try {
                ctx.send(message);

            } catch (Exception e){
                System.out.println(e);
            }
        });
    }

    public void broadcastState(String sessionId, ProgramInformation programInformation, WebHavenState state) {
        String jsonState = gson.toJson(state);
        String progInfoString = gson.toJson(programInformation);
        String message = String.format("{\"type\": \"state\", \"program\": %s, \"session\": \"%s\", \"data\": %s}",
                progInfoString, sessionId, jsonState);
        wsConnections.values().forEach(ctx -> {
            try {
                ctx.send(message);

            } catch (Exception e){
                System.out.println(e);
            }
        });
    }

    public void broadcastChat(String sessionId, ChatMessage chatState) {
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
    public void broadcastAvailableSessions( Set<String> sessions) {
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

    public void broadcastAvailablePrograms(Set<ProgramInformation> programs) {
        String jsonState = gson.toJson(programs);
        String message = String.format("{\"type\": \"programs\", \"data\": %s}",jsonState);
        wsConnections.values().forEach(ctx -> {
            try {
                ctx.send(message);

            } catch (Exception e){
                System.out.println(e);
            }
        });
    }

    public void broadcastMessage(String sender, String message) {
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


    private static HashMap<String, String> parseRunningArgs(Map<String, String> env) {
        HashMap<String, String> runningArgs = new HashMap<>();
        final String PREFIX = "RUNNINGARG_";

        for (Map.Entry<String, String> entry : env.entrySet()) {
            if (entry.getKey().startsWith(PREFIX)) {
                String argName = entry.getKey().substring(PREFIX.length()).toLowerCase();
                runningArgs.put(argName, entry.getValue());
            }
        }

        return runningArgs;
    }

    // Modified main class
    public static void main(String[] args) {
        Map<String, String> env = System.getenv();
        HashMap<String, String> runningArgs = parseRunningArgs(env);

        String host = "localhost";
        String port = "7071";

        String initialUser = "";
        String initialPassword = "";
        String initialChar = "";

        String initialProgram = PlayerSpotterProgram.class.getName();



        if(env.containsKey("HOST")) {
            host = env.get("HOST");
        }
        if(env.containsKey("PORT")) {
            port = env.get("PORT");
        }

        if(env.containsKey("INITIAL_PROGRAM")) {
            initialProgram = env.get("INITIAL_PROGRAM");
        }

        if(env.containsKey("AUTOLOGIN_USER")){
            initialUser = env.get("AUTOLOGIN_USER");
            initialPassword = env.get("AUTOLOGIN_PASSWORD");
            initialChar = env.get("AUTOLOGIN_CHAR");
        }

        int parsedPort = Integer.parseInt(port);

        WebHavenSessionManager sessionManager = new WebHavenSessionManager();

        JsonMapper gsonMapper = new JsonMapper() {
            @Override
            public String toJsonString(@NotNull Object obj, @NotNull Type type) {
                return sessionManager.gson.toJson(obj, type);
            }

            @Override
            public <T> T fromJsonString(@NotNull String json, @NotNull Type targetType) {
                return sessionManager.gson.fromJson(json, targetType);
            }
        };
        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> cors.addRule(CorsPluginConfig.CorsRule::anyHost));
            config.staticFiles.add("/dist", Location.CLASSPATH);

            config.jetty.modifyWebSocketServletFactory(wsFactory -> {
                wsFactory.setIdleTimeout(Duration.ofMinutes(5));
            });
            config.jsonMapper(gsonMapper);

        }).start(host, parsedPort);

        app.get("/programs", ctx -> {
            engine.reloadScripts();
            Set<ClassFinder.ProgramInfoSerializable> allFromClassPath = ClassFinder.findAllSubclassesWithArgsSerializable();
            for (Map.Entry<String, Class<? extends AbstractProgram>> w: engine.getLoadedClasses().entrySet()) {
                allFromClassPath.add(new ClassFinder.ProgramInfoSerializable(w.getValue(), new HashMap<>()));
            }
            ctx.json(allFromClassPath);
        });


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
                    // @todo refactor xd
                    switch (type) {
                        case "proginput":
                            String programId = command.get("program").getAsString();

                            AbstractProgram program = sessionManager.getPrograms().get(programId);
                            if(program != null) {
                                program.handleInput(command);
                            } else {
                                ctx.send("{\"error\": \"Program not found\"}");
                            }
                            break;
                        case "start_program":
                            String username = command.get("username").getAsString();
                            String password = command.get("password").getAsString();
                            String altname = command.get("altname").getAsString();
                            String prog = command.get("program").getAsString();
                            String programName = command.get("programName").getAsString();
                            JsonObject argsJson = command.get("args").getAsJsonObject();
                            HashMap<String, String> argsConverted = new HashMap<>();

                            for (Map.Entry<String, JsonElement> entry : argsJson.entrySet()) {
                                argsConverted.put(entry.getKey(), entry.getValue().getAsString());
                            }
                            Credential credential = new Credential(username, password, altname);
                            System.out.println(engine.getLoadedClasses());
                            if(engine.getLoadedClasses().get(prog) != null) {
                                System.out.println("INSTANTIATE GROOVY: " +  engine.getLoadedClasses().get(prog));
                                AbstractProgram program2 = ProgramRegistry.instantiateFromClass(engine.getLoadedClasses().get(prog), programName, sessionManager, credential, argsConverted);
                                sessionManager.startProgram(program2);
                            } else {
                                System.out.println("INSTANTIATE NORMAL: " +  prog);
                                AbstractProgram program2 = ProgramRegistry.instantiate(prog, programName, sessionManager, credential, argsConverted);
                                System.out.println(program2);
                                sessionManager.startProgram(program2);
                            }
                            break;

                        case "stop_session":
                            String sessionId = command.get("session").getAsString();
                            sessionManager.stopSession(sessionId);
                            break;

                        case "stop_program":
                            String progName = command.get("program").getAsString();
                            sessionManager.stopProgram(progName);
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
            if(initialPassword.isEmpty() ) {
                throw new RuntimeException("IF AUTOLOGIN_USER IS NOT EMPTY THEN AUTOLOGIN_PASSWORD AND  AUTOLOGIN_CHAR MUST BE NOT EMPTY");
            }
            try {
                Credential credentials = new Credential(initialUser, initialPassword, initialChar);

                AbstractProgram program = ProgramRegistry.instantiate(initialProgram, initialProgram, sessionManager, credentials, runningArgs);

                sessionManager.startProgram(program);
            } catch (InterruptedException e) {
                System.out.println("CANNOT AUTHENTICATE INITIAL USER OR SOMETHING: " + e);
                throw new RuntimeException(e);
            } catch (Exception e) {
                System.out.println("CANNOT AUTHENTICATE INITIAL USER OR SOMETHING: " + e);
                throw new RuntimeException(e);
            }
        } else {
                System.out.println(
                        "HINT: You can set AUTOLOGIN_USER, AUTOLOGIN_PASSWORD, and AUTOLOGIN_CHAR to autologin some session"
                );
        }
    }

}
