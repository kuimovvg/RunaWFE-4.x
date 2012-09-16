package ru.runa.bpm.ui.common.model;

public abstract class NamedGraphElement extends GraphElement {

    private String name;

    public NamedGraphElement() {
    }

    protected NamedGraphElement(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        String old = this.getName();
        if (name != null && canSetNameTo(name)) {
            this.name = name;
            firePropertyChange(PROPERTY_NAME, old, this.getName());
        }
    }

    protected abstract boolean canSetNameTo(String name);

    protected boolean canNameBeSetFromProperties() {
        return true;
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_NAME.equals(id)) {
            return safeStringValue(name);
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_NAME.equals(id)) {
            setName((String) value);
        } else {
            super.setPropertyValue(id, value);
        }
    }

    @Override
    public String toString() {
        return name;
    }

}
