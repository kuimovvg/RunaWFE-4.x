package ru.runa.wfe.handler.bot;

import ru.runa.wfe.handler.Configurable;

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
