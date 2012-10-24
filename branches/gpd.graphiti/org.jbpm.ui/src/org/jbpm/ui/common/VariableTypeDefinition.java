package org.jbpm.ui.common;

import org.jbpm.ui.common.model.GraphElement;
import org.jbpm.ui.common.model.Variable;
import org.jbpm.ui.resource.Messages;

public class VariableTypeDefinition implements IElementTypeDefinition {

    public String getEntryLabel() {
        return Messages.getString("default.variable.name");
    }

    public Class<? extends GraphElement> getModelClass() {
        return Variable.class;
    }

}
