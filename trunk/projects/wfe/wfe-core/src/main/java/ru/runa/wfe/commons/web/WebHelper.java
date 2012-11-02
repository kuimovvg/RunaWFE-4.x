package ru.runa.wfe.commons.web;

import java.util.Map;

import javax.servlet.jsp.PageContext;

public interface WebHelper {

    String getUrl(String relativeUrl, PageContext pageContext, PortletUrlType portletUrlType);

    String getActionUrl(String relativeUrl, Map<String, ? extends Object> params, PageContext pageContext, PortletUrlType portletUrlType);
}
