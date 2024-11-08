package tech.razikus.headlesshaven;

import java.util.HashMap;

public class ResourceManager {

    private HashMap<Integer, ResourceInformation> resourceInformationHashMap = new HashMap<>();

    public void addResource(ResourceInformation info) {
        resourceInformationHashMap.put(info.getId(), info);
    }

    public ResourceInformation getResource(int id) {
        return resourceInformationHashMap.get(id);
    }

}
