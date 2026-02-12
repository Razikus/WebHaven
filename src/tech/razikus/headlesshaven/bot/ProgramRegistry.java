package tech.razikus.headlesshaven.bot;

import tech.razikus.headlesshaven.WebHavenSessionManager;

import java.util.HashMap;

public class ProgramRegistry {

    public static AbstractProgram instantiate(String className,
                                              String progname,
                                              WebHavenSessionManager manager,
                                              Credential credential, HashMap<String, String> runningArgs) throws Exception {


        Class<?> clazz = Class.forName(className);
        return (AbstractProgram) clazz.getDeclaredConstructor(
                String.class,
                WebHavenSessionManager.class,
                Credential.class,
                HashMap.class
        ).newInstance(progname, manager, credential, runningArgs);
    }

    public static AbstractProgram instantiateFromClass(Class<?> clazz,
                                              String progname,
                                              WebHavenSessionManager manager,
                                              Credential credential, HashMap<String, String> runningArgs) throws Exception {


        return (AbstractProgram) clazz.getDeclaredConstructor(
                String.class,
                WebHavenSessionManager.class,
                Credential.class,
                HashMap.class
        ).newInstance(progname, manager, credential, runningArgs);
    }
}
