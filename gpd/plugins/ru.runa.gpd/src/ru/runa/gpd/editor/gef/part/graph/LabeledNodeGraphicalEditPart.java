package ru.runa.gpd.editor.gef.part.graph;

import java.beans.PropertyChangeEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.jface.viewers.TextCellEditor;

import ru.runa.gpd.editor.gef.figure.NodeFigure;
import ru.runa.gpd.editor.gef.policy.NodeDirectEditPolicy;
import ru.runa.gpd.util.LabelCellEditorLocator;
import ru.runa.gpd.util.LabelDirectEditManager;

public class LabeledNodeGraphicalEditPart extends NodeGraphicalEditPart {

    private DirectEditManager directEditManager;

    @Override
    protected IFigure createFigure() {
        NodeFigure figure = (NodeFigure) super.createFigure();
        figure.setName(getModel().getName());
        updateTooltip(figure);
        return figure;
    }

    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new NodeDirectEditPolicy());
    }

    private void performDirectEdit() {
        if (directEditManager == null) {
            CellEditorLocator locator = new LabelCellEditorLocator(getFigure().getLabel());
            directEditManager = new LabelDirectEditManager(this, TextCellEditor.class, locator);
        }
        if (getFigure().getLabel().isVisible()) {
            directEditManager.show();
        }
    }

    @Override
    public void performRequest(Request request) {
        if (request.getType() == RequestConstants.REQ_DIRECT_EDIT) {
            performDirectEdit();
        } else if (request.getType() == RequestConstants.REQ_OPEN) {
            performDirectEdit();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        if (PROPERTY_NAME.equals(evt.getPropertyName())) {
            getFigure().setName(getModel().getName());
        }
    }

}
