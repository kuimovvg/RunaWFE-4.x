package ru.runa.gpd.editor.gef.part.graph;

import java.beans.PropertyChangeEvent;

import ru.runa.gpd.lang.model.Subprocess;

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
