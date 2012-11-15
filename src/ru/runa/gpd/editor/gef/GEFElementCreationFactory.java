package ru.runa.gpd.editor.gef;

import org.eclipse.gef.requests.CreationFactory;

import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;

public class GEFElementCreationFactory implements CreationFactory {
    private final String elementType;
    private final ProcessDefinition definition;

    public GEFElementCreationFactory(String elementType, ProcessDefinition definition) {
        this.elementType = elementType;
        this.definition = definition;
    }

    @Override
    public Object getNewObject() {
        GraphElement element = NodeRegistry.getNodeTypeDefinition(elementType).createElement();
        element.setParent(definition);
        element.postCreate();
        return element;
    }

    public Object getNewObject(GraphElement parent) {
        GraphElement element = NodeRegistry.getNodeTypeDefinition(elementType).createElement();
        element.setParent(parent);
        element.postCreate();
        return element;
    }

    @Override
    public Object getObjectType() {
        return elementType;
    }
}
