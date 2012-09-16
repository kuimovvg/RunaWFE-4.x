package ru.runa.bpm.ui.common;

import ru.runa.bpm.ui.common.model.GraphElement;
import ru.runa.bpm.ui.common.model.Swimlane;
import ru.runa.bpm.ui.resource.Messages;

public class SwimlaneTypeDefinition implements IElementTypeDefinition {

    public String getEntryLabel() {
        return Messages.getString("default.swimlane.name");
    }

    public Class<? extends GraphElement> getModelClass() {
        return Swimlane.class;
    }

}
