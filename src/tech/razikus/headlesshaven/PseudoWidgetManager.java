package tech.razikus.headlesshaven;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PseudoWidgetManager {
    final private HashMap<Integer, PseudoWidget> pseudoWidgetHashMap = new HashMap<>();

    private BuddyPseudoWidget instantiatedBuddy;

    private HashMap<Integer, ChatPseudoWidget> chatWidgets = new HashMap<>();
    private HashMap<Integer, FlowerMenuPseudoWidget> flowerMenus = new HashMap<>();

    private ArrayList<ChatCallback> callbacks;

    private MapViewPseudoWidget mapView = null;


    // @todo move to different components, but why now
    private boolean isInVillage = false;
    private String villageName;
    private int villageID = -1;

    private long myGOBId = -1;
    private String myCharacter;


    public PseudoWidgetManager(ArrayList<ChatCallback> callbacks) {
        this.callbacks = callbacks;
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
                System.out.println("WIDGET NOT FOUND: " + message);
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

    public void addNewWidget(PseudoWidget widget) {
        System.out.println("NEW WIDGET: " + widget);
        synchronized (pseudoWidgetHashMap) {
            PseudoWidget toAdd = widget;
            if(widget.type.startsWith("ui/vlg:")) {
                String nameOfVillage = (String) widget.getCargs()[0];
                int idOf = Integer.parseInt(widget.type.substring(7));
                this.villageName = nameOfVillage;
                this.villageID = idOf;
                this.isInVillage = true;

            }
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
                case "sm":
                    FlowerMenuPseudoWidget flowerMenuPseudoWidget = new FlowerMenuPseudoWidget(widget);
                    toAdd = flowerMenuPseudoWidget;
                    flowerMenus.put(widget.id, flowerMenuPseudoWidget);
                    break;
                case "mapview":
                    mapView = new MapViewPseudoWidget(widget);
                    break;
            }
            pseudoWidgetHashMap.put(widget.id, toAdd);
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
        }
        System.out.println("REMOVED WIDGET: " + widgetId);
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

}
