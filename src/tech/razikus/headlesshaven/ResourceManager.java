package tech.razikus.headlesshaven;

import com.google.gson.*;
import haven.*;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
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

    // @todo - this is a way to load unID resources
    public void loadResource(NameVersion nameVersion) {
        GlobalShareableResourceHammer.HAMMER.loadResource(nameVersion);
    }


    public Resource getRealResource(int id) {
        ResourceInformation info = resourceInformationHashMap.get(id);
        if(info == null) {
            return null;
        }
        return GlobalShareableResourceHammer.HAMMER.getResource(info.getName(), info.getResver());
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



class ResourceSerializer implements JsonSerializer<Resource> {

    @Override
    public JsonElement serialize(Resource src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("name", src.name);
        json.addProperty("version", src.ver);
        JsonArray layers = new JsonArray();
        for (FastMesh.MeshRes layer: src.layers(FastMesh.MeshRes.class)) {
            JsonObject mesh = new JsonObject();
            FastMesh.MeshRes meshRes = layer;
            if(meshRes.id != -1) {
                mesh.addProperty("id", meshRes.id);
                mesh.addProperty("type", "mesh");
                layers.add(mesh);
            }
        }
        for (Resource.Obstacle layer: src.layers(Resource.Obstacle.class)) {
            JsonObject obstacle = new JsonObject();
            obstacle.addProperty("id", layer.id);
            obstacle.add("obstacle", coord2dArrayToJson(layer.p));
            obstacle.addProperty("type", "obstacle");
            layers.add(obstacle);
        }
        json.add("layers", layers);
        return json;
    }
    public JsonArray coord2dArrayToJson(Coord2d[][] coords) {
        JsonArray array = new JsonArray();
        for(Coord2d[] row : coords) {
            JsonArray rowArray = new JsonArray();
            for(Coord2d coord : row) {
                JsonObject point = new JsonObject();
                point.addProperty("x", coord.x);
                point.addProperty("y", coord.y);
                rowArray.add(point);
            }
            array.add(rowArray);
        }
        return array;
    }
}