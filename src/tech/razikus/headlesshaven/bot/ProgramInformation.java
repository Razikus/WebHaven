package tech.razikus.headlesshaven.bot;

import java.util.HashSet;
import java.util.Objects;

public class ProgramInformation {
    private String name;
    private String programClass;
    private HashSet<String> sessionNames;

    public ProgramInformation(String name, String programClass, HashSet<String> sessionNames) {
        this.name = name;
        this.programClass = programClass;
        this.sessionNames = sessionNames;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProgramClass() {
        return programClass;
    }

    public void setProgramClass(String programClass) {
        this.programClass = programClass;
    }

    public HashSet<String> getSessionNames() {
        return sessionNames;
    }

    public void setSessionNames(HashSet<String> sessionNames) {
        this.sessionNames = sessionNames;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProgramInformation that = (ProgramInformation) o;
        return Objects.equals(name, that.name) && Objects.equals(programClass, that.programClass) && Objects.equals(sessionNames, that.sessionNames);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, programClass, sessionNames);
    }

    @Override
    public String toString() {
        return "ProgramInformation{" +
                "name='" + name + '\'' +
                ", programClass='" + programClass + '\'' +
                ", sessionNames=" + sessionNames +
                '}';
    }
}
