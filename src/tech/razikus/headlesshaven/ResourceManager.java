package tech.razikus.headlesshaven;

import haven.*;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

public class ResourceManager {

    private HashMap<Integer, ResourceInformation> resourceInformationHashMap = new HashMap<>();

    public void addResource(ResourceInformation info) {
        resourceInformationHashMap.put(info.getId(), info);
        GlobalShareableResourceHammer.HAMMER.loadResource(NameVersion.fromResourceInformation(info));
    }

    @SuppressWarnings("unchecked")
    public static <Layer> Collection<Layer> getLayers(Resource resource) {
        try {
            Field layersField = Resource.class.getDeclaredField("layers");
            layersField.setAccessible(true);
            return (Collection<Layer>) layersField.get(resource);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access layers field", e);
        }
    }

    public ResourceInformation getResource(int id) {
        return resourceInformationHashMap.get(id);
    }

}
