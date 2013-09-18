package ru.runa.notifier.util;

public enum AppServerType {
    JBOSS4("http://${server.name}:${server.port}/runawfe-wfe-service-${server.version}/SERVICE_NAMEServiceBean?wsdl"),
    //
    JBOSS7("http://${server.name}:${server.port}/wfe-service-${server.version}/SERVICE_NAMEWebService/SERVICE_NAMEAPI?wsdl");

    private final String urlPattern;

    private AppServerType(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public String getUrlPattern() {
        return urlPattern;
    }
}
