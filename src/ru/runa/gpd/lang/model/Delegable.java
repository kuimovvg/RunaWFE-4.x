package ru.runa.gpd.lang.model;

public interface Delegable {
    public String getDelegationClassName();

    public void setDelegationClassName(String delegateClassName);

    public String getDelegationConfiguration();

    public void setDelegationConfiguration(String configuration);

    public String getDelegationType();
}
