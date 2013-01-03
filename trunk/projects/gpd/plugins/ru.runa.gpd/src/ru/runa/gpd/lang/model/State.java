package ru.runa.gpd.lang.model;

import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.util.Delay;

import com.google.common.base.Objects;

public abstract class State extends FormNode implements Active, ITimed, ITimeOut {
    private Delay timeOutDelay = new Delay();
    private boolean reassignmentEnabled = false;
    private boolean minimizedView = false;

    @Override
    public boolean testAttribute(Object target, String name, String value) {
        if (super.testAttribute(target, name, value)) {
            return true;
        }
        if ("minimizedView".equals(name)) {
            return Objects.equal(value, isMinimizedView());
        }
        return false;
    }

    @Override
    public Timer getTimer() {
        return getFirstChild(Timer.class);
    }

    public boolean isReassignmentEnabled() {
        return reassignmentEnabled;
    }

    public boolean isMinimizedView() {
        return minimizedView;
    }

    public void setMinimizedView(boolean minimazedView) {
        this.minimizedView = minimazedView;
        firePropertyChange(PROPERTY_MINIMAZED_VIEW, !reassignmentEnabled, reassignmentEnabled);
    }

    public void setReassignmentEnabled(boolean forceReassign) {
        this.reassignmentEnabled = forceReassign;
        firePropertyChange(PROPERTY_SWIMLANE_REASSIGN, !reassignmentEnabled, reassignmentEnabled);
    }

    public String getTimeOutDueDate() {
        if (timeOutDelay == null || !timeOutDelay.hasDuration()) {
            return null;
        }
        return timeOutDelay.getDuration();
    }

    @Override
    public void setTimeOutDelay(Delay timeOutDuration) {
        this.timeOutDelay = timeOutDuration;
        firePropertyChange(PROPERTY_TIMEOUT_DELAY, null, null);
    }

    @Override
    public Delay getTimeOutDelay() {
        return timeOutDelay;
    }

    @Override
    public String getNextTransitionName() {
        if (getTimer() != null && getTransitionByName(PluginConstants.TIMER_TRANSITION_NAME) == null) {
            return PluginConstants.TIMER_TRANSITION_NAME;
        }
        return super.getNextTransitionName();
    }

    @Override
    public void addLeavingTransition(Transition transition) {
        if (getTimer() == null && PluginConstants.TIMER_TRANSITION_NAME.equals(transition.getName())) {
            transition.setName(getNextTransitionName());
        }
        super.addLeavingTransition(transition);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_TIMEOUT_DELAY.equals(id)) {
            if (value == null) {
                // ignore, edit was canceled
                return;
            }
            setTimeOutDelay((Delay) value);
        } else {
            super.setPropertyValue(id, value);
        }
    }
}
