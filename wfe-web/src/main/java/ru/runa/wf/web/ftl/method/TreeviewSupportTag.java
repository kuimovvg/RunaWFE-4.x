package ru.runa.wf.web.ftl.method;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONAware;

import ru.runa.common.web.Commons;
import ru.runa.wfe.commons.ftl.AjaxJsonFreemarkerTag;
import ru.runa.wfe.commons.web.PortletUrlType;

public class TreeviewSupportTag extends AjaxJsonFreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected JSONAware processAjaxRequest(HttpServletRequest request) throws Exception {
        return null;
    }

    @Override
    protected String renderRequest() throws Exception {
        StringBuffer html = new StringBuffer();
        if (webHelper.getPageContext() != null) {
            String css = Commons.getUrl("/css/jquery.treeview.css", webHelper.getPageContext(), PortletUrlType.Resource);
            html.append("<link rel='stylesheet' href='" + css + "'>\n");
            String js = Commons.getUrl("/js/jquery.treeview.js", webHelper.getPageContext(), PortletUrlType.Resource);
            html.append("<script type='text/javascript' src='" + js + "'>c=0;</script>\n");
        }
        return html.toString();
    }

}
