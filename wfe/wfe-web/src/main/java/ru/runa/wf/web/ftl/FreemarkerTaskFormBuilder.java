package ru.runa.wf.web.ftl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import ru.runa.common.web.StrutsWebHelper;
import ru.runa.wf.web.FormUtils;
import ru.runa.wf.web.TaskFormBuilder;
import ru.runa.wfe.commons.ftl.FormHashModel;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.service.client.DelegateProcessVariableProvider;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.MapDelegableVariableProvider;

public class FreemarkerTaskFormBuilder extends BaseTaskFormBuilder implements TaskFormBuilder {

    @Override
    public String build(User user, PageContext pageContext, Interaction interaction, WfTask task) {
        IVariableProvider variableProvider = new DelegateProcessVariableProvider(user, task.getProcessId());
        Map<String, Object> userDefinedVariables = FormUtils.getUserFormInputVariables((HttpServletRequest) pageContext.getRequest(), interaction);
        if (userDefinedVariables != null) {
            variableProvider = new MapDelegableVariableProvider(userDefinedVariables, variableProvider);
        }
        FormHashModel model = new FormHashModel(user, variableProvider, new StrutsWebHelper(pageContext));
        return build(interaction, model, task.getDefinitionId());
    }

}
