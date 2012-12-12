package ru.runa.gpd.editor.gef.part.graph;

import java.util.List;

import org.eclipse.gef.EditPolicy;

import ru.runa.gpd.editor.gef.figure.StateFigure;
import ru.runa.gpd.editor.gef.policy.ActiveLayoutEditPolicy;
import ru.runa.gpd.lang.model.State;

public class StateGraphicalEditPart extends SwimlaneNodeEditPart implements ActionsHost {
    @Override
    public State getModel() {
        return (State) super.getModel();
    }

    @Override
    public StateFigure getFigure() {
        return (StateFigure) super.getFigure();
    }

    @Override
    protected List<? extends Object> getModelChildren() {
        return getModel().getActions();
    }

    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new ActiveLayoutEditPolicy());
    }

    @Override
    public void refreshActionsVisibility(boolean visible) {
        getFigure().getActionsContainer().setVisible(visible);
    }

    @Override
    protected void fillFigureUpdatePropertyNames(List<String> list) {
        super.fillFigureUpdatePropertyNames(list);
        list.add(PROPERTY_MINIMAZED_VIEW);
    }
}
