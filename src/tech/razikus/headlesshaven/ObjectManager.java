package tech.razikus.headlesshaven;

import haven.OCache;

import java.util.ArrayList;
import java.util.HashMap;

public class ObjectManager {

    private ResourceManager resourceManager;
    private PseudoWidgetManager widgetManager;
    private ArrayList<ObjectChangeCallback> callbacks = new ArrayList<>();


    public ObjectManager(ResourceManager resourceManager, PseudoWidgetManager widgetManager, ArrayList<ObjectChangeCallback> callbacks) {
        this.resourceManager = resourceManager;
        this.widgetManager = widgetManager;
        this.callbacks = callbacks;
    }



    final private HashMap<Long, PseudoObject> pseudoObjectHashMap = new HashMap<>();


    public void addObject(PseudoObject obj) {
        synchronized (pseudoObjectHashMap) {
            pseudoObjectHashMap.put(obj.getId(), obj);
        }
    }

    public void removeObject(PseudoObject obj) {
        removeObject(obj.getId());
    }

    public HashMap<Long, PseudoObject> getPseudoObjectHashMap() {
        return pseudoObjectHashMap;
    }

    public HashMap<Long, PseudoObject> getPseudoObjectHashMapTHSafe() {
        synchronized (pseudoObjectHashMap) {
            return new HashMap<>(pseudoObjectHashMap);
        }
    }

    public PseudoObject getPlayer() {
        synchronized (pseudoObjectHashMap) {
            for (PseudoObject obj : pseudoObjectHashMap.values()) {
                if (obj.getId() == widgetManager.getMyGOBId()) {
                    return obj;
                }
            }
        }
        return null;
    }


    public boolean objectExists(long id) {
        synchronized (pseudoObjectHashMap) {
            return pseudoObjectHashMap.containsKey(id);
        }
    }

    public void handleRealDelta(long id, OCache.ObjDelta delta) {
        PseudoObject obj = pseudoObjectHashMap.get(id);
        obj.fillDelta(delta);
    }

    public void handleDelta(OCache.ObjDelta delta) {
        if (delta.rem) {
            removeObject(delta.id);
            System.out.println("OBJECT REMOVED: " + delta.id);
            for (ObjectChangeCallback cb : callbacks) {
                cb.objectRemoved(delta.id);
            }
        } else {
            if(!objectExists(delta.id)) {
                int flags = delta.fl;
                int initFrame = delta.initframe;
                int frame = delta.frame;
                PseudoObject obj = new PseudoObject(resourceManager, widgetManager, delta.id);
                obj.fillNew(delta);
                addObject(obj);
                for (ObjectChangeCallback cb : callbacks) {
                    cb.objectAdded(obj);
                }
            } else {
                handleRealDelta(delta.id, delta);
                for (ObjectChangeCallback cb : callbacks) {
                    if(pseudoObjectHashMap.containsKey(delta.id)) {
                        cb.objectChanged(pseudoObjectHashMap.get(delta.id));
                    }
                }
            }
        }
    }

    public void removeObject(long id) {
        synchronized (pseudoObjectHashMap) {
            pseudoObjectHashMap.remove(id);
        }

    }



}
