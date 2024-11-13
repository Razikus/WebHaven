package tech.razikus.headlesshaven;

import java.util.List;
import java.util.Objects;

public class PoseData {
    private final List<ResourceInformationLazyProxy> resources;
    private final boolean interpolation;
    private final float transitionTime;  // only used for transition poses

    public PoseData(List<ResourceInformationLazyProxy> resources,
                    boolean interpolation,
                    float transitionTime) {
        this.resources = resources;
        this.interpolation = interpolation;
        this.transitionTime = transitionTime;
    }

    public List<ResourceInformationLazyProxy> getResources() {
        return resources;
    }

    public boolean isInterpolation() {
        return interpolation;
    }

    public float getTransitionTime() {
        return transitionTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PoseData poseData = (PoseData) o;
        return interpolation == poseData.interpolation && Float.compare(transitionTime, poseData.transitionTime) == 0 && Objects.equals(resources, poseData.resources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resources, interpolation, transitionTime);
    }

    @Override
    public String toString() {
        return "PoseData{" +
                "resources=" + resources +
                ", interpolation=" + interpolation +
                ", transitionTime=" + transitionTime +
                '}';
    }
}