package ru.runa.notifier;

import java.net.MalformedURLException;
import java.net.URL;

import ru.runa.notifier.util.ResourcesManager;
import ru.runa.wfe.webservice.AuthenticationAPI;
import ru.runa.wfe.webservice.AuthenticationWebService;
import ru.runa.wfe.webservice.ExecutionAPI;
import ru.runa.wfe.webservice.ExecutionWebService;

public class WFEConnection {

    private static URL getUrl(String serviceName) {
        try {
            String url = ResourcesManager.getWebServiceUrl();
            url = url.replaceAll("SERVICE_NAME", serviceName);
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static AuthenticationAPI getAuthenticationAPI() {
        return new AuthenticationWebService(getUrl("Authentication")).getAuthenticationAPIPort();
    }

    public static ExecutionAPI getExecutionAPI() {
        return new ExecutionWebService(getUrl("Execution")).getExecutionAPIPort();
    }
}
