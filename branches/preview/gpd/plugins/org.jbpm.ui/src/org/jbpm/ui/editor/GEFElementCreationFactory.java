package ru.runa.bpm.ui.editor;

import org.eclipse.gef.requests.CreationFactory;
import ru.runa.bpm.ui.JpdlVersionRegistry;
import ru.runa.bpm.ui.common.model.GraphElement;
import ru.runa.bpm.ui.common.model.ProcessDefinition;

public class GEFElementCreationFactory implements CreationFactory {
    private final String elementType;

    private final ProcessDefinition definition;

    public GEFElementCreationFactory(String elementType, ProcessDefinition definition) {
        this.elementType = elementType;
        this.definition = definition;
    }

    public Object getNewObject() {
        GraphElement element = JpdlVersionRegistry.getElementTypeDefinition(definition.getJpdlVersion(), elementType).createElement();
        element.setParent(definition);
        element.postCreate();
        return element;
    }

    public Object getNewObject(GraphElement parent) {
        GraphElement element = JpdlVersionRegistry.getElementTypeDefinition(definition.getJpdlVersion(), elementType).createElement();
        element.setParent(parent);
        element.postCreate();
        return element;
    }

    public Object getObjectType() {
        return elementType;
    }

}
