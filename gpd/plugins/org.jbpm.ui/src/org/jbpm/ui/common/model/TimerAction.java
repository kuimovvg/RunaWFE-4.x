package ru.runa.bpm.ui.common.model;

import ru.runa.bpm.ui.resource.Messages;
import ru.runa.bpm.ui.util.TimerDuration;

public class TimerAction extends Action {
    public final static TimerAction NONE = new TimerAction(null);
    
    private TimerDuration repeat = new TimerDuration(TimerDuration.EMPTY);

    private final ProcessDefinition definition;
    public TimerAction(ProcessDefinition definition) {
        setDelegationClassName("");
        this.definition = definition;
    }
    
    public TimerDuration getRepeat() {
        return repeat;
    }
    
    @Override
    public void setDirty() {
        // stub
    }
    
    @Override
    public ProcessDefinition getProcessDefinition() {
        return definition;
    }
    
    public void setRepeat(String repeat) {
        if (repeat != null) {
            this.repeat = new TimerDuration(repeat);
        }
    }
    
    public boolean isValid() {
        return getDelegationClassName().length() > 0;
    }
    
    @Override
    public String toString() {
        if (getDelegationClassName().length() == 0) 
            return "";
        StringBuffer buffer = new StringBuffer(getDelegationClassName());
        buffer.append(" | ");
        buffer.append(repeat.hasDuration() ? repeat : Messages.getString("duration.norepeat"));
        return buffer.toString();
    }
}
