package tech.razikus.headlesshaven;

import haven.ResCache;
import haven.Resource;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

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
    private BlockingQueue<ResourceInformation> resourceInformationQueue  = new LinkedBlockingQueue<>();
    private ConcurrentHashMap<ResourceInformation, Resource> resourceHashMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Resource> resourceInformationHashMap = new ConcurrentHashMap<>();

    private boolean shouldClose = false;

    public void setShouldClose(boolean shouldClose) {
        this.shouldClose = shouldClose;
    }

    public boolean isShouldClose() {
        return shouldClose;
    }

    public void loadResource(ResourceInformation info) {
        resourceInformationQueue.add(info);
    }

    public Resource getResource(ResourceInformation info) {
        return resourceHashMap.get(info);
    }

    public Resource getResourceById(int id) {
        return resourceInformationHashMap.get(id);
    }

    @Override
    public void run() {
        System.out.println("HAMMER STARTED");
        while (!isShouldClose()) {
            ResourceInformation info = null;
            try {
                info = resourceInformationQueue.take();
                Resource resource = Resource.remote().loadwait(info.getName(), info.getResver());
                resourceHashMap.put(info, resource);
                resourceInformationHashMap.put(info.getId(), resource);
            } catch (InterruptedException e) {
                this.shouldClose = true;
            }
        }

    }
}
