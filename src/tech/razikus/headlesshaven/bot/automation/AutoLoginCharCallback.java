package tech.razikus.headlesshaven.bot.automation;

import tech.razikus.headlesshaven.PseudoWidget;
import tech.razikus.headlesshaven.PseudoWidgetCallback;
import tech.razikus.headlesshaven.PseudoWidgetManager;
import tech.razikus.headlesshaven.WebHavenSession;

public class AutoLoginCharCallback extends PseudoWidgetCallback {

    private String charName;
    private WebHavenSession session;


    public AutoLoginCharCallback(String charName, WebHavenSession session) {
        this.charName = charName;
        this.session = session;
    }

    @Override
    public void onWidgetCreated(PseudoWidget widget) {
        System.out.println("Widget created: " + widget.getId());
        if(widget.getType().equals("charlist")) {
            widget.WidgetMsg("play", charName);
            session.getWidgetManager().removeWidgetCallback(this);
        }
    }

    @Override
    public void onWidgetDestroyed(int id) {
        System.out.println("Widget destroyed: " + id);
    }
}
