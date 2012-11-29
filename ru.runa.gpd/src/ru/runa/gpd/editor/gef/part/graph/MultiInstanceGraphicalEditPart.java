package ru.runa.gpd.editor.gef.part.graph;

import java.beans.PropertyChangeEvent;

import ru.runa.gpd.lang.model.MultiInstance;

public class MultiInstanceGraphicalEditPart extends LabeledNodeGraphicalEditPart {
    @Override
    public MultiInstance getModel() {
        return (MultiInstance) super.getModel();
    }

    @Override
    protected String getTooltipMessage() { // TODO move tooltip to model
        return getModel().getSubProcessName();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        if (PROPERTY_SUBPROCESS.equals(evt.getPropertyName())) {
            updateTooltip(getFigure());
        }
    }
}
