package ru.runa.bpm.ui.common.policy;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import ru.runa.bpm.ui.common.command.NodeChangeConstraintCommand;
import ru.runa.bpm.ui.common.command.NodeCreateCommand;
import ru.runa.bpm.ui.common.figure.GEFConstants;
import ru.runa.bpm.ui.common.figure.NodeFigure;
import ru.runa.bpm.ui.common.model.Node;
import ru.runa.bpm.ui.common.model.ProcessDefinition;

public class ProcessDefinitionXYLayoutEditPolicy extends XYLayoutEditPolicy {

    @Override
    protected Command createAddCommand(EditPart child, Object constraint) {
        return null;
    }

    @Override
    protected EditPolicy createChildEditPolicy(EditPart child) {
        IFigure figure = ((GraphicalEditPart) child).getFigure();
        if (figure instanceof NodeFigure && !((NodeFigure) figure).isResizeable()) {
            return new NonResizableEditPolicy();
        }
        return new ResizableEditPolicy();
    }

    @Override
    protected Command createChangeConstraintCommand(EditPart child, Object constraint) {
        NodeChangeConstraintCommand locationCommand = new NodeChangeConstraintCommand();
        locationCommand.setNode((Node) child.getModel());
        Rectangle newRect = getClosestRectangle((Rectangle) constraint);
        locationCommand.setNewConstraint(newRect);
        return locationCommand;
    }

    @Override
    protected Command getCreateCommand(CreateRequest request) {
        Object newObject = request.getNewObject();
        if (newObject instanceof Node) {
            NodeCreateCommand createCommand = new NodeCreateCommand();
            createCommand.setNode((Node) newObject);
            createCommand.setParent((ProcessDefinition) getHost().getModel());
            Rectangle newRect = getClosestRectangle((Rectangle) getConstraintFor(request));
            createCommand.setConstraint(newRect);
            return createCommand;
        }
        return null;
    }

    @Override
    protected Command getDeleteDependantCommand(Request request) {
        return null;
    }

    private Rectangle getClosestRectangle(Rectangle rect) {
        int xCount = (int) Math.round((double) rect.x / GEFConstants.GRID_SIZE);
        int yCount = (int) Math.round((double) rect.y / GEFConstants.GRID_SIZE);
        int wCount = (int) Math.round((double) rect.width / GEFConstants.GRID_SIZE);
        int hCount = (int) Math.round((double) rect.height / GEFConstants.GRID_SIZE);
        return new Rectangle(xCount * GEFConstants.GRID_SIZE, yCount * GEFConstants.GRID_SIZE, wCount * GEFConstants.GRID_SIZE, hCount
                * GEFConstants.GRID_SIZE);
    }
}
