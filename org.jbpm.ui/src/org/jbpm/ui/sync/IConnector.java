package org.jbpm.ui.sync;

public interface IConnector {

    public boolean isConfigured();

    public boolean connect() throws Exception;

    public void disconnect() throws Exception;

}
