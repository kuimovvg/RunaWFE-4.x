package ru.runa.gpd.lang.model;

import java.util.List;
import java.util.Set;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.Activator;
import ru.runa.gpd.BotCache;
import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.handler.ParamDefConfig;
import ru.runa.gpd.property.DurationPropertyDescriptor;
import ru.runa.gpd.property.EscalationActionPropertyDescriptor;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.util.BotTaskUtils;
import ru.runa.gpd.util.Duration;
import ru.runa.wfe.extension.handler.EscalationActionHandler;
import ru.runa.wfe.lang.AsyncCompletionMode;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

public class TaskState extends State implements Synchronizable {
    private TimerAction escalationAction;
    private boolean ignoreSubstitution;
    private boolean useEscalation;
    private Duration escalationDelay = new Duration();
    private boolean async;
    private AsyncCompletionMode asyncCompletionMode = AsyncCompletionMode.ON_MAIN_PROCESS_END;
    private BotTaskLink botTaskLink;

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
                escalationDelay = new Duration(expirationTime);
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
        list.add(new DurationPropertyDescriptor(PROPERTY_TIMEOUT_DELAY, getProcessDefinition(), getTimeOutDelay(), Localization.getString("timeout.property.duration")));
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
        if (PROPERTY_TIMEOUT_DELAY.equals(id)) {
            Duration d = getTimeOutDelay();
            if (d == null || !d.hasDuration()) {
                return "";
            }
            return d;
        }
        if (PROPERTY_ESCALATION_ACTION.equals(id)) {
            return escalationAction;
        }
        if (PROPERTY_IGNORE_SUBSTITUTION.equals(id)) {
            return ignoreSubstitution ? Localization.getString("yes") : Localization.getString("no");
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
        } else {
            super.setPropertyValue(id, value);
        }
    }

    @Override
    protected void validate() {
        super.validate();
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
                addError("taskState.botTaskLink.botTaskNotFound", getSwimlaneBotName(), getBotTaskLink().getBotTaskName());
                return;
            }
            Set<String> botTaskConfigParameterNames = botTask.getParamDefConfig().getAllParameterNames(true);
            if (linkConfigParameterNames.isEmpty() && !botTaskConfigParameterNames.isEmpty()) {
                addError("taskState.botTaskLinkConfig.empty");
                return;
            }
            botTaskConfigParameterNames.removeAll(linkConfigParameterNames);
            if (botTaskConfigParameterNames.size() > 0) {
                addError("taskState.botTaskLinkConfig.insufficient", botTaskConfigParameterNames);
            }
        }
    }
}
