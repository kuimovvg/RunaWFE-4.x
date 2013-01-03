package ru.runa.common.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.commons.web.WebHelper;

public class StrutsWebHelper implements WebHelper {
    private final PageContext pageContext;

    public StrutsWebHelper(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    @Override
    public PageContext getPageContext() {
        return pageContext;
    }

    @Override
    public HttpSession getSession() {
        return pageContext.getSession();
    }

    @Override
    public HttpServletRequest getRequest() {
        return (HttpServletRequest) pageContext.getRequest();
    }

    @Override
    public String getUrl(String relativeUrl) {
        return Commons.getUrl(relativeUrl, pageContext, PortletUrlType.Resource);
    }

    @Override
    public String getActionUrl(String relativeUrl, Map<String, ? extends Object> params) {
        return Commons.getActionUrl(relativeUrl, params, pageContext, PortletUrlType.Render);
    }
}
