package tech.razikus.headlesshaven;

import com.google.gson.*;
import haven.Coord2d;
import haven.Message;
import haven.MessageBuf;
import haven.OCache;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static haven.OCache.posres;


public class PseudoObject {
    private ResourceManager manager;
    private PseudoWidgetManager widgetManager;
    private long id;
    private ArrayList<ResourceInformationLazyProxy> resourceInformationLazyProxies = new ArrayList<>();
    private BuddyStateProxy buddyStateProxy;

    private HashMap<Integer, ResourceInformationLazyProxy> overlays = new HashMap<>();
    private Coord2d coordinate;
    private double angle; // a.k.a rotation
    private float health = -1.0f;
    private List<CompositeModification> compositeModifications = new ArrayList<>();
    private CompositePoseState poseState = new CompositePoseState();


    private ResourceClickableInfo clickableInfo;

    private void loadClickableInfo(String resName) {
        manager.getResourceInfo(resName)
                .thenAccept(info -> {
                    if(resName.contains("timberh")) {
                        System.out.println(info);

                    }
                    this.clickableInfo = info;
                })
                .exceptionally(throwable -> {
                    System.err.println("Failed to load clickable info for " + resName + ": " + throwable.getMessage());
                    return null;
                });
    }

    public PseudoObject(ResourceManager manager, PseudoWidgetManager widgetManager, long id) {
        this.id = id;
        this.widgetManager = widgetManager;
        this.manager = manager;
    }

    public PseudoWidgetManager getWidgetManager() {
        return widgetManager;
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

    public void fillDelta(OCache.ObjDelta delta) {
        if(delta.rem) {
            throw new RuntimeException("Never fill with object marked to remove");
        }
        fillNew(delta); // probably this is enough?

//        for (OCache.AttrDelta attr: delta.attrs) {
//            switch (attr.type) {
//                case OCache.OD_MOVE:
//                    handleMove(delta, attr);
//                    break;
//                default:
////                    System.out.println("NOT SUPPORTED YET: " + MessageParser.getODName(attr.type));
//                    break;
//            }
//        }


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
                case OCache.OD_MOVE:
                    handleMove(delta, attr);
                    break;
                case OCache.OD_OVERLAY:
                    handleOverlay(delta, attr);
//                    new Thread(() -> {
//                        try {
//                            Thread.sleep(3000);
//                            System.out.println(this.overlays);
//                        } catch (InterruptedException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }).start();
                    break;
                case OCache.OD_HEALTH:
                    health = attr.uint8() / 4.0f;
                    break;
                case OCache.OD_CMPMOD:
                    handleCompositeMod(delta, attr);
                    break;
                case OCache.OD_CMPPOSE:
                    handleComposePose(delta, attr);
                    break;
                default:
                    // not supported OD_LINBEG
                    // not supported OD_LINSTEP
//                    System.out.println("NOT SUPPORTED YET: " + MessageParser.getODName(attr.type));
                    break;
            }
        }
    }

    private void handleOverlay(OCache.ObjDelta delta, OCache.AttrDelta attr) {
        int olidf = attr.int32();
        boolean prs = (olidf & 1) != 0;
        int olid = olidf >>> 1;

        int resid = attr.uint16();
        Message sdt = Message.nil;

        if(resid == 65535) {
            overlays.remove(olid);
        } else {
            if((resid & 0x8000) != 0) {
                resid &= ~0x8000;
                sdt = new MessageBuf(attr.bytes(attr.uint8()));
            }
            ResourceInformationLazyProxy proxy =
                    new ResourceInformationLazyProxy(manager, resid, sdt);
            overlays.put(olid, proxy);
        }
    }

    private void handleMove(OCache.ObjDelta delta, OCache.AttrDelta attr) {
        Coord2d c = attr.coord().mul(posres);
        double a = (attr.uint16() / 65536.0) * Math.PI * 2;
        this.coordinate = c;
        this.angle = a;
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
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                String name = proxy.getResource().getInformation().getName();
                loadClickableInfo(name);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
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
        return "PseudoObject{" +
                "id=" + id +
                ", resourceInformationLazyProxies=" + resourceInformationLazyProxies +
                ", buddyStateProxy=" + buddyStateProxy +
                ", overlays=" + overlays +
                ", coordinate=" + coordinate +
                ", angle=" + angle +
                ", compositeModifications=" + compositeModifications +
                ", health=" + health +
                ", poseState=" + poseState +
                '}';
    }

    public Coord2d getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coord2d coordinate) {
        this.coordinate = coordinate;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    private void handleCompositeMod(OCache.ObjDelta delta, OCache.AttrDelta attr) {
        List<CompositeModification> modifications = new ArrayList<>();
        int mseq = 0;

        while (true) {
            int modid = attr.uint16();
            if (modid == 65535) break;  // End of modifiers

            List<ResourceInformationLazyProxy> resources = new ArrayList<>();

            // Read texture/resource entries for this modifier
            while (true) {
                int resid = attr.uint16();
                if (resid == 65535) break;  // End of resources

                Message sdt = Message.nil;
                if ((resid & 0x8000) != 0) {
                    resid &= ~0x8000;
                    sdt = new MessageBuf(attr.bytes(attr.uint8()));
                }

                ResourceInformationLazyProxy proxy =
                        new ResourceInformationLazyProxy(manager, resid, sdt);
                resources.add(proxy);
            }

            modifications.add(new CompositeModification(modid, resources, mseq++));
        }

        this.compositeModifications = modifications;
    }


    private List<ResourceInformationLazyProxy> readPoseList(OCache.AttrDelta msg) {
        List<ResourceInformationLazyProxy> poses = new ArrayList<>();
        while (true) {
            int resid = msg.uint16();
            if (resid == 65535) break;  // End marker

            Message sdt = Message.nil;
            if ((resid & 0x8000) != 0) {
                resid &= ~0x8000;
                sdt = new MessageBuf(msg.bytes(msg.uint8()));
            }

            poses.add(new ResourceInformationLazyProxy(manager, resid, sdt));
        }
        return poses;
    }

    private void handleComposePose(OCache.ObjDelta delta, OCache.AttrDelta msg) {
        int pfl = msg.uint8();
        int pseq = msg.uint8();
        boolean interp = (pfl & 1) != 0;

        // Only process if sequence number is different
        if (poseState.getPoseSequence() == pseq) {
            return;
        }

        poseState.setPoseSequence(pseq);

        // Handle main poses
        if ((pfl & 2) != 0) {
            List<ResourceInformationLazyProxy> poses = readPoseList(msg);
            poseState.setMainPoses(new PoseData(poses, interp, 0));
        }

        // Handle transition poses
        if ((pfl & 4) != 0) {
            List<ResourceInformationLazyProxy> tposes = readPoseList(msg);
            float ttime = msg.uint8() / 10.0f;  // Convert to seconds
            poseState.setTransitionPoses(new PoseData(tposes, interp, ttime));
        }
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

        json.addProperty("coordsX", src.getCoordinate().x);
        json.addProperty("coordsY", src.getCoordinate().y);
        json.addProperty("angle", src.getAngle());

        json.addProperty("isMyself", src.getId() == src.getWidgetManager().getMyGOBId());

        return json;
    }


}