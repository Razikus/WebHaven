package tech.razikus.headlesshaven;

import haven.*;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

public class ResourceManager {

    private HashMap<Integer, ResourceInformation> resourceInformationHashMap = new HashMap<>();
    private final Map<String, ResourceClickableInfo> cache = new ConcurrentHashMap<>();

    public void addResource(ResourceInformation info) {
        resourceInformationHashMap.put(info.getId(), info);
    }

    public static Coord readCoord(Message msg) {
        return new Coord(msg.int16(), msg.int16());
    }

    public CompletableFuture<ResourceClickableInfo> getResourceInfo(String resName) {
        ResourceClickableInfo cached = cache.get(resName);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                ResourceClickableInfo info = extractResourceInfo(resName);
                cache.put(resName, info);
                return info;
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }

    private static int extractMeshId(LimitMessage buf) {

        int fl = buf.uint8();
        int num = buf.uint16();
        int matid = buf.int16();
        if((fl & 2) != 0) {
            return buf.int16();
        } else {
            return -1;
        }
    }

    private ResourceClickableInfo extractResourceInfo(String resName) {
        ResourceClickableInfo info = new ResourceClickableInfo(resName);

        try {
            byte[] data = ResourceReader.downloadResource(resName);
            Message in = new StreamMessage(new ByteArrayInputStream(data));

            in.bytes("Haven Resource 1".length());
            in.uint16(); // version
            int currentMeshId = -1;
            while (!in.eom()) {
                String layerType = in.string();
                int len = in.int32();
                LimitMessage layerMsg = new LimitMessage(in, len);

                switch (layerType) {
                    case "mesh":
                        currentMeshId = extractMeshId(layerMsg);
                        if(resName.contains("timberh")) {
                            System.out.println("MESH ID: "+ currentMeshId  + " " + resName);
                        }
                        layerMsg.skip();
                        break;
                    case "neg":
                        Coord cc = readCoord(layerMsg);
                        Coord[][] ep = new Coord[8][0];
                        info.setClickCenter(cc);
                        layerMsg.skip(12);
                        int en = layerMsg.uint8();
                        for (int i = 0; i < en; i++) {
                            int epid = layerMsg.uint8();
                            int cn = layerMsg.uint16();
                            ep[epid] = new Coord[cn];
                            for (int o = 0; o < cn; o++) {
                                ep[epid][o] = readCoord(layerMsg);
                            }
                        }
                        info.setClickPoints(ep);
                        if(resName.contains("timberh")) {
                            System.out.println("NEG: "+ currentMeshId  + " " + resName);
                        }

                        break;

                    case "obst":
                        int ver = layerMsg.uint8();
                        if (ver >= 2) layerMsg.string(); // id
                        int numPolys = layerMsg.uint8();
                        for (int i = 0; i < numPolys; i++) {
                            int points = layerMsg.uint8();
                            List<Coord2d> polyPoints = new ArrayList<>();
                            for (int j = 0; j < points; j++) {
                                float x = layerMsg.float16();
                                float y = layerMsg.float16();
                                polyPoints.add(new Coord2d(x, y));
                            }
                            info.addObstacle(polyPoints);
                        }
                        break;

                    default:
                        if(resName.contains("timberh")) {
                            System.out.println("SKIP: " + layerType + " CURRENT MESH ID: " + currentMeshId);
                        }
                        layerMsg.skip(len);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to extract resource info: " + resName, e);
        }

        return info;
    }
    public ResourceInformation getResource(int id) {
        return resourceInformationHashMap.get(id);
    }

}
