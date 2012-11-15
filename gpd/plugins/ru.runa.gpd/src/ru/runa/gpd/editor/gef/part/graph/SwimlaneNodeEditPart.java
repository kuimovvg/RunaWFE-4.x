package ru.runa.gpd.editor.gef.part.graph;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;

import ru.runa.gpd.editor.gef.figure.NodeFigure;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;

public class SwimlaneNodeEditPart extends LabeledNodeGraphicalEditPart {

    @Override
    public SwimlanedNode getModel() {
        return (SwimlanedNode) super.getModel();
    }

    private Swimlane getSwimlane() {
        return getModel().getSwimlane();
    }
    
    @Override
    protected IFigure createFigure() {
        NodeFigure figure = (NodeFigure) super.createFigure();
        figure.setSwimlaneName(getSwimlane());
        return figure;
    }
    
    @Override
    public void activate() {
        if (!isActive()) {
            Swimlane swimlane = getSwimlane();
            if (swimlane != null) {
                swimlane.addPropertyChangeListener(this);
            }
            super.activate();
        }
    }

    @Override
    public void deactivate() {
        if (isActive()) {
            Swimlane swimlane = getSwimlane();
            if (swimlane != null) {
                swimlane.removePropertyChangeListener(this);
            }
            super.deactivate();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        String propertyName = evt.getPropertyName();
        if (PROPERTY_SWIMLANE.equals(propertyName)) {
            Swimlane oldSwimlane = (Swimlane) evt.getOldValue();
            Swimlane newSwimlane = (Swimlane) evt.getNewValue();
            if (oldSwimlane != null) {
                oldSwimlane.removePropertyChangeListener(this);
            }
            if (newSwimlane != null) {
                newSwimlane.addPropertyChangeListener(this);
            }
            getFigure().setSwimlaneName(newSwimlane);
        }
        if (PROPERTY_NAME.equals(propertyName) && evt.getSource() instanceof Swimlane) {
            getFigure().setSwimlaneName(getModel().getSwimlane());
        }
    }

}
