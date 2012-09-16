package ru.runa.bpm.ui.common.part.tree;

import java.beans.PropertyChangeEvent;

import ru.runa.bpm.ui.common.model.Action;

public class ActionTreeEditPart extends ElementTreeEditPart {

    @Override
    protected void refreshVisuals() {
        super.refreshVisuals();
        setWidgetText(getLabel());
    }

    protected String getLabel() {
        return ((Action) getModel()).getDisplayName();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        String messageId = evt.getPropertyName();
        if (PROPERTY_CLASS.equals(messageId)) {
            refreshVisuals();
        }
    }

}
