package ru.runa.gpd.lang.model;

import ru.runa.gpd.extension.handler.ParamDef;

public enum BotTaskType {
    /**
     * This bot task has a fixed configuration
     */
    SIMPLE,
    /**
     * This bot task has a fixed configuration in {@link ParamDef} xml format.
     */
    PARAMETERIZED,
    /**
     * This bot task has a dynamic configuration (in server there will be changes to real variables according to task binding parameters)
     */
    EXTENDED
}
