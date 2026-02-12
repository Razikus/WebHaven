package tech.razikus.headlesshaven;

import haven.Message;

import java.util.Objects;

public class ResourceInformationLazyProxy {
    private ResourceManager manager;
    private int resID;
    private Message resData;

    public ResourceInformationLazyProxy(ResourceManager manager, int resID, Message resData) {
        this.manager = manager;
        this.resID = resID;
        this.resData = resData;
    }

    public int getResID() {
        return resID;
    }

    public Message getResData() {
        return resData;
    }

    public InstantiatedResourceInformation getResource() {
        ResourceInformation information = this.manager.getResource(this.resID);
        if (information == null) {
            return new InstantiatedResourceInformation(new ResourceInformation(resID, null, -1), this.resData);
        } else {
            return new InstantiatedResourceInformation(information, this.resData);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceInformationLazyProxy that = (ResourceInformationLazyProxy) o;
        return resID == that.resID;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(resID);
    }

    @Override
    public String toString() {
        return "ResourceInformationLazyProxy{" +
                ", resID=" + resID +
                ", resData=" + resData +
                ", resource=" + getResource() +
                '}';
    }
}
