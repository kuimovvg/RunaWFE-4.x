package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import ru.runa.wfe.audit.presentation.ExecutorNameValue;
import ru.runa.wfe.user.Actor;

@Entity
@DiscriminatorValue(value = "E")
public class AdminActionLog extends ProcessLog {
    private static final long serialVersionUID = 1L;
    public static final String ACTION_UPDATE_VARIABLES = "update_variables";
    public static final String ACTION_UPGRADE_PROCESS_TO_NEXT_VERSION = "upgrate_to_next_version";

    public AdminActionLog() {
    }

    public AdminActionLog(Actor actor, String actionName) {
        addAttribute(ATTR_ACTOR_NAME, actor.getName());
        addAttribute(ATTR_ACTION, actionName);
        setSeverity(Severity.INFO);
    }

    @Override
    @Transient
    public String getPatternName() {
        return super.getPatternName() + "." + getAttributeNotNull(ATTR_ACTION);
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { new ExecutorNameValue(getAttributeNotNull(ATTR_ACTOR_NAME)) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onAdminActionLog(this);
    }
}
