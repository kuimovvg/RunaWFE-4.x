package ru.runa.gpd.ui.dialog;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.TimerAction;

public class EscalationActionEditDialog extends TimerActionEditDialog {
    public EscalationActionEditDialog(ProcessDefinition definition, TimerAction timerAction) {
        super(definition, timerAction);
    }

    @Override
    protected boolean isClassNameFieldEnabled() {
        return false;
    }

    @Override
    protected String getConfigurationLabel() {
        return Localization.getString("property.escalation.configuration");
    }

    @Override
    protected boolean isDeleteButtonEnabled() {
        return false;
    }
}
