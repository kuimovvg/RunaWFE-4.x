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
package ru.runa.wfe.commons.ftl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.commons.web.WebHelper;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;

import freemarker.template.TemplateModelException;

public abstract class AjaxFreemarkerTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;
    private WebHelper webHelper;

    public void setWebHelper(WebHelper webHelper) {
        this.webHelper = webHelper;
    }

    @Override
    protected final Object executeTag() throws TemplateModelException {
        return renderRequest();
    }

    protected String getSavedValue(String varName) {
        return getSavedValue(String.class, varName);
    }

    @SuppressWarnings("unchecked")
    protected <T extends Object> T getSavedValue(Class<T> clazz, String varName) {
        Map<String, String[]> map = (Map<String, String[]>) pageContext.getRequest().getAttribute("UserDefinedVariables");
        Object o = null;
        if (map != null && map.containsKey(varName)) {
            if (clazz.isArray()) {
                o = map.get(varName);
            } else {
                o = map.get(varName)[0];
            }
        }
        return TypeConversionUtil.convertTo(o, clazz);
    }

    protected abstract String renderRequest() throws TemplateModelException;

    public void processAjaxRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
    }

    protected String exportExternalScript(String src) {
        if (pageContext == null || webHelper == null) {
            return "";
        }
        if (pageContext.getAttribute(src) == null) {
            pageContext.setAttribute(src, Boolean.TRUE);
            String url = webHelper.getUrl(src, pageContext, PortletUrlType.Resource);
            return "<script type=\"text/javascript\" src=\"" + url + "\"></script>";
        }
        return "";
    }

    protected String exportScriptCode(String javascript) {
        return "<script type=\"text/javascript\">" + javascript + "</script>";
    }

    protected String exportScript(String path, Map<String, String> substitutions) throws IOException {
        if (pageContext == null || webHelper == null) {
            return "";
        }
        InputStream is = ClassLoaderUtil.getResourceAsStream(path, getClass());
        if (is == null) {
            throw new NullPointerException("Script not found '" + path + "'");
        }
        String jsCode = new String(ByteStreams.toByteArray(is), Charsets.UTF_8);

        substitutions.put("jsonUrl", webHelper.getUrl("/form.fp?json=true", pageContext, PortletUrlType.Resource));
        for (String sKey : substitutions.keySet()) {
            String v = substitutions.get(sKey);
            jsCode = jsCode.replaceAll(Pattern.quote(sKey), Matcher.quoteReplacement(v));
        }
        if (pageContext.getAttribute(path) == null) {
            pageContext.setAttribute(path, Boolean.TRUE);
            return "<script type=\"text/javascript\">" + jsCode + "</script>";
        }
        return "";
    }
}
