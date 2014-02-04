package ru.runa.gpd.lang.model;

import ru.runa.gpd.Localization;
import ru.runa.gpd.util.Duration;

import com.google.common.base.Strings;

public class TimerAction extends Action {
    private Duration repeatDelay = new Duration();
    private final ProcessDefinition processDefinition;

    public TimerAction(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
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
        return processDefinition;
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
    
    @Override
    public TimerAction getCopy(GraphElement parent) {
        TimerAction copy = new TimerAction((ProcessDefinition) parent);
        copy.setDescription(getDescription());
        copy.setDelegationClassName(getDelegationClassName());
        copy.setDelegationConfiguration(getDelegationConfiguration());
        copy.setRepeatDuration(getRepeatDelay().getDuration());
        return copy;
    }

}
