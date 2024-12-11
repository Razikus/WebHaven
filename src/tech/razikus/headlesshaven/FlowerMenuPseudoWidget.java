package tech.razikus.headlesshaven;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;


public class FlowerMenuPseudoWidget extends PseudoWidget {
    private PseudoWidgetManager manager;
    private String[] options;
    public FlowerMenuPseudoWidget(PseudoWidgetManager manager, PseudoWidget original) {
        super(original);
        this.manager = manager;
        String[] opts = new String[original.getCargs().length];
        System.out.println("FLOWER MENU OPTIONS: " + original.getCargs().length + ": " + Arrays.toString(original.getCargs()));
        for(int i = 0; i < original.getCargs().length; i++)
            opts[i] = (String)original.getCargs()[i];
        options = opts;
    }

    public String[] getOptions() {
        return options;
    }

    public void close() {
        this.WidgetMsg("cl", -1);
        this.manager.removeWidgetAndChildrens(this.getId());
    }

    public void click(int index) {
        // handle shifts etc @todo
        int modflags = 0;
        this.WidgetMsg("cl", index, modflags);
        this.manager.removeWidgetAndChildrens(this.getId());
    }

    @Override
    public void ReceiveMessage(WidgetMessage message) {
    }
}

class FlowerMenuPseudoWidgetSerializer implements JsonSerializer<FlowerMenuPseudoWidget> {

    @Override
    public JsonElement serialize(FlowerMenuPseudoWidget src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", src.getId());
        obj.addProperty("type", src.getType());
        obj.addProperty("parent", src.getParent());
        obj.add("options", context.serialize(src.getOptions()));
        return obj;
    }
}
