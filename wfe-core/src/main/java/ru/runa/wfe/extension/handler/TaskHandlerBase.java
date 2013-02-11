package ru.runa.wfe.extension.handler;


import ru.runa.wfe.extension.Configurable;
import ru.runa.wfe.extension.TaskHandler;

import com.google.common.base.Charsets;

public abstract class TaskHandlerBase implements TaskHandler, Configurable {
    private String configuration;

    @Override
    public final void setConfiguration(byte[] config) throws Exception {
        this.configuration = new String(config, Charsets.UTF_8);
        setConfiguration(configuration);
    }

    @Override
    public String getConfiguration() {
        return configuration;
    }

}
