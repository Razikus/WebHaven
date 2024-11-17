package tech.razikus.headlesshaven;

import haven.ResCache;
import haven.Resource;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

class NameVersion {
    private String name;
    private int version;

    public NameVersion(String name, int version) {
        this.name = name;
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NameVersion that = (NameVersion) o;
        return version == that.version && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version);
    }

    @Override
    public String toString() {
        return "NameVersion{" +
                "name='" + name + '\'' +
                ", version=" + version +
                '}';
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public static NameVersion fromResourceInformation(ResourceInformation info) {
        return new NameVersion(info.getName(), info.getResver());
    }
}

class GlobalShareableResourceHammer {
    public static final ShareableResourceHammer HAMMER = new ShareableResourceHammer();
    private static Thread hammerThread = new Thread(HAMMER);
    static {
        URI uri = null;
        try {
            uri = new URI("https://game.havenandhearth.com/res/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e); // Should never happen
        }
        ResCache simpleCache = new SimpleResourceCache();
        Resource.setcache(simpleCache);
        Resource.addurl(uri);

        hammerThread.start();
    }
    public static ShareableResourceHammer getInstance() {
        return HAMMER;
    }

}

public class ShareableResourceHammer implements  Runnable {
    private BlockingQueue<NameVersion> resourceInformationQueue  = new LinkedBlockingQueue<>();
    private ConcurrentHashMap<NameVersion, Resource> resourceHashMap = new ConcurrentHashMap<>();

    private CopyOnWriteArrayList<ResourceLoadedCallback> callbacks = new CopyOnWriteArrayList<>();

    private boolean shouldClose = false;

    public void setShouldClose(boolean shouldClose) {
        this.shouldClose = shouldClose;
    }

    public boolean isShouldClose() {
        return shouldClose;
    }

    public void loadResource(NameVersion info) {
        resourceInformationQueue.add(info);
    }

    public void loadResource(String name, int version) {
        loadResource(new NameVersion(name, version));
    }

    public Resource getResource(NameVersion info) {
        return resourceHashMap.get(info);
    }

    public Resource getResource(String name, int version) {
        return getResource(new NameVersion(name, version));
    }

    public ArrayList<Resource> getAllLoadedResources() {
        return new ArrayList<>(resourceHashMap.values());
    }

    public void addCallback(ResourceLoadedCallback callback) {
        callbacks.add(callback);
    }

    public void removeCallback(ResourceLoadedCallback callback) {
        callbacks.remove(callback);
    }


    @Override
    public void run() {
        System.out.println("HAMMER STARTED");
        while (!isShouldClose()) {
            NameVersion info = null;
            try {
                info = resourceInformationQueue.take();
                Resource resource = Resource.remote().loadwait(info.getName(), info.getVersion());
                if(info.getName().contains("flute")) {
                    System.out.println("LOADED FLUTE: " +  resource);
                    ArrayList<Resource> res = new ArrayList<>();
                    res.add(resource);
                    DebugRender.dumpAllResources(res, "debug");

                }
                resourceHashMap.put(info, resource);
                for (ResourceLoadedCallback callback: callbacks) {
                    callback.onFullResourceLoaded(resource);
                }
            } catch (InterruptedException e) {
                this.shouldClose = true;
            }
        }

    }

}
