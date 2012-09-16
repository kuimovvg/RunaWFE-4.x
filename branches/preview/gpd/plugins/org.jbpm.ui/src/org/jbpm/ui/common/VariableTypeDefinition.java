package ru.runa.bpm.ui.common;

import ru.runa.bpm.ui.common.model.GraphElement;
import ru.runa.bpm.ui.common.model.Variable;
import ru.runa.bpm.ui.resource.Messages;

public class VariableTypeDefinition implements IElementTypeDefinition {

    public String getEntryLabel() {
        return Messages.getString("default.variable.name");
    }

    public Class<? extends GraphElement> getModelClass() {
        return Variable.class;
    }

}
