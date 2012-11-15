package ru.runa.gpd.wfe;

public interface IConnector {

    public boolean isConfigured();

    public boolean connect() throws Exception;

    public void disconnect() throws Exception;

}
