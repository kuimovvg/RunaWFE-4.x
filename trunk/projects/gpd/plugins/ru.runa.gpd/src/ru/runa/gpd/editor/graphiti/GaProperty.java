package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.mm.impl.PropertyImpl;

public class GaProperty extends PropertyImpl {
    public GaProperty(String name, String value) {
        setKey(name);
        setValue(value);
    }
}
