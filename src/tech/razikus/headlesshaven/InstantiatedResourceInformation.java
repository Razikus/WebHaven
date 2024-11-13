package tech.razikus.headlesshaven;

import haven.Message;

public class InstantiatedResourceInformation {
    private ResourceInformation information;
    private Message resData;

    public InstantiatedResourceInformation(ResourceInformation information, Message resData) {
        this.information = information;
        this.resData = resData;
    }

    public ResourceInformation getInformation() {
        return information;
    }

    public void setInformation(ResourceInformation information) {
        this.information = information;
    }

    public Message getResData() {
        return resData;
    }

    public void setResData(Message resData) {
        this.resData = resData;
    }


    @Override
    public String toString() {
        return "InstantiatedResourceInformation{" +
                "information=" + information +
                ", resData=" + resData +
                '}';
    }
}
