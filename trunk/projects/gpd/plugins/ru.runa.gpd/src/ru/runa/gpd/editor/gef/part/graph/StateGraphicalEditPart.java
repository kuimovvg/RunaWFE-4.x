package ru.runa.gpd.editor.gef.part.graph;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;

import ru.runa.gpd.editor.gef.figure.TimerStateFigure;
import ru.runa.gpd.editor.gef.policy.ActiveLayoutEditPolicy;
import ru.runa.gpd.lang.model.State;

public class StateGraphicalEditPart extends FormNodeEditPart implements ActionsHost {
    @Override
    public State getModel() {
        return (State) super.getModel();
    }

    @Override
    protected IFigure createFigure() {
        TimerStateFigure figure = (TimerStateFigure) super.createFigure();
        figure.setTimerExist(getModel().timerExist());
        figure.setMinimizedView(getModel().isMinimizedView(), getModel().getProcessDefinition().isShowActions());
        return figure;
    }

    @Override
    public TimerStateFigure getFigure() {
        return (TimerStateFigure) super.getFigure();
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
    protected String getTooltipMessage() {
        String tooltip = null;
        if (getModel().isMinimizedView()) {
            tooltip = getModel().getSwimlaneLabel();
            tooltip += "\n" + getModel().getName();
        }
        return tooltip;
    }

    @Override
    public void refreshActionsVisibility(boolean visible) {
        getFigure().getActionsContainer().setVisible(visible);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        if (PROPERTY_TIMER.equals(evt.getPropertyName())) {
            getFigure().setTimerExist(getModel().timerExist());
            refreshVisuals();
        }
        /*if (PROPERTY_ESCALATION.equals(evt.getPropertyName())) {
            getFigure().setTimerExist(getModel().timerExist());
            refreshVisuals();
        }*/
        if (PROPERTY_MINIMAZED_VIEW.equals(evt.getPropertyName())) {
            getFigure().setMinimizedView(getModel().isMinimizedView(), getModel().getProcessDefinition().isShowActions());
            refreshVisuals();
        }
        if (PROPERTY_MINIMAZED_VIEW.equals(evt.getPropertyName()) || PROPERTY_SWIMLANE.equals(evt.getPropertyName()) || PROPERTY_NAME.equals(evt.getPropertyName())) {
            updateTooltip(getFigure());
        }
    }
}
