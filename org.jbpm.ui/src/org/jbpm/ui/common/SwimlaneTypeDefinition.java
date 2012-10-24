package org.jbpm.ui.common;

import org.jbpm.ui.common.model.GraphElement;
import org.jbpm.ui.common.model.Swimlane;
import org.jbpm.ui.resource.Messages;

public class SwimlaneTypeDefinition implements IElementTypeDefinition {

    public String getEntryLabel() {
        return Messages.getString("default.swimlane.name");
    }

    public Class<? extends GraphElement> getModelClass() {
        return Swimlane.class;
    }

}
