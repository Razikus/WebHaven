package tech.razikus.headlesshaven.bot;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ClassFinder {
    public static class ProgramInfo {
        public Class<? extends AbstractProgram> programClass;
        public HashMap<String, String> declaredArgs;

        public ProgramInfo(Class<? extends AbstractProgram> programClass, HashMap<String, String> declaredArgs) {
            this.programClass = programClass;
            this.declaredArgs = declaredArgs;
        }
    }

    public static class ProgramInfoSerializable {
        public String programClass;
        public HashMap<String, String> declaredArgs;

        public ProgramInfoSerializable(Class<? extends AbstractProgram> programClass, HashMap<String, String> declaredArgs) {
            this.programClass = programClass.getName();
            this.declaredArgs = declaredArgs;
        }
    }


    public static Set<ProgramInfoSerializable> findAllSubclassesWithArgsSerializable() {
        Set<ProgramInfo> programInfos = findAllSubclassesWithArgs();
        Set<ProgramInfoSerializable> programInfoSerializables = new HashSet<>();
        for (ProgramInfo prog: programInfos) {
            programInfoSerializables.add(new ProgramInfoSerializable(prog.programClass, prog.declaredArgs));
        }
        return programInfoSerializables;

    }


    public static Set<ProgramInfo> findAllSubclassesWithArgs() {
        ConfigurationBuilder config = new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forJavaClassPath())
                .setScanners(Scanners.SubTypes);

        Reflections reflections = new Reflections(config);

        Set<Class<? extends AbstractProgram>> subclasses =
                reflections.getSubTypesOf(AbstractProgram.class);

        Set<ProgramInfo> programInfos = new HashSet<>();

        for (Class<? extends AbstractProgram> clazz : subclasses) {
            try {
                Field declaredArgsField = clazz.getDeclaredField("declaredArgs");
                declaredArgsField.setAccessible(true);

                @SuppressWarnings("unchecked")
                HashMap<String, String> declaredArgs =
                        (HashMap<String, String>) declaredArgsField.get(null);

                programInfos.add(new ProgramInfo(clazz, declaredArgs));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                programInfos.add(new ProgramInfo(clazz, new HashMap<>()));
            }
        }

        return programInfos;
    }
}