package ru.runa.gpd.lang.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class EventSupport {
    private PropertyChangeSupport listeners = new PropertyChangeSupport(this);

    protected void firePropertyChange(String propName, Object old, Object newValue) {
        listeners.firePropertyChange(propName, old, newValue);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        // duplicates
        removePropertyChangeListener(listener);
        listeners.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.removePropertyChangeListener(listener);
    }

    protected void removeAllPropertyChangeListeners() {
        listeners = new PropertyChangeSupport(this);
    }



}
