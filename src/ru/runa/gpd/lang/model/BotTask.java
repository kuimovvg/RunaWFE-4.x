package ru.runa.gpd.lang.model;

import ru.runa.gpd.handler.HandlerArtifact;
import ru.runa.gpd.handler.action.ParamDefConfig;

public class BotTask implements Delegable {
    public static final String BOT_EXECUTOR_SWIMLANE_NAME = "ExecutorByNameFunction";
    private String name;
    private String clazz;
    private String config;
    private ParamDefConfig paramDefConfig;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    private boolean dirty;

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        boolean stateChanged = this.dirty != dirty;
        if (stateChanged) {
            this.dirty = dirty;
            //firePropertyChange(NotificationMessages.PROPERTY_DIRTY, !this.dirty, this.dirty);
        }
    }

    private String delegationClassName;
    private String delegationConfiguration = "";

    @Override
    public String getDelegationClassName() {
        return delegationClassName;
    }

    @Override
    public String getDelegationConfiguration() {
        return delegationConfiguration;
    }

    @Override
    public String getDelegationType() {
        return HandlerArtifact.TASK_HANDLER;
    }

    @Override
    public void setDelegationClassName(String delegateClassName) {
        this.delegationClassName = delegateClassName;
    }

    @Override
    public void setDelegationConfiguration(String configuration) {
        delegationConfiguration = configuration;
    }

    private ProcessDefinition processDefinition;

    public ProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    public void setProcessDefinition(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
    }

    public ParamDefConfig getParamDefConfig() {
        return paramDefConfig;
    }

    public void setParamDefConfig(ParamDefConfig paramDefConfig) {
        this.paramDefConfig = paramDefConfig;
    }
}
