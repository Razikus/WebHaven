package tech.razikus.headlesshaven;

public abstract class PseudoWidgetCallback {

    public abstract void onWidgetCreated(PseudoWidget widget);
    public abstract void onWidgetDestroyed(int id);
}
