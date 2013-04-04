package ru.runa.alfresco;

import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Web services connection settings.
 * 
 * @author dofs
 */
public class WSConnectionSettings extends Settings {
    private static String endpointAddress;
    private static String systemLogin;
    private static String systemPassword;

    static {
        loadConnectionInfo();
    }

    private static void loadConnectionInfo() {
        try {
            Document document = getConfigDocument();
            Element root = document.getRootElement();

            Element connectionElement = root.element("connection");
            systemLogin = connectionElement.attributeValue("login");
            systemPassword = connectionElement.attributeValue("password");
            endpointAddress = connectionElement.attributeValue("endpoint");
        } catch (Throwable e) {
            log.error("Unable to load ws connection info", e);
        }
    }

    public static void setConnectionInfo(String hostname, int port, String login, String password) {
        systemLogin = login;
        systemPassword = password;
        endpointAddress = "http://" + hostname + ":" + port + "/alfresco/api";
    }

    public static String getEndpointAddress() {
        return endpointAddress;
    }

    public static String getSystemLogin() {
        return systemLogin;
    }

    public static String getSystemPassword() {
        return systemPassword;
    }

    public static String getAlfBaseUrl() {
        // 'api' removal at the end
        return endpointAddress.substring(0, endpointAddress.length() - 3);
    }

}
