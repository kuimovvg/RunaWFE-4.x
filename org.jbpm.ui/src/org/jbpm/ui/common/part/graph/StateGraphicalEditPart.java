package org.jbpm.ui.common.part.graph;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.jbpm.ui.common.figure.TimerStateFigure;
import org.jbpm.ui.common.model.State;
import org.jbpm.ui.common.policy.ActiveLayoutEditPolicy;
import org.jbpm.ui.jpdl3.model.TaskState;

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
    public boolean testAttribute(Object target, String name, String value) {
        if ("org.jbpm.ui.timerExists".equals(name)) {
            return value.equals(String.valueOf(getModel().timerExist()));
        }
        if ("org.jbpm.ui.escalationEnableOn".equals(name)) {
        	if (!TaskState.class.isInstance(getModel())) return false;
        	TaskState state = (TaskState) getModel();
            return value.equals(String.valueOf(!state.isUseEscalation()));
        }
        if ("org.jbpm.ui.escalationEnableOff".equals(name)) {
        	if (!TaskState.class.isInstance(getModel())) return false;
        	TaskState state = (TaskState) getModel();
            return value.equals(String.valueOf(state.isUseEscalation()));
        }
        if ("org.jbpm.ui.enableMinimizeView".equals(name)) {
            return !getModel().isMinimizedView();
        }
        if ("org.jbpm.ui.enableRestoreView".equals(name)) {
            return getModel().isMinimizedView();
        }
        return super.testAttribute(target, name, value);
    }

    @Override
    protected String getTooltipMessage() {
        String tooltip = null;
        if (getModel().isMinimizedView()) {
            tooltip = "(" + getModel().getSwimlaneName() + ")";
            tooltip += "\n" + getModel().getName();
        }
        return tooltip;
    }

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
        if (PROPERTY_MINIMAZED_VIEW.equals(evt.getPropertyName()) || PROPERTY_SWIMLANE.equals(evt.getPropertyName())
                || PROPERTY_NAME.equals(evt.getPropertyName())) {
            updateTooltip(getFigure());
        }
    }

}
