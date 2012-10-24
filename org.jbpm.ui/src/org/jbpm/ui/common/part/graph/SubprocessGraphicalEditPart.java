package org.jbpm.ui.common.part.graph;

import java.beans.PropertyChangeEvent;

import org.jbpm.ui.common.model.Subprocess;

public class SubprocessGraphicalEditPart extends LabeledNodeGraphicalEditPart {

    @Override
    public Subprocess getModel() {
        return (Subprocess) super.getModel();
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
