package ru.runa.wfe.commons.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import ru.runa.wfe.commons.ftl.AjaxFreemarkerTag;

public interface WebHelper {

    PageContext getPageContext();

    HttpSession getSession();

    HttpServletRequest getRequest();

    String getUrl(String relativeUrl);

    String getActionUrl(String relativeUrl, Map<String, ? extends Object> params);

    void removeAllTags();

    void setTag(String key, AjaxFreemarkerTag tag);
}
