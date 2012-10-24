package org.jbpm.ui.editor;

import org.eclipse.gef.requests.CreationFactory;
import org.jbpm.ui.JpdlVersionRegistry;
import org.jbpm.ui.common.model.GraphElement;
import org.jbpm.ui.common.model.ProcessDefinition;

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
