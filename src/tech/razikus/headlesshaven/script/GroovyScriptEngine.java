package tech.razikus.headlesshaven.script;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilerConfiguration;
import tech.razikus.headlesshaven.WebHavenSessionManager;
import tech.razikus.headlesshaven.bot.AbstractProgram;
import tech.razikus.headlesshaven.bot.Credential;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GroovyScriptEngine {
    private final String scriptsDirectory;
    private final GroovyClassLoader classLoader;
    private final Map<String, Class<? extends AbstractProgram>> loadedScripts = new HashMap<>();

    public GroovyScriptEngine(String scriptsDirectory) {
        this.scriptsDirectory = scriptsDirectory;
        CompilerConfiguration config = new CompilerConfiguration();
        config.setSourceEncoding("UTF-8");
        this.classLoader = new GroovyClassLoader(
                Thread.currentThread().getContextClassLoader(),
                config
        );
    }

    public Map<String, Class<? extends AbstractProgram>> getLoadedScripts() {
        return Collections.unmodifiableMap(loadedScripts);
    }

    public Map<String, Class<? extends  AbstractProgram>> getLoadedClasses() {
        Map<String, Class<? extends AbstractProgram>> loadedClasses = new HashMap<>();
        for (Class<? extends AbstractProgram> w: getLoadedScripts().values()) {
            loadedClasses.put(w.getName(), w);
        }
        return loadedClasses;
    }

    @SuppressWarnings("unchecked")
    public void loadScripts() throws IOException {
        Path scriptsPath = Paths.get(scriptsDirectory);
        if (!Files.exists(scriptsPath)) {
            Files.createDirectories(scriptsPath);
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(scriptsPath, "*.groovy")) {
            for (Path path : stream) {
                String scriptName = path.getFileName().toString();
                try {
                    File scriptFile = path.toFile();
                    Class<?> scriptClass = classLoader.parseClass(scriptFile);

                    if (AbstractProgram.class.isAssignableFrom(scriptClass)) {
                        loadedScripts.put(
                                scriptName,
                                (Class<? extends AbstractProgram>) scriptClass
                        );
                    } else {
                        System.err.println("Script " + scriptName + " doesn't extend AbstractProgram");
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load script " + scriptName + ": " + e.getMessage());
                }
            }
        }
    }

    public AbstractProgram instantiateScript(
            String scriptName,
            String progName,
            WebHavenSessionManager manager,
            Credential credential,
            HashMap<String, String> runningArgs) {

        Class<? extends AbstractProgram> scriptClass = loadedScripts.get(scriptName);
        if (scriptClass == null) {
            throw new IllegalArgumentException("Script not found: " + scriptName);
        }

        try {
            return scriptClass.getDeclaredConstructor(
                    String.class,
                    WebHavenSessionManager.class,
                    Credential.class,
                    HashMap.class
            ).newInstance(progName, manager, credential, runningArgs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate script: " + scriptName, e);
        }
    }

    public void reloadScripts() throws IOException {
        loadedScripts.clear();
        classLoader.clearCache();
        loadScripts();
    }
}