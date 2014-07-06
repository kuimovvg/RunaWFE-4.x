package ru.runa.wfe.commons.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public interface WebHelper {

    String getMessage(String key);

    HttpServletRequest getRequest();

    String getUrl(String relativeUrl);

    String getActionUrl(String relativeUrl, Map<String, ? extends Object> params);

}
