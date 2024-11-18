package tech.razikus.headlesshaven;

import java.util.Objects;

public class ResourceInformation {

    private int id;
    private String name;
    private int resver;


    public ResourceInformation(int id, String name, int resver) {
        this.id = id;
        this.name = name;
        this.resver = resver;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceInformation that = (ResourceInformation) o;
        return id == that.id && resver == that.resver && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, resver);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getResver() {
        return resver;
    }

    @Override
    public String toString() {
        return "ResourceInformation{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", resver=" + resver +
                '}';
    }
}
