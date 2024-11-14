package tech.razikus.headlesshaven;

import haven.Coord;

import java.util.Arrays;

public class MapViewPseudoWidget extends PseudoWidget{
    public MapViewPseudoWidget(PseudoWidget original) {
        super(original);
    }

    public void mapClick(Coord pixelCord, Coord clickCoord, int button, int modifiers) {
        Object[] args = new Object[] {
                pixelCord, clickCoord, button, modifiers
        };
        this.WidgetMsg("click", args);
    }

    public void gobClick(Coord pixelCord, Coord clickCoord, int button, int modifiers, boolean isOverlay, long gobId, Coord gobCoordPosres, int overlayId, int meshid) {
        int overlay = 0;
        int overlayIdToSend = 0;
        if(isOverlay) {
            overlay = 1;
            overlayIdToSend = overlayId;
        }
        Object[] args = new Object[] {
                pixelCord,                 // screen coordinate
                clickCoord,        // clicked map coordinate
                button,            // button pressed
                modifiers,         // modifier keys
                overlay, // clickargs of GobClick extends Clickabl in Gob.java
                gobId,              // clicked gob ID
                gobCoordPosres,
                overlayIdToSend,
                meshid
        };
        System.out.println("GobClick: " + Arrays.toString(args));
        this.WidgetMsg("click", args);
    }

//                click: [(733, 407), (-965701, -975892), 3, 0, 0, 1648077781, (-965632, -978944), 0, 16] in
    //               click: [(725, 615), (-970284, -972670), 3, 0, 0, 1868781591, (-969728, -972800), 0, -1] out
                /* ORIGINICAL CLICKARGS:
                public Object[] clickargs(ClickData cd) {
                    Object[] ret = {0, (int)gob.id, gob.rc.floor(OCache.posres), 0, -1};
                    for(Object node : cd.array()) {
                    if(node instanceof Gob.Overlay) {
                        ret[0] = 1;
                        ret[3] = ((Gob.Overlay)node).id;
                    }
                    if(node instanceof FastMesh.ResourceMesh)
                        ret[4] = ((FastMesh.ResourceMesh)node).id;
                    }
                    return(ret);
                }
                 */


    @Override
    public void ReceiveMessage(WidgetMessage message) {
        System.out.println("MAPVIEW UNHANDLED MESSAGE: " + message);
    }
}


