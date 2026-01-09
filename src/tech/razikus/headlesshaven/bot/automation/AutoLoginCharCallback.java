package tech.razikus.headlesshaven.bot.automation;

import tech.razikus.headlesshaven.PseudoWidget;
import tech.razikus.headlesshaven.PseudoWidgetCallback;
import tech.razikus.headlesshaven.PseudoWidgetManager;
import tech.razikus.headlesshaven.WebHavenSession;

class SelectSessionWidgetCallback extends PseudoWidgetCallback {
    private String charName;
    private WebHavenSession session;
    private OnCharLoggedIn listener;

    public SelectSessionWidgetCallback(String charName, WebHavenSession session, OnCharLoggedIn listener) {
        this.charName = charName;
        this.session = session;
        this.listener = listener;
    }

    @Override
    public void onWidgetCreated(PseudoWidget widget) {
        if(widget.getType().equals("sess")) {
            System.out.println("Selecting session for: " + charName);
            widget.WidgetMsg("res", 0);
            session.getWidgetManager().removeWidgetCallback(this);
            // cargs 0 is host, cargs 1 is port, cargs 2 is cookie
            String host = (String)widget.getCargs()[0];
            Integer port = (Integer)widget.getCargs()[1];
            String cookie = (String)widget.getCargs()[2];
            WebHavenSession newSesssion = session.teleportIntoAnotherSession(host, port, cookie);
            this.listener.onCharLoggedIn(newSesssion);
        }else if (widget.getType().equals("gameui")) {
            session.getWidgetManager().removeWidgetCallback(this);
            this.listener.onCharLoggedIn(this.session);
        }

    }

    @Override
    public void onWidgetDestroyed(int id) {
        System.out.println("Widget destroyed: " + id);
    }
}

public class AutoLoginCharCallback extends PseudoWidgetCallback {

    private String charName;
    private WebHavenSession session;
    private OnCharLoggedIn listener;


    public AutoLoginCharCallback(String charName, WebHavenSession session, OnCharLoggedIn listener) {
        this.charName = charName;
        this.session = session;
        this.listener = listener;
    }

    @Override
    public void onWidgetCreated(PseudoWidget widget) {
        System.out.println("Widget created: " + widget.getId());
        if(widget.getType().equals("charlist")) {
            System.out.println("Auto selecting character: " + charName);
            session.getWidgetManager().addWidgetCallback(new SelectSessionWidgetCallback(this.charName, this.session, this.listener));
            // execute after 1 second
            widget.WidgetMsg("play", charName);
            session.getWidgetManager().removeWidgetCallback(this);
        }
    }

    @Override
    public void onWidgetDestroyed(int id) {
        System.out.println("Widget destroyed: " + id);
    }
}
