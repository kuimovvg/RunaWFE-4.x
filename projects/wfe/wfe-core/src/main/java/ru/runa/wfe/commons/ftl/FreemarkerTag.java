package ru.runa.wfe.commons.ftl;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.AbstractVariableProvider;
import ru.runa.wfe.var.IVariableProvider;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

@SuppressWarnings("unchecked")
public abstract class FreemarkerTag implements TemplateMethodModelEx, Serializable {
    private static final long serialVersionUID = 1L;
    public static final String TARGET_PROCESS_PREFIX = "TargetProcess";
    protected User user;
    protected IVariableProvider variableProvider;
    protected WebHelper webHelper;
    private List<TemplateModel> arguments;
    private boolean targetProcess;

    public void init(User user, WebHelper webHelper, IVariableProvider variableProvider, boolean targetProcess) {
        this.user = user;
        this.webHelper = webHelper;
        this.variableProvider = variableProvider;
        this.targetProcess = targetProcess;
    }

    public void initChained(FreemarkerTag parent) {
        init(parent.user, parent.webHelper, parent.variableProvider, false);
        arguments = parent.arguments;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public final Object exec(List arguments) throws TemplateModelException {
        try {
            this.arguments = arguments;
            if (targetProcess) {
                Long targetProcessId = getParameterVariableValueNotNull(Long.class, 0);
                this.arguments.remove(0);
                this.variableProvider = ((AbstractVariableProvider) variableProvider).getSameProvider(targetProcessId);
            }
            return executeTag();
        } catch (Throwable th) {
            LogFactory.getLog(getClass()).error(arguments.toString(), th);
            return "<div style=\"background-color: #ffb0b0; border: 1px solid red; padding: 3px;\">" + th.getMessage() + "</div>";
        }
    }

    protected void registerVariableHandler(String variableName) {
        webHelper.getSession().setAttribute(FtlTagVariableHandler.HANDLER_KEY_PREFIX + variableName, this);
    }

    protected abstract Object executeTag() throws Exception;

    protected String getParameterAsString(int i) throws TemplateModelException {
        return getParameterAs(String.class, i);
    }

    protected <T> T getParameterAs(Class<T> clazz, int i) throws TemplateModelException {
        Object paramValue = null;
        if (i < arguments.size()) {
            paramValue = BeansWrapper.getDefaultInstance().unwrap(arguments.get(i));
        }
        return TypeConversionUtil.convertTo(clazz, paramValue);
    }

    protected <T> T getParameterVariableValueNotNull(Class<T> clazz, int i) throws TemplateModelException {
        String variableName = getParameterAsString(i);
        return variableProvider.getValueNotNull(clazz, variableName);
    }

    protected <T> T getParameterVariableValue(Class<T> clazz, int i, T defaultValue) throws TemplateModelException {
        String variableName = getParameterAsString(i);
        T value = variableProvider.getValue(clazz, variableName);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

}
