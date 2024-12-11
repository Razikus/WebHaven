package tech.razikus.headlesshaven;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class PseudoWidgetManager {
    final private HashMap<Integer, PseudoWidget> pseudoWidgetHashMap = new HashMap<>();

    private BuddyPseudoWidget instantiatedBuddy;
    private ResourceManager resourceManager;

    private HashMap<Integer, ChatPseudoWidget> chatWidgets = new HashMap<>();
    private HashMap<Integer, FlowerMenuPseudoWidget> flowerMenus = new HashMap<>();
    private HashMap<Integer, PseudoItem> items = new HashMap<>();
    private HashMap<Integer, PseudoInventory> inventories = new HashMap<>();

    private CopyOnWriteArrayList<ChatCallback> callbacks;

    private CopyOnWriteArrayList<PseudoWidgetErrorCallback> errorCallbacks;
    private CopyOnWriteArrayList<PseudoWidgetCallback> widgetCallbacks;
    private MapViewPseudoWidget mapView = null;

    private ArrayList<PseudoMusicWidget> musicWidgets = new ArrayList<>();


    // @todo move to different components, but why now
    private boolean isInVillage = false;
    private String villageName;
    private int villageID = -1;

    private long myGOBId = -1;
    private String myCharacter;


    public PseudoWidgetManager(ResourceManager manager, CopyOnWriteArrayList<ChatCallback> callbacks, CopyOnWriteArrayList<PseudoWidgetErrorCallback> errorCallbacks, CopyOnWriteArrayList<PseudoWidgetCallback> widgetCallbacks) {
        this.resourceManager = manager;
        this.callbacks = callbacks;
        this.errorCallbacks = errorCallbacks;
        this.widgetCallbacks = widgetCallbacks;
    }



    public boolean isInVillage() {
        return isInVillage;
    }

    public String getVillageName() {
        return villageName;
    }

    public int getVillageID() {
        return villageID;
    }

    public BuddyPseudoWidget getInstantiatedBuddy() {
        return instantiatedBuddy;
    }

    public void dispatchMessage(WidgetMessage message) {
        synchronized (pseudoWidgetHashMap) {
            PseudoWidget found = pseudoWidgetHashMap.get(message.getWidgetID());
            if (found != null) {
                found.ReceiveMessage(message);
            } else {
                if(message.getName().equals("err")) {
                    synchronized (errorCallbacks) {
                        for (PseudoWidgetErrorCallback cb: errorCallbacks) {
                            cb.onError((String) message.getArgs()[0]);
                        }
                    }
                    System.out.println("ERROR: " + message);

                } else {
                    System.out.println("WIDGET NOT FOUND: " + message);
                }
            }
        }
    }

    public ArrayList<PseudoWidget> getWidgetsByType(String type) {
        ArrayList<PseudoWidget> toRet = new ArrayList<>();
        synchronized (pseudoWidgetHashMap) {
            for (Map.Entry<Integer, PseudoWidget> wg: pseudoWidgetHashMap.entrySet()) {
                if (wg.getValue().type.equals(type)) {
                    toRet.add(wg.getValue());
                }
            }
        }
        return toRet;

    }

    public PseudoWidget getWidgetById(int id) {
        synchronized (pseudoWidgetHashMap) {
            return pseudoWidgetHashMap.get(id);
        }
    }

    public String getMyCharacter() {
        return myCharacter;
    }

    public long getMyGOBId() {
        return myGOBId;
    }

    public ChatPseudoWidget getChatChannelByName(String name) {
        for (Map.Entry<Integer, ChatPseudoWidget> wg: chatWidgets.entrySet()) {
            if (wg.getValue().getChatName().equals(name)) {
                return wg.getValue();
            }
        }
        return null;
    }

    public ArrayList<String> getChatChannels() {
        ArrayList<String> toRet = new ArrayList<>();
        for (Map.Entry<Integer, ChatPseudoWidget> wg: chatWidgets.entrySet()) {
            toRet.add(wg.getValue().getChatName());
        }
        return toRet;

    }

    public HashMap<Integer, PseudoInventory> getInventories() {
        return inventories;
    }

    public void addNewWidget(PseudoWidget widget) {
        synchronized (pseudoWidgetHashMap) {
            PseudoWidget toAdd = widget;
            if(widget.type.startsWith("ui/vlg:")) {
                String nameOfVillage = (String) widget.getCargs()[0];
                int idOf = Integer.parseInt(widget.type.substring(7));
                this.villageName = nameOfVillage;
                this.villageID = idOf;
                this.isInVillage = true;
            }



            System.out.println("ADDED WIDGET: " + toAdd);
            switch (widget.type) {
                case "buddy":
                    BuddyPseudoWidget buddyPseudoWidget = new BuddyPseudoWidget(widget);
                    toAdd = buddyPseudoWidget;
                    instantiatedBuddy = buddyPseudoWidget;
                    break;
                case "gameui":
                    myCharacter = (String) widget.cargs[0];
                    myGOBId = (int) widget.cargs[1];
                    break;
                case "mchat":
                    ChatPseudoWidget chatPseudoWidget = new ChatPseudoWidget(widget, this);
                    toAdd = chatPseudoWidget;
                    synchronized (callbacks) {
                        for (ChatCallback cb: callbacks) {
                            chatPseudoWidget.addCallback(cb);
                        }
                        chatWidgets.put(widget.id, chatPseudoWidget);

                    }
                    break;
                case "inv":
                    PseudoInventory pseudoInventory = new PseudoInventory(widget, this);
                    toAdd = pseudoInventory;
                    inventories.put(widget.id, pseudoInventory);
                    break;
                case "item":
                    PseudoItem pseudoItem = new PseudoItem(widget, this.resourceManager);
                    toAdd = pseudoItem;
                    synchronized (items) {
                        items.put(widget.id, pseudoItem);
                    }

                    // @todo callbacks
                    break;
                case "sm":
                    FlowerMenuPseudoWidget flowerMenuPseudoWidget = new FlowerMenuPseudoWidget(this, widget);
                    toAdd = flowerMenuPseudoWidget;
                    flowerMenus.put(widget.id, flowerMenuPseudoWidget);
                    break;
                case "mapview":
                    mapView = new MapViewPseudoWidget(widget);
                    break;
            }
            if(widget.type.startsWith("ui/music:")) {
                PseudoMusicWidget toAddReal = new PseudoMusicWidget(widget);
                toAdd = toAddReal;
                musicWidgets.add(toAddReal);
            }
            synchronized (widgetCallbacks) {
                for (PseudoWidgetCallback cb: widgetCallbacks) {
                    cb.onWidgetCreated(toAdd);
                }
            }
            pseudoWidgetHashMap.put(widget.id, toAdd);
        }
    }

    public ArrayList<PseudoItem> getAllItemsForInventory(int id) {
        synchronized (items) {  // Synchronize the entire operation on the items map
            ArrayList<PseudoItem> toRet = new ArrayList<>();
            for (Map.Entry<Integer, PseudoItem> wg: items.entrySet()) {
                if (wg.getValue().getParent() == id) {
                    toRet.add(wg.getValue());
                }
            }
            return toRet;
        }
    }

    public ArrayList<PseudoItem> getAllItemsThatAreInEQ() {
        synchronized (items) {  // Synchronize the entire operation on the items map
            ArrayList<PseudoItem> toRet = new ArrayList<>();
            for (Map.Entry<Integer, PseudoItem> wg: items.entrySet()) {
                if (wg.getValue().getPositonInEquipment() != -1) {
                    toRet.add(wg.getValue());
                }
            }
            return toRet;
        }
    }

    public void removeWidgetAndChildrens(PseudoWidget widget) {
        removeWidgetAndChildrens(widget.id);
    }

    public Optional<FlowerMenuPseudoWidget> getVisibleFlowerMenu() {
        return flowerMenus.values().stream().findFirst();
    }

    public Optional<MapViewPseudoWidget> getMapView() {
        return Optional.ofNullable(mapView);
    }

    public void removeWidgetAndChildrens(int widgetId) {
        synchronized (pseudoWidgetHashMap) {
            pseudoWidgetHashMap.remove(widgetId);
            chatWidgets.remove(widgetId);
            pseudoWidgetHashMap.entrySet().removeIf(entry -> entry.getValue().parent == widgetId);
            if(instantiatedBuddy != null && widgetId == instantiatedBuddy.getId()) {
                instantiatedBuddy = null;
            }
            if (mapView != null && widgetId == mapView.getId()) {
                mapView = null;
            }
            chatWidgets.remove(widgetId);
            flowerMenus.remove(widgetId);
            items.remove(widgetId);
            inventories.remove(widgetId);
            widgetCallbacks.forEach(cb -> cb.onWidgetDestroyed(widgetId));


        }
    }

    public void widgetSetParent(int widgetID, PseudoWidget widget) {
        synchronized (pseudoWidgetHashMap) {
            PseudoWidget currentWidget = pseudoWidgetHashMap.get(widgetID);
            if(currentWidget.parent != null && currentWidget.parent != -1) {
                throw new RuntimeException("WIDGET ALREADY HAVE PARENT");
            } else {
                currentWidget.parent = widget.parent;
                currentWidget.pargs = widget.pargs;
            }
        }
    }

    public void addErrorCallback(PseudoWidgetErrorCallback callback) {
        synchronized (errorCallbacks) {
            errorCallbacks.add(callback);
        }
    }

    public void addWidgetCallback(PseudoWidgetCallback callback) {
        synchronized (widgetCallbacks) {
            widgetCallbacks.add(callback);
        }
    }

    public void removeWidgetCallback(PseudoWidgetCallback callback) {
        synchronized (widgetCallbacks) {
            widgetCallbacks.remove(callback);
        }
    }

    public ArrayList<PseudoMusicWidget> getMusicWidgets() {
        return musicWidgets;
    }
}
