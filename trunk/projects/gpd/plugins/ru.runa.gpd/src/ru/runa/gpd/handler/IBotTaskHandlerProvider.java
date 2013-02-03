package ru.runa.gpd.handler;

import java.util.Map;

public interface IBotTaskHandlerProvider {
    public String showConfigurationDialog(String delegationConfiguration, Map<String, String> variables);
}
