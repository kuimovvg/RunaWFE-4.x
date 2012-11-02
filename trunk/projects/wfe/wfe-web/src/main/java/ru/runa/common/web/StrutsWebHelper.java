package ru.runa.common.web;

import java.util.Map;

import javax.servlet.jsp.PageContext;

import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.commons.web.WebHelper;

public class StrutsWebHelper implements WebHelper {
    public static final StrutsWebHelper INSTANCE = new StrutsWebHelper();

    private StrutsWebHelper() {
    }

    @Override
    public String getUrl(String relativeUrl, PageContext pageContext, PortletUrlType portletUrlType) {
        return Commons.getUrl(relativeUrl, pageContext, portletUrlType);
    }

    @Override
    public String getActionUrl(String relativeUrl, Map<String, ? extends Object> params, PageContext pageContext, PortletUrlType portletUrlType) {
        return Commons.getActionUrl(relativeUrl, params, pageContext, portletUrlType);
    }
}
