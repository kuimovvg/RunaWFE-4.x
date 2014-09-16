package ru.runa.wfe.commons.ftl;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.AbstractVariableProvider;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

@SuppressWarnings("unchecked")
public abstract class FreemarkerTag implements TemplateMethodModelEx, Serializable {
    private static final long serialVersionUID = 1L;
    protected Log log = LogFactory.getLog(getClass());
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
    public final Object exec(List arguments) {
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

    protected void registerVariablePostProcessor(String variableName) {
        Preconditions.checkArgument(this instanceof FtlTagVariableSubmissionPostProcessor, "not a FtlTagVariableSubmissionPostProcessor instance");
        webHelper.getRequest().getSession().setAttribute(FtlTagVariableSubmissionPostProcessor.KEY_PREFIX + variableName, this);
    }

    protected void registerVariableHandler(String variableName) {
        Preconditions.checkArgument(this instanceof FtlTagVariableSubmissionHandler, "not a FtlTagVariableSubmissionHandler instance");
        webHelper.getRequest().getSession().setAttribute(FtlTagVariableSubmissionHandler.KEY_PREFIX + variableName, this);
    }

    protected abstract Object executeTag() throws Exception;

    protected String getParameterAsString(int i) {
        return getParameterAs(String.class, i);
    }

    protected <T> T getParameterAs(Class<T> clazz, int i) {
        Object paramValue = null;
        if (i < arguments.size()) {
            try {
                paramValue = BeansWrapper.getDefaultInstance().unwrap(arguments.get(i));
            } catch (TemplateModelException e) {
                Throwables.propagate(e);
            }
        }
        return TypeConversionUtil.convertTo(clazz, paramValue);
    }

    protected <T> T getParameterVariableValueNotNull(Class<T> clazz, int i) {
        String variableName = getParameterAsString(i);
        return variableProvider.getValueNotNull(clazz, variableName);
    }

    protected <T> T getParameterVariableValue(Class<T> clazz, int i, T defaultValue) {
        String variableName = getParameterAsString(i);
        T value = variableProvider.getValue(clazz, variableName);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

}
