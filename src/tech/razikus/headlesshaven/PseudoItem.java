package tech.razikus.headlesshaven;

import haven.Coord;

import java.util.ArrayList;

public class PseudoItem extends PseudoWidget {
    private int resourceId = -1;
    private int meter = 0;  // For quality/durability
    private int num = -1;   // For stack size
    private Object[] rawTooltip = null;  // Store raw tooltip data
    private ArrayList<PseudoItemInfo> itemInfo = new ArrayList<>();
    private Coord position = null;
    private int positonInEquipment = -1;

    private ResourceManager resourceManager;
    private ResourceInformationLazyProxy resProxy = null;

    public PseudoItem(PseudoWidget widget, ResourceManager resourceManager) {
        super(widget);
        this.resourceManager = resourceManager;
        if (widget.getCargs() != null && widget.getCargs().length > 0) {
            this.resourceId = ((Number) widget.getCargs()[0]).intValue();
            this.resProxy = new ResourceInformationLazyProxy(resourceManager, this.resourceId, null);
        }
        if (widget.getPargs() != null && widget.getPargs().length > 0) {
            if (widget.getPargs()[0] instanceof Coord) {
                this.position = ((Coord) widget.getPargs()[0]);
            } else if (widget.getPargs()[0] instanceof Integer) {
                this.positonInEquipment = ((Number) widget.getPargs()[0]).intValue();
            }

        }
    }
    public void parseItemInfos() {
        itemInfo.clear();
        if(rawTooltip != null) {
            for(Object obj : rawTooltip) {
                if(obj instanceof Object[]) {
                    itemInfo.add(new PseudoItemInfo(resourceManager, (Object[])obj));
                }
            }
        }

        System.out.println(this);

    }

    public ResourceInformationLazyProxy getResProxy() {
        return resProxy;
    }

    public int getPositonInEquipment() {
        return positonInEquipment;
    }

    @Override
    public void ReceiveMessage(WidgetMessage message) {
        switch (message.getName()) {
            case "tt": {
                // Store raw tooltip data for later processing
                this.rawTooltip = message.getArgs();
                this.parseItemInfos();
                break;
            }
            case "meter": {
                if (message.getArgs().length > 0) {
                    this.meter = ((Number) message.getArgs()[0]).intValue();
                }
                break;
            }
            case "num": {
                if (message.getArgs().length > 0) {
                    this.num = ((Number) message.getArgs()[0]).intValue();
                }
                break;
            }
        }
    }

    public void take() {
        this.WidgetMsg("take");
    }

    public void transfer(int targetInventoryId, Coord targetPos) {
        this.WidgetMsg("transfer", targetInventoryId, targetPos);
    }

    public void drop() {
        this.WidgetMsg("drop");
    }

    public void iact() {  // Interact with item
//        item.wdgmsg("iact", ev.c, ui.modflags());
        Coord cc = new Coord(0, 0);
        this.WidgetMsg("iact", cc, 0);
    }



    public int getResourceId() {
        return resourceId;
    }

    public int getMeter() {
        return meter;
    }

    public int getNum() {
        return num;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public void setMeter(int meter) {
        this.meter = meter;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public Object[] getRawTooltip() {
        return rawTooltip;
    }

    public void setRawTooltip(Object[] rawTooltip) {
        this.rawTooltip = rawTooltip;
    }

    @Override
    public String toString() {
        return "PseudoItem{" +
                "num=" + num +
                ", resourceId=" + resourceId +
                ", resProxy=" + resProxy +
                ", meter=" + meter +
                ", itemInfo=" + itemInfo +
                ", position=" + position +
                ", positonInEquipment=" + positonInEquipment +
                '}';
    }
}


class PseudoItemInfo {
    private String type;
    private final Object[] data;
    private ResourceManager resourceManager;
    private int resourceId = -1;
    private ResourceInformationLazyProxy resourceProxy;

    public PseudoItemInfo(ResourceManager manager, Object[] info) {
        this.resourceManager = manager;
        if (info == null || info.length == 0) {
            throw new IllegalArgumentException("Info array cannot be null or empty");
        }


        if (info[0] instanceof Object[]) {
            // Handle type array format: [typeArr, data1, data2, ...]
            Object[] typeArr = (Object[])info[0];

            // Get type from first element of type array
            this.type = typeArr.length > 0 ? String.valueOf(typeArr[0]) : "unknown";

            // Get resource ID and create proxy
            this.resourceId = (typeArr.length > 1) ? ((Number)typeArr[1]).intValue() : -1;
            this.resourceProxy = (this.resourceId != -1) ?
                    new ResourceInformationLazyProxy(resourceManager, this.resourceId, null) :
                    null;

            // Copy remaining data, excluding the type array
            this.data = new Object[info.length - 1];
            if (info.length > 1) {
                System.arraycopy(info, 1, this.data, 0, info.length - 1);
            }
        } else {
            // Handle direct resource format: [resid, data1, data2, ...]
            try {
                this.resourceId = ((Number)info[0]).intValue();
                this.resourceProxy = new ResourceInformationLazyProxy(resourceManager, this.resourceId, null);
                this.type = "direct"; // We'll get the actual type lazily through the proxy when needed
            } catch (ClassCastException e) {
                this.resourceId = -1;
                this.resourceProxy = null;
                this.type = info[0].toString();
            }

            // Store all data including the first element
            this.data = info.clone();
        }
    }

    // Helper methods for common tooltip types
    public boolean isQuality() {
        return resourceProxy != null &&
                resourceProxy.getResource() != null &&
                "ui/tt/q/quality".equals(resourceProxy.getResource().getInformation().getName());
    }

    public boolean isContainer() {
        return resourceProxy != null &&
                resourceProxy.getResource() != null &&
                "ui/tt/cont".equals(resourceProxy.getResource().getInformation().getName());
    }

    public boolean isLevel() {
        return resourceProxy != null &&
                resourceProxy.getResource() != null &&
                "ui/tt/level".equals(resourceProxy.getResource().getInformation().getName());
    }

    public boolean isName() {
        return resourceProxy != null &&
                resourceProxy.getResource() != null &&
                "ui/tt/name".equals(resourceProxy.getResource().getInformation().getName());
    }

    public boolean isAmount() {
        return resourceProxy != null &&
                resourceProxy.getResource() != null &&
                "ui/tt/amount".equals(resourceProxy.getResource().getInformation().getName());
    }

    public boolean isStackable() {
        return resourceProxy != null &&
                resourceProxy.getResource() != null &&
                "ui/tt/stackn".equals(resourceProxy.getResource().getInformation().getName());
    }

    public boolean isWeaponDamage() {
        return resourceProxy != null &&
                resourceProxy.getResource() != null &&
                "ui/tt/wpn/dmg".equals(resourceProxy.getResource().getInformation().getName());
    }

    // Value getters
    public float getQuality() {
        if (isQuality() && data.length > 1 && data[1] instanceof Number) {
            return ((Number)data[1]).floatValue();
        }
        return -1f;
    }

    public int getAmount() {
        if (isAmount() && data.length > 1 && data[1] instanceof Number) {
            return ((Number)data[1]).intValue();
        }
        return -1;
    }

    public String getName() {
        if (isName() && data.length > 1) {
            return String.valueOf(data[1]);
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PseudoItemInfo{");

        // Basic info
        sb.append("type='").append(type).append('\'');
        sb.append(", resourceId=").append(resourceId);

        // Resource information via proxy
        if (resourceProxy != null) {
            InstantiatedResourceInformation resource = resourceProxy.getResource();
            if (resource != null) {
                sb.append(", resource=").append(resource);
            }
        }

        // Data array contents
        sb.append(", data=").append(formatData(data));

        // Add specific values based on type
        if (isQuality()) {
            sb.append(", quality=").append(getQuality());
        }
        if (isAmount()) {
            sb.append(", amount=").append(getAmount());
        }
        if (isName()) {
            sb.append(", name=").append(getName());
        }
        if (isContainer()) {
            sb.append(", contents=").append(formatContainerContents());
        }
        if (isLevel()) {
            sb.append(", level=").append(formatLevelInfo());
        }

        sb.append('}');
        return sb.toString();
    }

    private String formatData(Object[] data) {
        if (data == null) return "null";
        StringBuilder buf = new StringBuilder();
        buf.append("[");
        for (int i = 0; i < data.length; i++) {
            if (i > 0) buf.append(", ");
            buf.append(formatData(data[i]));
        }
        buf.append("]");
        return buf.toString();
    }

    private String formatData(Object arg) {
        if (arg instanceof Object[]) {
            return formatData((Object[])arg);
        } else {
            return String.valueOf(arg);
        }
    }

    private String formatContainerContents() {
        if (!isContainer() || data.length < 2 || !(data[1] instanceof Object[])) {
            return "invalid";
        }
        Object[] contents = (Object[])data[1];
        return formatData(contents);
    }

    private String formatLevelInfo() {
        if (!isLevel() || data.length < 4) {
            return "invalid";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("max=").append(data[1]);
        sb.append(", current=").append(data[2]);
        if (data[3] instanceof java.awt.Color) {
            sb.append(", color=").append(data[3]);
        }
        return sb.toString();
    }

    // Getters
    public String getType() {
        return type;
    }

    public Object[] getData() {
        return data;
    }

    public int getResourceID() {
        return resourceId;
    }

    public ResourceInformationLazyProxy getResourceProxy() {
        return resourceProxy;
    }

}