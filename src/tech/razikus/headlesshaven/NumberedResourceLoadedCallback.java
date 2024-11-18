package tech.razikus.headlesshaven;

import haven.Resource;

public abstract class NumberedResourceLoadedCallback {
    public abstract void onFullResourceLoaded(int id, Resource info);

}
