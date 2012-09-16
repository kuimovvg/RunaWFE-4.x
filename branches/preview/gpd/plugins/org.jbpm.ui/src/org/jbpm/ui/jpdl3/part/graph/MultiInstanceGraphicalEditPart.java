package ru.runa.bpm.ui.jpdl3.part.graph;

import java.beans.PropertyChangeEvent;

import ru.runa.bpm.ui.common.part.graph.LabeledNodeGraphicalEditPart;
import ru.runa.bpm.ui.jpdl3.model.MultiInstance;

public class MultiInstanceGraphicalEditPart extends LabeledNodeGraphicalEditPart {

    @Override
    public MultiInstance getModel() {
        return (MultiInstance) super.getModel();
    }

    @Override
    protected String getTooltipMessage() {
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
