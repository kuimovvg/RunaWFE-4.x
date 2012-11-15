package ru.runa.gpd.lang.model;

public interface Delegable {
    public static final String ACTION_HANDLER = "actionHandler";
    public static final String DECISION_HANDLER = "decisionHandler";
    public static final String ASSIGNMENT_HANDLER = "assignmentHandler";
    
    public String getDelegationClassName();

    public void setDelegationClassName(String delegateClassName);

    public String getDelegationConfiguration();

    public void setDelegationConfiguration(String configuration);

    public String getDelegationType();
}
