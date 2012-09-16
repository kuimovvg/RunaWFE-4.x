package ru.runa.commons.ftl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import ru.runa.commons.TypeConversionUtil;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

@SuppressWarnings("unchecked")
public abstract class FreemarkerTag implements TemplateMethodModelEx, Serializable {

    private static final long serialVersionUID = 1L;
    protected Subject subject;
    protected PageContext pageContext;
    protected Map<String, Object> variables;

    private List<TemplateModel> arguments;

    public void setPageContext(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    @SuppressWarnings("rawtypes")
    public final Object exec(List arguments) throws TemplateModelException {
        this.arguments = arguments;
        return executeTag();
    }

    protected void registerVariableHandler(String variableName) {
        pageContext.getSession().setAttribute(FtlTagVariableHandler.HANDLER_KEY_PREFIX + variableName, this);
    }

    protected abstract Object executeTag() throws TemplateModelException;

    protected <T> T getParameterAs(Class<T> clazz, int i) throws TemplateModelException {
        Object paramValue = null;
        if (i < arguments.size()) {
            paramValue = BeansWrapper.getDefaultInstance().unwrap(arguments.get(i));
        }
        return TypeConversionUtil.convertTo(paramValue, clazz);
    }

    protected <T> T getVariableAs(Class<T> clazz, String varName, boolean allowNullValue) throws TemplateModelException {
        Object variable = variables.get(varName);
        if (variable == null) {
            if (allowNullValue) {
                return null;
            }
            throw new TemplateModelException("Variable '" + varName + "' is not defined.");
        }
        return TypeConversionUtil.convertTo(variable, clazz);
    }
}
