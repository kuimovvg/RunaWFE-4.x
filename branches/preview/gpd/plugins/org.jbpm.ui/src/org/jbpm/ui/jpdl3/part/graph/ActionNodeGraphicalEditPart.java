package ru.runa.bpm.ui.jpdl3.part.graph;

import java.util.List;

import org.eclipse.gef.EditPolicy;
import ru.runa.bpm.ui.common.part.graph.ActionsHost;
import ru.runa.bpm.ui.common.part.graph.LabeledNodeGraphicalEditPart;
import ru.runa.bpm.ui.common.policy.ActiveLayoutEditPolicy;
import ru.runa.bpm.ui.jpdl3.figure.ActionNodeFigure;
import ru.runa.bpm.ui.jpdl3.model.ActionNode;

public class ActionNodeGraphicalEditPart extends LabeledNodeGraphicalEditPart implements ActionsHost {

    @Override
    public ActionNode getModel() {
        return (ActionNode) super.getModel();
    }

    @Override
    public ActionNodeFigure getFigure() {
        return (ActionNodeFigure) super.getFigure();
    }

    @Override
    protected List<? extends Object> getModelChildren() {
        return getModel().getActions();
    }
    
    public void refreshActionsVisibility(boolean visible) {
        getFigure().getActionsContainer().setVisible(visible);
    }

    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new ActiveLayoutEditPolicy());
    }
}
