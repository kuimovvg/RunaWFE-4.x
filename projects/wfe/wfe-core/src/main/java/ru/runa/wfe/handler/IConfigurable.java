package ru.runa.wfe.handler;

import ru.runa.wfe.ConfigurationException;

public interface IConfigurable {

    public void setConfiguration(String configuration) throws ConfigurationException;

}
