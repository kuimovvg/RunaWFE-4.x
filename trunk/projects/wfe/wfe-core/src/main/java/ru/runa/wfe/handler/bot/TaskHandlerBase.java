package ru.runa.wfe.handler.bot;

import com.google.common.base.Charsets;

public abstract class TaskHandlerBase implements ITaskHandler {
    private String configuration;

    @Override
    public String getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(byte[] config) throws Exception {
        this.configuration = new String(config, Charsets.UTF_8);
    }

}
