package tech.razikus.headlesshaven;

public class BuddyStateProxy {
    private int buddyInteger;
    private PseudoWidgetManager widgetManager;

    public BuddyStateProxy(int buddyInteger, PseudoWidgetManager widgetManager) {
        this.buddyInteger = buddyInteger;
        this.widgetManager = widgetManager;
    }

    public BuddyState getBuddyState() {
        if (widgetManager == null || widgetManager.getInstantiatedBuddy() == null) {
            return null;
        }
        return widgetManager.getInstantiatedBuddy().getBuddy(buddyInteger);
    }
}
