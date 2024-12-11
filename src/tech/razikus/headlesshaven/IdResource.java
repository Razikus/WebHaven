package tech.razikus.headlesshaven;

import haven.Resource;

import java.util.Objects;

public class IdResource {
    private int id;
    private Resource resource;

    public IdResource(int id, Resource resource) {
        this.id = id;
        this.resource = resource;
    }

    public int getId() {
        return id;
    }

    public Resource getResource() {
        return resource;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdResource that = (IdResource) o;
        return id == that.id && Objects.equals(resource, that.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, resource);
    }

    @Override
    public String toString() {
        return "IdResource{" +
                "id=" + id +
                ", resource=" + resource +
                '}';
    }
}
