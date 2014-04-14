package ru.runa.gpd.lang.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.Activator;
import ru.runa.gpd.BotCache;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.extension.handler.ParamDefConfig;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.property.DurationPropertyDescriptor;
import ru.runa.gpd.property.EscalationActionPropertyDescriptor;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.util.BotTaskUtils;
import ru.runa.gpd.util.Duration;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.wfe.extension.handler.EscalationActionHandler;
import ru.runa.wfe.lang.AsyncCompletionMode;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

public class TaskState extends FormNode implements Active, ITimed, Synchronizable {
    private TimerAction escalationAction;
    private boolean ignoreSubstitutionRules;
    private boolean useEscalation;
    private Duration escalationDelay = new Duration();
    private boolean async;
    private AsyncCompletionMode asyncCompletionMode = AsyncCompletionMode.ON_MAIN_PROCESS_END;
    private BotTaskLink botTaskLink;
    private Duration timeOutDelay = new Duration();
    private boolean reassignmentEnabled = false;

    @Override
    public Timer getTimer() {
        return getFirstChild(Timer.class);
    }

    public boolean isReassignmentEnabled() {
        return reassignmentEnabled;
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

    public void setTimeOutDelay(Duration timeOutDuration) {
        Duration old = this.timeOutDelay;
        this.timeOutDelay = timeOutDuration;
        firePropertyChange(PROPERTY_TASK_DEADLINE, old, this.timeOutDelay);
    }

    public Duration getTimeOutDelay() {
        return timeOutDelay;
    }

    @Override
    public String getNextTransitionName() {
        if (getProcessDefinition().getLanguage() == Language.JPDL && getTimer() != null && getTransitionByName(PluginConstants.TIMER_TRANSITION_NAME) == null) {
            return PluginConstants.TIMER_TRANSITION_NAME;
        }
        return super.getNextTransitionName();
    }

    @Override
    public AsyncCompletionMode getAsyncCompletionMode() {
        return asyncCompletionMode;
    }

    @Override
    public void setAsyncCompletionMode(AsyncCompletionMode asyncCompletionMode) {
        AsyncCompletionMode old = this.asyncCompletionMode;
        this.asyncCompletionMode = asyncCompletionMode;
        firePropertyChange(PROPERTY_ASYNC_COMPLETION_MODE, old, asyncCompletionMode);
    }

    /**
     * @see BotTaskUtils#getBotName(org.jbpm.ui.common.model.Swimlane)
     * @return bot name or <code>null</code>
     */
    public String getSwimlaneBotName() {
        return BotTaskUtils.getBotName(getSwimlane());
    }

    @Override
    public boolean testAttribute(Object target, String name, String value) {
        if (super.testAttribute(target, name, value)) {
            return true;
        }
        if ("swimlanePointsToBot".equals(name)) {
            String botName = getSwimlaneBotName();
            return botName != null && BotCache.getAllBotNames().contains(botName);
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

    public BotTaskLink getBotTaskLink() {
        return botTaskLink;
    }

    public void setBotTaskLink(BotTaskLink botTaskLink) {
        if (!Objects.equal(this.botTaskLink, botTaskLink)) {
            this.botTaskLink = botTaskLink;
            if (this.botTaskLink != null) {
                this.botTaskLink.setTaskState(this);
            }
            firePropertyChange(PROPERTY_BOT_TASK_NAME, null, "");
        }
    }

    public TimerAction getEscalationAction() {
        return escalationAction;
    }

    public void setEscalationAction(TimerAction escalationAction) {
        this.escalationAction = escalationAction;
    }

    public Duration getEscalationDelay() {
        return escalationDelay;
    }

    public void setEscalationDelay(Duration escalationDelay) {
        this.escalationDelay = escalationDelay;
        firePropertyChange(PROPERTY_ESCALATION, null, escalationDelay);
    }

    public boolean isUseEscalation() {
        return useEscalation;
    }

    public void setUseEscalation(boolean useEscalation) { // TODO refactor
        if (escalationAction == null || !this.useEscalation) {
            escalationAction = new TimerAction(getProcessDefinition());
            escalationAction.setDelegationClassName(EscalationActionHandler.class.getName());
            String orgFunction = Activator.getPrefString(PrefConstants.P_ESCALATION_CONFIG);
            escalationAction.setDelegationConfiguration(orgFunction);
            String repeat = Activator.getPrefString(PrefConstants.P_ESCALATION_REPEAT);
            if (!Strings.isNullOrEmpty(repeat)) {
                escalationAction.setRepeatDuration(repeat);
            }
            String expirationTime = Activator.getPrefString(PrefConstants.P_ESCALATION_DURATION);
            if (!Strings.isNullOrEmpty(expirationTime)) {
                escalationDelay = new Duration(expirationTime);
            }
        }
        this.useEscalation = useEscalation;
        firePropertyChange(PROPERTY_ESCALATION, !useEscalation, useEscalation);
    }

    public boolean isIgnoreSubstitutionRules() {
        return ignoreSubstitutionRules;
    }

    public void setIgnoreSubstitutionRules(boolean ignoreSubstitutionRules) {
        boolean old = this.ignoreSubstitutionRules;
        this.ignoreSubstitutionRules = ignoreSubstitutionRules;
        firePropertyChange(PROPERTY_IGNORE_SUBSTITUTION_RULES, old, this.ignoreSubstitutionRules);
    }

    @Override
    public List<IPropertyDescriptor> getCustomPropertyDescriptors() {
        List<IPropertyDescriptor> list = super.getCustomPropertyDescriptors();
        list.add(new PropertyDescriptor(PROPERTY_IGNORE_SUBSTITUTION_RULES, Localization.getString("property.ignoreSubstitution")));
        list.add(new DurationPropertyDescriptor(PROPERTY_TASK_DEADLINE, getProcessDefinition(), getTimeOutDelay(), Localization.getString("property.deadline")));
        if (useEscalation) {
            list.add(new EscalationActionPropertyDescriptor(PROPERTY_ESCALATION_ACTION, Localization.getString("escalation.action"), this));
            list.add(new DurationPropertyDescriptor(PROPERTY_ESCALATION_DURATION, getProcessDefinition(), getEscalationDelay(), Localization.getString("escalation.duration")));
        }
        if (botTaskLink != null) {
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
        if (PROPERTY_TASK_DEADLINE.equals(id)) {
            Duration d = getTimeOutDelay();
            if (d == null || !d.hasDuration()) {
                return "";
            }
            return d;
        }
        if (PROPERTY_ESCALATION_ACTION.equals(id)) {
            return escalationAction;
        }
        if (PROPERTY_IGNORE_SUBSTITUTION_RULES.equals(id)) {
            return ignoreSubstitutionRules ? Localization.getString("yes") : Localization.getString("no");
        }
        if (PROPERTY_ASYNC.equals(id)) {
            return async ? Localization.getString("yes") : Localization.getString("no");
        }
        if (PROPERTY_BOT_TASK_NAME.equals(id)) {
            return botTaskLink == null ? "" : botTaskLink.getBotTaskName();
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_ESCALATION_ACTION.equals(id)) {
            setEscalationAction((TimerAction) value);
        } else if (PROPERTY_ESCALATION_DURATION.equals(id)) {
            setEscalationDelay((Duration) value);
        } else if (PROPERTY_TASK_DEADLINE.equals(id)) {
            if (value == null) {
                // ignore, edit was canceled
                return;
            }
            setTimeOutDelay((Duration) value);
        } else {
            super.setPropertyValue(id, value);
        }
    }
    
    @Override
    public TaskState getCopy(GraphElement parent) {
        TaskState copy = (TaskState) super.getCopy(parent);
        copy.setAsync(async);
        copy.setAsyncCompletionMode(copy.getAsyncCompletionMode());
        if (getBotTaskLink() != null) {
            copy.setBotTaskLink(getBotTaskLink().getCopy(copy));
        }
        if (getEscalationAction() != null) {
            copy.setEscalationAction(getEscalationAction().getCopy(parent.getProcessDefinition()));
        }
        if (getEscalationDelay() != null) {
            copy.setEscalationDelay(new Duration(getEscalationDelay()));
        }
        copy.setIgnoreSubstitutionRules(ignoreSubstitutionRules);
        copy.setReassignmentEnabled(reassignmentEnabled);
        if (getTimeOutDelay() != null) {
            copy.setTimeOutDelay(new Duration(getTimeOutDelay()));
        }
        copy.setUseEscalation(useEscalation);
        return copy;
    }

    @Override
    public List<Variable> getUsedVariables(IFolder processFolder) {
        List<Variable> result = super.getUsedVariables(processFolder);
        if (getBotTaskLink() != null && !Strings.isNullOrEmpty(getBotTaskLink().getDelegationConfiguration())) {
                Map<String, String> map = ParamDefConfig.getAllParameters(getBotTaskLink().getDelegationConfiguration());
                for (String variableName : map.values()) {
                    Variable variable = VariableUtils.getVariableByName(getProcessDefinition(), variableName);
                    if (variable != null) {
                        result.add(variable);
                    }
                }
        }
        return result;
    }

    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
        super.validate(errors, definitionFile);
        if (getBotTaskLink() != null) {
            Set<String> linkConfigParameterNames;
            if (Strings.isNullOrEmpty(getBotTaskLink().getDelegationConfiguration())) {
                linkConfigParameterNames = Sets.newHashSet();
            } else {
                ParamDefConfig linkConfig = ParamDefConfig.parse(getBotTaskLink().getDelegationConfiguration());
                linkConfigParameterNames = linkConfig.getAllParameterNames(true);
            }
            BotTask botTask = BotCache.getBotTask(getSwimlaneBotName(), getBotTaskLink().getBotTaskName());
            if (botTask == null) {
                errors.add(ValidationError.createLocalizedWarning(this, "taskState.botTaskLink.botTaskNotFound", 
                        getSwimlaneBotName(), getBotTaskLink().getBotTaskName()));
                return;
            }
            if (botTask.getParamDefConfig() == null) {
                errors.add(ValidationError.createLocalizedWarning(this, "taskState.botTaskParamDefConfig.null"));
                return;
            }
            Set<String> botTaskConfigParameterNames = botTask.getParamDefConfig().getAllParameterNames(true);
            if (linkConfigParameterNames.isEmpty() && !botTaskConfigParameterNames.isEmpty()) {
                errors.add(ValidationError.createLocalizedError(this, "taskState.botTaskLinkConfig.empty"));
                return;
            }
            botTaskConfigParameterNames.removeAll(linkConfigParameterNames);
            if (botTaskConfigParameterNames.size() > 0) {
                errors.add(ValidationError.createLocalizedError(this, "taskState.botTaskLinkConfig.insufficient", botTaskConfigParameterNames));
            }
        }
        if (isAsync() && getTimer() != null) {
            errors.add(ValidationError.createLocalizedError(this, "taskState.timerInAsyncTask"));
        }
    }
}
