package ru.runa.gpd.lang.model;


import ru.runa.gpd.Localization;
import ru.runa.gpd.util.TimerDuration;

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
        buffer.append(repeat.hasDuration() ? repeat : Localization.getString("duration.norepeat"));
        return buffer.toString();
    }
}
