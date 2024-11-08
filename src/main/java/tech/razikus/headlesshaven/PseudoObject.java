package tech.razikus.headlesshaven;

import com.google.gson.*;

import haven.Message;
import haven.MessageBuf;
import haven.OCache;

import java.lang.reflect.Type;
import java.util.*;

class InstantiatedResourceInformation {
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

public class PseudoObject {
    private ResourceManager manager;
    private PseudoWidgetManager widgetManager;
    private long id;
    private ArrayList<ResourceInformationLazyProxy> resourceInformationLazyProxies = new ArrayList<>();
    private BuddyStateProxy buddyStateProxy;

    public PseudoObject(ResourceManager manager, PseudoWidgetManager widgetManager, long id) {
        this.id = id;
        this.widgetManager = widgetManager;
        this.manager = manager;
    }

    public boolean isBuddyRes() {
        boolean found = false;
        for (ResourceInformationLazyProxy proxy: resourceInformationLazyProxies) {
            ResourceInformation info = proxy.getResource().getInformation();
            if(info.getName() != null && info.getName().equals("ui/obj/buddy")) {
                found = true;
                break;
            }
        }
        return found;
    }

    public boolean isVillageBuddy() {
        boolean found = false;
        for (ResourceInformationLazyProxy proxy: resourceInformationLazyProxies) {
            ResourceInformation info = proxy.getResource().getInformation();
            if(info != null && info.getName() != null && info.getName().equals("ui/obj/buddy-v")) {
                found = true;
                break;
            }
        }
        return found;
    }

    public boolean isProbablyPlayer() {
        if(id == widgetManager.getMyGOBId()) {
            return false;
        }
        if(isVillageBuddy()) {
            return true;
        }
        for (ResourceInformationLazyProxy proxy: resourceInformationLazyProxies) {
            ResourceInformation info = proxy.getResource().getInformation();
            if(info != null && info.getName() != null && info.getName().equals("gfx/borka/body")) {
                return true;
            }
        }
        return false;
    }

    public BuddyStateProxy getBuddyState() {
        if(this.buddyStateProxy != null) {
            return this.buddyStateProxy;
        } else {
            for (ResourceInformationLazyProxy proxy: resourceInformationLazyProxies) {
                ResourceInformation info = proxy.getResource().getInformation();
                if(info.getName() != null && info.getName().equals("ui/obj/buddy")) {
                    Message resData = proxy.getResData();
                    if(resData != null) {
                        int n = resData.uint8();
                        int idOf = resData.int32();

                        this.buddyStateProxy = new BuddyStateProxy(idOf, widgetManager);
                        return this.buddyStateProxy;
                    }

                    break;
                }
            }
        }
        return null;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ArrayList<ResourceInformationLazyProxy> getResourceInformationLazyProxies() {
        return resourceInformationLazyProxies;
    }

    public void fillNew(OCache.ObjDelta delta) {
//        MessageParser.parseObjDelta(delta);
        if(delta.rem) {
            throw new RuntimeException("Never fill with object marked to remove");
        }
        for (OCache.AttrDelta attr: delta.attrs) {
            switch (attr.type) {
                case OCache.OD_RES:
                    handleRes(delta, attr);
                    break;
                case OCache.OD_RESATTR:
                    handleResAttr(delta, attr);
                    break;
                case OCache.OD_ICON:
                    handleIcon(delta, attr);
                    break;
                case OCache.OD_COMPOSE:
                    handleOdCompose(delta, attr);
                    break;
                default:
//                    System.out.println("NOT SUPPORTED YET: " + MessageParser.getODName(attr.type));
                    break;
            }
        }
    }

    private void handleIcon(OCache.ObjDelta fulldelta, OCache.AttrDelta attr) {

        int resid = attr.uint16();
        if(resid == 65535) {
//            g.delattr(GobIcon.class);
        } else {
            int ifl = attr.uint8();
            byte[] sdt = attr.bytes();
            Message sdt2 = new MessageBuf(sdt);
            ResourceInformationLazyProxy proxy = new ResourceInformationLazyProxy(manager, resid, sdt2);
            resourceInformationLazyProxies.add(proxy);

        }
    }

    private void handleRes(OCache.ObjDelta fulldelta, OCache.AttrDelta attr) {
        int resid = attr.uint16();
        MessageBuf sdt = null;
        if((resid & 0x8000) != 0) {
            resid &= ~0x8000;
            sdt = new MessageBuf(attr.bytes(attr.uint8()));
        }
        ResourceInformationLazyProxy proxy = new ResourceInformationLazyProxy(manager, resid, sdt);
        resourceInformationLazyProxies.add(proxy);
    }

    private void handleOdCompose(OCache.ObjDelta fulldelta, OCache.AttrDelta attr) {
        int resid = attr.uint16();
        ResourceInformationLazyProxy proxy = new ResourceInformationLazyProxy(manager, resid, null);
        resourceInformationLazyProxies.add(proxy);
    }

    private void handleResAttr(OCache.ObjDelta fulldelta, OCache.AttrDelta attr) {
        int resId = attr.uint16();
        int len = attr.uint8();
        Message dat = (len > 0) ? new MessageBuf(attr.bytes(len)) : null;
        ResourceInformationLazyProxy proxy = new ResourceInformationLazyProxy(manager, resId, dat);
        resourceInformationLazyProxies.add(proxy);
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ID=");
        builder.append(id);
        builder.append("\n");
        if(resourceInformationLazyProxies.size() > 0) {
            for (ResourceInformationLazyProxy proxy: resourceInformationLazyProxies) {
                builder.append(proxy.getResource().toString());
                builder.append("\n");
            }
        }

        if(isBuddyRes()) {
            builder.append("ISBUDDY=");
            builder.append(true);
            builder.append("\n");
            builder.append(getBuddyState().getBuddyState());
            builder.append("\n");
        }
        if (isVillageBuddy()) {
            builder.append("ISVILLAGE=");
            builder.append(true);
            builder.append("\n");
        }

        return builder.toString();
    }
}


class PseudoObjectSerializer implements JsonSerializer<PseudoObject> {
    @Override
    public JsonElement serialize(PseudoObject src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();

        // Serialize basic fields
        json.addProperty("id", src.getId());

        // Handle resourceInformationLazyProxies
        JsonArray resources = new JsonArray();
        for (ResourceInformationLazyProxy proxy : src.getResourceInformationLazyProxies()) {
            // Get the actual resource data
            InstantiatedResourceInformation resource = proxy.getResource();
            resources.add(context.serialize(resource));
        }
        json.add("resources", resources);

        // Handle buddy state
        if (src.getBuddyState() != null && src.getBuddyState().getBuddyState() != null) {
            BuddyState buddyState = src.getBuddyState().getBuddyState();
            if (buddyState != null) {
                json.add("buddyState", context.serialize(buddyState));
            }
        }

        json.addProperty("isBuddy", src.isBuddyRes());
        json.addProperty("isVillageBuddy", src.isVillageBuddy());

        return json;
    }
}