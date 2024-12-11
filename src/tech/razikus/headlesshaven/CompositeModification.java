package tech.razikus.headlesshaven;

import java.util.List;
import java.util.Objects;

public class CompositeModification {
    private final int modifierId;
    private final List<ResourceInformationLazyProxy> resources;
    private final int sequence;

    public CompositeModification(int modifierId, List<ResourceInformationLazyProxy> resources, int sequence) {
        this.modifierId = modifierId;
        this.resources = resources;
        this.sequence = sequence;
    }

    public int getModifierId() {
        return modifierId;
    }

    public List<ResourceInformationLazyProxy> getResources() {
        return resources;
    }

    public int getSequence() {
        return sequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompositeModification that = (CompositeModification) o;
        return modifierId == that.modifierId && sequence == that.sequence && Objects.equals(resources, that.resources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modifierId, resources, sequence);
    }

    @Override
    public String toString() {
        return "CompositeModification{" +
                "modifierId=" + modifierId +
                ", resources=" + resources +
                ", sequence=" + sequence +
                '}';
    }
}
