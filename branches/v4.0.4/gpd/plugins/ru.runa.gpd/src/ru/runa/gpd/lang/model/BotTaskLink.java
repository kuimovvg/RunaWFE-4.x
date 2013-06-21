package ru.runa.gpd.lang.model;

import ru.runa.gpd.extension.HandlerArtifact;

/**
 * Provides mapping with formal parameters between {@link TaskState} and {@link BotTask}.
 * 
 * Configuration is in param-based xml.
 * 
 * @author Dofs
 * @since 3.6
 */
public class BotTaskLink implements Delegable {
    private String botTaskName;
    private String delegationClassName;
    private String delegationConfiguration = "";

    /**
     * linked {@link BotTask} name
     */
    public String getBotTaskName() {
        return botTaskName;
    }

    /**
     * linked {@link BotTask} name
     */
    public void setBotTaskName(String botTaskName) {
        this.botTaskName = botTaskName;
    }

    @Override
    public String getDelegationType() {
        return HandlerArtifact.TASK_HANDLER;
    }

    @Override
    public String getDelegationClassName() {
        return delegationClassName;
    }

    @Override
    public void setDelegationClassName(String delegateClassName) {
        this.delegationClassName = delegateClassName;
    }

    @Override
    public String getDelegationConfiguration() {
        return delegationConfiguration;
    }

    @Override
    public void setDelegationConfiguration(String delegationConfiguration) {
        this.delegationConfiguration = delegationConfiguration;
    }
}
