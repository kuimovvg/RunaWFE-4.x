package org.jbpm.ui.common;

import org.jbpm.ui.common.model.GraphElement;

public interface IElementTypeDefinition {

    String getEntryLabel();

    Class<? extends GraphElement> getModelClass();
}
