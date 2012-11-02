package ru.runa.wf.web.ftl.method;

import java.util.HashMap;

import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;

import ru.runa.common.web.Commons;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.var.FileVariable;

import com.google.common.collect.Maps;

import freemarker.template.TemplateModelException;

public class DownloadFileTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        String varName = getParameterAs(String.class, 0);
        FileVariable var = getVariableAs(FileVariable.class, varName, true);
        if (pageContext == null || var == null) {
            return "";
        }

        String pricessInstanceIdParam = pageContext.getRequest().getParameter("id");
        if (pricessInstanceIdParam == null) {
            throw new InternalApplicationException("taskIdParam was not passed correctly to DownloadFileTag");
        }

        A ahref = new A();
        ahref.addElement(new StringElement(var.getName()));

        HashMap<String, Object> parameters = Maps.newHashMap();
        parameters.put("id", pricessInstanceIdParam);
        parameters.put("variableName", varName);
        ahref.setHref(Commons.getActionUrl("/variableDownloader", parameters, pageContext, PortletUrlType.Render));
        return ahref.toString();
    }

}
