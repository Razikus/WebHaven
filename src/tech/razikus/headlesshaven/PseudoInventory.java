package tech.razikus.headlesshaven;

import haven.Coord;
import haven.Coord2d;

import java.util.ArrayList;
import java.util.Arrays;

public class PseudoInventory extends  PseudoWidget{
    private Coord size;
    private String name;
    private PseudoWidgetManager manager;

    private String invType; // For special types like "study", "toolbelt"
    private Coord2d position; // For positioned inventories like toolbelt
    private boolean dropul = true;  // Drop mode: true = upper-left, false = center
    private boolean[] sqmask = null; // Mask for disabled inventory squares

    public PseudoInventory(PseudoWidget original, PseudoWidgetManager manager) {
        super(original);
        this.manager = manager;
        parseArgs(original);
    }

    public ArrayList<PseudoItem> getAllItems() {
        return this.manager.getAllItemsForInventory(this.getId());
    }


    public String getContainerNameIfExist() {
        if(this.parent != -1) {
            PseudoWidget parent = this.manager.getWidgetById(this.parent);
            if  (parent != null) {
                if(parent.getType().equals("gameui")) {
                    return this.invType;
                } else if(parent.getType().equals("wnd")) {
                    return parent.getCargs()[1].toString();
                }
            }
        }
        return this.invType;

    }

    private void parseArgs(PseudoWidget original) {
        // Parse creation args (size)
        System.out.println(original.toString());
        if (original.getCargs() != null && original.getCargs().length > 0
                && original.getCargs()[0] instanceof Coord) {
            this.size = (Coord) original.getCargs()[0];
        }

        if (original.getPargs() != null && original.getPargs().length > 0) {
            if (original.getPargs()[0] instanceof String) {
                this.invType = (String) original.getPargs()[0];
            }
            else if (original.getPargs()[0] instanceof Coord2d && original.getPargs().length >= 3) {
                this.position = (Coord2d) original.getPargs()[0];
                this.name = String.valueOf(original.getPargs()[1]);
                this.invType = String.valueOf(original.getPargs()[2]);
            }
        }
    }

    @Override
    public void ReceiveMessage(WidgetMessage message) {
        System.out.println("Inventory received message: " + message);
        switch (message.getName()) {
            case "sz": {
                if (message.getArgs().length > 0 && message.getArgs()[0] instanceof Coord) {
                    this.size = (Coord) message.getArgs()[0];
                    this.sqmask = null;
                }
                break;
            }

            case "mode": {
                if (message.getArgs().length > 0 && message.getArgs()[0] instanceof Boolean) {
                    this.dropul = !((Boolean) message.getArgs()[0]);
                }
                break;
            }

            case "mask": {
                if (message.getArgs().length > 0) {
                    if (message.getArgs()[0] == null) {
                        this.sqmask = null;
                    } else {
                        byte[] raw = (byte[]) message.getArgs()[0];
                        this.sqmask = new boolean[size.x * size.y];
                        for (int i = 0; i < size.x * size.y; i++) {
                            this.sqmask[i] = (raw[i >> 3] & (1 << (i & 7))) != 0;
                        }
                    }
                }
                break;
            }

        }
    }

    public void transferTo(int targetInvId, int amount) {
        WidgetMsg("invxf", targetInvId, amount);
    }

    public void dropItem(Coord pos) {
        WidgetMsg("drop", pos);
    }

    @Override
    public String toString() {
        return "PseudoInventory{" +
                "size=" + size +
                ", name='" + name + '\'' +
                ", manager=" + manager +
                ", invType='" + invType + '\'' +
                ", position=" + position +
                ", dropul=" + dropul +
                ", sqmask=" + Arrays.toString(sqmask) +
                ", containerName=" + getContainerNameIfExist() +
                '}';
    }
}
