package tech.razikus.headlesshaven;

import java.util.Arrays;
import java.util.Objects;

public class PseudoWidget {
    PlayerHandler playerHandler;
    int id;
    String type;
    Integer parent;
    Object[] pargs;
    Object[] cargs;

    public PseudoWidget(PseudoWidget original) {
        this.playerHandler = original.playerHandler;
        this.id = original.id;
        this.type = original.type;
        this.parent = original.parent;
        this.pargs = original.pargs;
        this.cargs = original.cargs;
    }


    public PseudoWidget(PlayerHandler handler, int id, String type, int parent, Object[] pargs, Object[] cargs) {
        this.playerHandler = handler;
        this.id = id;
        this.type = type;
        this.parent = parent;
        this.pargs = pargs; // place args? parent args? sometimes show the place where the widget should be added ex. GameUi.java addchild(
        this.cargs = cargs; // child args - used to construct widget, example - meter have in cargs [897] - which is resource id
    }

    public void WidgetMsg(String msg, Object... args) {
        this.playerHandler.sendMessageFromWidget(this.id, msg, args);
    }

    public void ReceiveMessage(WidgetMessage message) {
//        System.out.println("UNHANDLED WIDGET: " + this.getType() + " RECEIVED MESSAGE: " + this + ": " + message );

    }

    public PseudoWidget(int id, int parent, Object[] pargs) {
        this.id = id;
        this.parent = parent;
        this.pargs = pargs;
        this.cargs = new Object[]{};
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getParent() {
        return parent;
    }

    public void setParent(Integer parent) {
        this.parent = parent;
    }

    public Object[] getPargs() {
        return pargs;
    }

    public void setPargs(Object[] pargs) {
        this.pargs = pargs;
    }

    public Object[] getCargs() {
        return cargs;
    }

    public void setCargs(Object[] cargs) {
        this.cargs = cargs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PseudoWidget that = (PseudoWidget) o;
        return id == that.id && parent == that.parent && Objects.equals(type, that.type) && Objects.deepEquals(pargs, that.pargs) && Objects.deepEquals(cargs, that.cargs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, parent, Arrays.hashCode(pargs), Arrays.hashCode(cargs));
    }

    @Override
    public String toString() {
        return "PseudoWidget{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", parent=" + parent +
                ", pargs=" + Arrays.toString(pargs) +
                ", cargs=" + Arrays.toString(cargs) +
                '}';
    }
}