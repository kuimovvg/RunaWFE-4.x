package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.mm.impl.PropertyImpl;

public class GaProperty extends PropertyImpl {
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String SWIMLANE_NAME = "swimlaneName";
    public static final String EXCLUSIVE_FLOW = "exclusiveFlow";
    public static final String DEFAULT_FLOW = "defaultFlow";
    public static final String SUBPROCESS = "subProcess";
    public static final String MULTIPROCESS = "multiProcess";

    public GaProperty(String name, String value) {
        setKey(name);
        setValue(value);
    }
}
