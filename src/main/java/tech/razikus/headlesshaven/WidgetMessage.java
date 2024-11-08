package tech.razikus.headlesshaven;

import java.util.Arrays;
import java.util.Objects;

public class WidgetMessage {
    private int widgetID;
    private String name;
    private Object[] args = new Object[]{};

    public WidgetMessage(int widgetID, String name, Object[] args) {
        this.widgetID = widgetID;
        this.name = name;
        this.args = args;
    }

    public int getWidgetID() {
        return widgetID;
    }

    public String getName() {
        return name;
    }

    public Object[] getArgs() {
        return args;
    }

    @Override
    public String toString() {
        return "WidgetMessage{" +
                "widgetID=" + widgetID +
                ", name='" + name + '\'' +
                ", args=" + Arrays.toString(args) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WidgetMessage that = (WidgetMessage) o;
        return widgetID == that.widgetID && Objects.equals(name, that.name) && Objects.deepEquals(args, that.args);
    }

    @Override
    public int hashCode() {
        return Objects.hash(widgetID, name, Arrays.hashCode(args));
    }
}
