/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.common.web;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.portals.bridges.struts.PortletServlet;
import org.apache.portals.bridges.struts.StrutsPortletURL;
import org.apache.struts.Globals;
import org.apache.struts.action.ActionForward;
import org.apache.struts.taglib.TagUtils;
import org.apache.struts.util.MessageResourcesFactory;

import ru.runa.InternalApplicationException;
import ru.runa.commons.Loader;

import com.google.common.base.Charsets;

/**
 * Created on 20.08.2004
 * 
 */
public class Commons {

    private static final TagUtils tagUtils = TagUtils.getInstance();

    public static enum PortletUrl {
        Render, Action, Resource
    }

    protected Commons() {
    }

    /**
     * Add parameters to provided ActionForward
     * 
     * @param forward
     *            ActionForward to add parameters to
     * @param parameters
     *            table of parameters
     * @return ActionForward with added parameters to URL
     */
    public static ActionForward forward(ActionForward forward, Map parameters) {
        ActionForward newActionForward = new ActionForward(forward);
        newActionForward.setPath(appendParams(forward.getPath(), parameters));
        return newActionForward;
    }

    /**
     * Add parameter to provided ActionForward
     * 
     * @param forward
     *            ActionForward to add parameters to
     * @param parameter
     *            parameter name
     * @param value
     *            parameter value
     * @return ActionForward with added parameters to URL
     */
    public static ActionForward forward(ActionForward forward, String parameter, Object value) {
        ActionForward newActionForward = new ActionForward(forward);
        newActionForward.setPath(appendParam(forward.getPath(), parameter, value));
        return newActionForward;
    }

    public static String getActionUrl(String actionMapping, PageContext pageContext, PortletUrl portletUrlType) {
        return getActionUrl(actionMapping, null, pageContext, portletUrlType);
    }

    public static String getActionUrl(String actionMapping, String parameterName, Object parameterValue, PageContext pageContext,
            PortletUrl portletUrlType) {
        Map<String, Object> parameterMap = new HashMap<String, Object>(1);
        parameterMap.put(parameterName, parameterValue);
        return getActionUrl(actionMapping, parameterMap, pageContext, portletUrlType);
    }

    public static String getActionUrl(String actionMapping, Map parameterMap, PageContext pageContext, PortletUrl portletUrlType) {
        try {
            String url = tagUtils.computeURL(pageContext, null, null, null, actionMapping, null, parameterMap, null, false, false);
            url = applyPortlet(url, pageContext, portletUrlType);
            return url;
        } catch (MalformedURLException e) {
            throw new InternalApplicationException(e);
        }
    }

    public static String getActionUrl(String actionMapping, Map parameterMap, String anchor, PageContext pageContext, PortletUrl portletUrlType) {
        try {
            String url = tagUtils.computeURL(pageContext, null, null, null, actionMapping, null, parameterMap, anchor, false, false);
            url = applyPortlet(url, pageContext, portletUrlType);
            return url;
        } catch (MalformedURLException e) {
            throw new InternalApplicationException(e);
        }
    }

    public static String getForwardUrl(String forward, PageContext pageContext, PortletUrl portletUrlType) {
        return getForwardUrl(forward, null, pageContext, portletUrlType);
    }

    public static String getForwardUrl(String forward, String parameterName, Object parameterValue, PageContext pageContext, PortletUrl portletUrlType) {
        Map<String, Object> parameterMap = new HashMap<String, Object>(1);
        parameterMap.put(parameterName, parameterValue);
        return getForwardUrl(forward, parameterMap, pageContext, portletUrlType);
    }

    public static String getForwardUrl(String forward, Map parameterMap, PageContext pageContext, PortletUrl portletUrlType) {
        try {
            String url = tagUtils.computeURL(pageContext, forward, null, null, null, null, parameterMap, null, false, false);
            url = applyPortlet(url, pageContext, portletUrlType);
            return url;
        } catch (MalformedURLException e) {
            throw new InternalApplicationException(e);
        }
    }

    public static String getUrl(String href, PageContext pageContext, PortletUrl portletUrlType) {
        return getUrl(href, null, pageContext, portletUrlType);
    }

    public static String getUrl(String href, Map parameterMap, PageContext pageContext, PortletUrl portletUrlType) {
        try {
            String url = tagUtils.computeURL(pageContext, null, null, href, null, null, parameterMap, null, false, false);
            url = applyPortlet(url, pageContext, portletUrlType);
            return url;
        } catch (MalformedURLException e) {
            throw new InternalApplicationException(e);
        }
    }

    public static String getMessage(String key, PageContext pageContext, Object arg) throws JspException {
        String result = tagUtils.message(pageContext, Globals.MESSAGES_KEY, Globals.LOCALE_KEY, key, new Object[] { arg });
        if (result == null) {
            result = MessageResourcesFactory.createFactory().createResources("struts_msgs").getMessage(getLocale(pageContext), key,
                    new Object[] { arg });
        }
        return result;
    }

    public static String getMessage(String key, PageContext pageContext, Object[] args) throws JspException {
        String result = tagUtils.message(pageContext, Globals.MESSAGES_KEY, Globals.LOCALE_KEY, key, args);
        if (result == null) {
            result = MessageResourcesFactory.createFactory().createResources("struts_msgs").getMessage(getLocale(pageContext), key, args);
        }
        return result;
    }

    public static String getMessage(String key, PageContext pageContext) throws JspException {
        String result = tagUtils.message(pageContext, Globals.MESSAGES_KEY, Globals.LOCALE_KEY, key);
        if (result == null) {
            result = MessageResourcesFactory.createFactory().createResources("struts_msgs").getMessage(getLocale(pageContext), key);
        }
        return result;
    }

    public static Locale getLocale(PageContext pageContext) {
        return tagUtils.getUserLocale(pageContext, Globals.LOCALE_KEY);
    }

    public static Object loadObject(String name, Object[] params) {
        try {
            return Loader.loadObject(name, params);
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

    // TODO Replace following three methods by something standard (like Struts tag utils for pageContext
    private static String appendParam(String url, String parameter, Object value) {
        StringBuilder newParams = new StringBuilder();
        newParams.append(encodeToUTF8(parameter)).append("=").append(encodeToUTF8(value.toString()));
        return appendParameterString(url, newParams);
    }

    private static String appendParams(String url, Map parameters) {
        // taken from ParamSupport from jakarta taglib-standard 1.1
        StringBuilder newParams = new StringBuilder();
        for (Iterator iter = parameters.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            newParams.append(encodeToUTF8(entry.getKey().toString())).append("=").append(encodeToUTF8(entry.getValue().toString()));
            if (iter.hasNext()) {
                newParams.append("&");
            }
        }
        return appendParameterString(url, newParams);
    }

    private static String appendParameterString(String url, StringBuilder newParams) {
        // taken from ParamSupport from jakarta taglib-standard 1.1
        if (newParams.length() > 0) {
            int questionMark = url.indexOf('?');
            if (questionMark == -1) {
                return (url + "?" + newParams);
            }
            StringBuilder workingUrl = new StringBuilder(url);
            workingUrl.insert(questionMark + 1, (newParams + "&"));
            return workingUrl.toString();
        }
        return url;
    }

    private static String encodeToUTF8(String string) {
        try {
            return URLEncoder.encode(string, Charsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new InternalApplicationException(e);
        }
    }

    private static String applyPortlet(String url, PageContext pageContext, PortletUrl portletUrlType) {
        if (PortletServlet.isPortletRequest(pageContext.getRequest()) && portletUrlType != PortletUrl.Resource) {
            url = portletUrlType.equals(PortletUrl.Render) ? StrutsPortletURL.createRenderURL(pageContext.getRequest(), url).toString()
                    : StrutsPortletURL.createActionURL(pageContext.getRequest(), url).toString();
        }
        return url;
    }

    public static Object getSessionAttribute(HttpSession session, String attributeName) {
        Object retVal = null;
        retVal = session.getAttribute(attributeName);
        if (retVal == null) {
            retVal = session.getAttribute("?" + attributeName);
        }
        if (retVal == null) {
            Enumeration<String> attributes = session.getAttributeNames();
            while (attributes.hasMoreElements()) {
                String attribute = attributes.nextElement();
                if (attribute.endsWith(attributeName)) {
                    retVal = session.getAttribute(attribute);
                    break;
                }
            }
        }
        return retVal;
    }
}
