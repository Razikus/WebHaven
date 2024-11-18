package tech.razikus.headlesshaven;

public abstract class ObjectChangeCallback {
    public abstract void objectRemoved(long id);

    public abstract void objectAdded(PseudoObject obj);

    public abstract void objectChanged(PseudoObject obj);
}
