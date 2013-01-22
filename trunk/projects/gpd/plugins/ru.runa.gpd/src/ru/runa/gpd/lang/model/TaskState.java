package ru.runa.gpd.lang.model;

import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.orgfunction.OrgFunctionDefinition;
import ru.runa.gpd.orgfunction.OrgFunctionsRegistry;
import ru.runa.gpd.property.EscalationActionPropertyDescriptor;
import ru.runa.gpd.property.EscalationDurationPropertyDescriptor;
import ru.runa.gpd.property.TimeOutDurationPropertyDescriptor;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.util.Delay;
import ru.runa.gpd.util.ProjectFinder;
import ru.runa.wfe.handler.action.EscalationActionHandler;

import com.google.common.base.Strings;

public class TaskState extends State implements Synchronizable {
    private TimerAction escalationAction;
    private boolean ignoreSubstitution;
    private boolean useEscalation;
    private Delay escalationDelay = new Delay();
    private boolean async;
    private boolean asyncTaskCompleteOnProcessComplete;
    private BotTask botTask;

    public boolean isAsyncTaskCompleteOnProcessComplete() {
        return asyncTaskCompleteOnProcessComplete; // TODO
    }

    @Override
    public boolean testAttribute(Object target, String name, String value) {
        if (super.testAttribute(target, name, value)) {
            return true;
        }
        if ("bindSwimlaneExists".equals(name)) {
            if (getSwimlane() != null && getSwimlane().getDelegationConfiguration() != null) {
                OrgFunctionDefinition definition = OrgFunctionsRegistry.parseSwimlaneConfiguration(getSwimlane().getDelegationConfiguration());
                if (definition != null && BotTask.BOT_EXECUTOR_SWIMLANE_NAME.equals(definition.getName())) {
                    if (definition.getParameters().size() > 0) {
                        String botFolderValue = definition.getParameters().get(0).getValue();
                        for (IFolder folder : ProjectFinder.getAllBotFolders()) {
                            if (folder.getName().equals(botFolderValue)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isAsync() {
        return async;
    }

    @Override
    public void setAsync(boolean async) {
        if (this.async != async) {
            this.async = async;
            firePropertyChange(PROPERTY_ASYNC, !async, async);
        }
    }

    public BotTask getBotTask() {
        return botTask;
    }

    public void setBotTask(BotTask botTask) {
        // TODO dirty
        this.botTask = botTask;
    }

    public TimerAction getEscalationAction() {
        return escalationAction;
    }

    public void setEscalationAction(TimerAction escalationAction) {
        this.escalationAction = escalationAction;
    }

    public Delay getEscalationDelay() {
        return escalationDelay;
    }

    public void setEscalationDelay(Delay escalationDelay) {
        this.escalationDelay = escalationDelay;
        firePropertyChange(PROPERTY_ESCALATION, null, escalationDelay);
    }

    public boolean isUseEscalation() {
        return useEscalation;
    }

    public void setUseEscalation(boolean useEscalation) { // TODO refactor
        if (escalationAction == null || !this.useEscalation) {
            escalationAction = new TimerAction();
            escalationAction.setDelegationClassName(EscalationActionHandler.class.getName());
            String org_function = Activator.getPrefString(PrefConstants.P_ESCALATION_CONFIG);
            escalationAction.setDelegationConfiguration(org_function);
            String repeat = Activator.getPrefString(PrefConstants.P_ESCALATION_REPEAT);
            if (!Strings.isNullOrEmpty(repeat)) {
                escalationAction.setRepeatDuration(repeat);
            }
            String expirationTime = Activator.getPrefString(PrefConstants.P_ESCALATION_DURATION);
            if (!Strings.isNullOrEmpty(expirationTime)) {
                escalationDelay = new Delay(expirationTime);
            }
        }
        this.useEscalation = useEscalation;
        firePropertyChange(PROPERTY_ESCALATION, !useEscalation, useEscalation);
    }

    @Override
    public TimerAction getTimeOutAction() {
        return null;
    }

    public boolean isIgnoreSubstitution() {
        return ignoreSubstitution;
    }

    public void setIgnoreSubstitution(boolean ignoreSubstitution) {
        boolean old = this.ignoreSubstitution;
        this.ignoreSubstitution = ignoreSubstitution;
        firePropertyChange(PROPERTY_IGNORE_SUBSTITUTION, old, this.ignoreSubstitution);
    }

    @Override
    public List<IPropertyDescriptor> getCustomPropertyDescriptors() {
        List<IPropertyDescriptor> list = super.getCustomPropertyDescriptors();
        list.add(new PropertyDescriptor(PROPERTY_IGNORE_SUBSTITUTION, Localization.getString("property.ignoreSubstitution")));
        list.add(new TimeOutDurationPropertyDescriptor(PROPERTY_TIMEOUT_DELAY, this));
        if (useEscalation) {
            list.add(new EscalationActionPropertyDescriptor(PROPERTY_ESCALATION_ACTION, Localization.getString("escalation.action"), this));
            list.add(new EscalationDurationPropertyDescriptor(PROPERTY_ESCALATION_DURATION, this));
        }
        if (botTask != null) {
            list.add(new PropertyDescriptor(PROPERTY_BOT_TASK_NAME, Localization.getString("property.botTaskName")));
        }
        list.add(new PropertyDescriptor(PROPERTY_ASYNC, Localization.getString("property.execution.async")));
        return list;
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_ESCALATION_DURATION.equals(id)) {
            if (escalationDelay.hasDuration()) {
                return escalationDelay;
            }
            return "";
        }
        if (PROPERTY_TIMEOUT_DELAY.equals(id)) {
            Delay d = getTimeOutDelay();
            if (d == null || !d.hasDuration()) {
                return "";
            }
            return d;
        }
        if (PROPERTY_ESCALATION_ACTION.equals(id)) {
            return escalationAction;
        }
        if (PROPERTY_IGNORE_SUBSTITUTION.equals(id)) {
            return ignoreSubstitution ? Localization.getString("message.yes") : Localization.getString("message.no");
        }
        if (PROPERTY_ASYNC.equals(id)) {
            return async ? Localization.getString("message.yes") : Localization.getString("message.no");
        }
        if (PROPERTY_BOT_TASK_NAME.equals(id)) {
            return botTask == null ? "" : botTask.getName();
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_ESCALATION_ACTION.equals(id)) {
            setEscalationAction((TimerAction) value);
        } else if (PROPERTY_ESCALATION_DURATION.equals(id)) {
            setEscalationDelay((Delay) value);
        } else {
            super.setPropertyValue(id, value);
        }
    }

    @Override
    protected void validate() {
        super.validate();
        if (getBotTask() != null && (getBotTask().getDelegationConfiguration() == null || getBotTask().getDelegationConfiguration().trim().length() == 0)) {
            addError("taskState.createBotTaskConfig");
        }
    }
}
