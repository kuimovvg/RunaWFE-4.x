package ru.runa.gpd.lang.model;

public interface Synchronizable {
    public boolean isAsync();

    public void setAsync(boolean async);
}
