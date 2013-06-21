package ru.runa.gpd.lang.model;

import ru.runa.gpd.Localization;
import ru.runa.gpd.util.Duration;

import com.google.common.base.Strings;

public class TimerAction extends Action {
    public final static TimerAction NONE = new TimerAction();
    private Duration repeatDelay = new Duration();
    private ProcessDefinition definition;

    public TimerAction() {
    }

    public Duration getRepeatDelay() {
        return repeatDelay;
    }

    @Override
    public void setDirty() {
        // stub
    }

    @Override
    public ProcessDefinition getProcessDefinition() {
        return definition;
    }

    public void setDefinition(ProcessDefinition definition) {
        this.definition = definition;
    }

    public void setRepeatDuration(String duration) {
        if (!Strings.isNullOrEmpty(duration)) {
            this.repeatDelay = new Duration(duration);
        }
    }

    public boolean isValid() {
        return !Strings.isNullOrEmpty(getDelegationClassName());
    }

    @Override
    public String toString() {
        if (Strings.isNullOrEmpty(getDelegationClassName())) {
            return "";
        }
        StringBuffer buffer = new StringBuffer(getDelegationClassName());
        buffer.append(" | ");
        buffer.append(repeatDelay.hasDuration() ? repeatDelay : Localization.getString("duration.norepeat"));
        return buffer.toString();
    }
}
