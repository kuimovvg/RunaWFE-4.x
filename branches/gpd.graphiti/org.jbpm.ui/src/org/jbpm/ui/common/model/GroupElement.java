package org.jbpm.ui.common.model;

import org.jbpm.ui.common.IElementTypeDefinition;

public class GroupElement extends GraphElement {
    private final IElementTypeDefinition typeDefinition;

    public GroupElement(ProcessDefinition definition, IElementTypeDefinition typeDefinition) {
        setParent(definition);
        this.typeDefinition = typeDefinition;
    }

    public IElementTypeDefinition getType() {
        return typeDefinition;
    }

}
