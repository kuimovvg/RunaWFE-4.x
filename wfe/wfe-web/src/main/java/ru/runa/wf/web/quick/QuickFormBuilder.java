package ru.runa.wf.web.quick;

import javax.servlet.jsp.PageContext;

import ru.runa.wf.web.TaskFormBuilder;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.service.client.DelegateProcessVariableProvider;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

public class QuickFormBuilder extends BaseQuickFormBuilder implements TaskFormBuilder {

    @Override
    public String build(User user, PageContext pageContext, Interaction interaction, WfTask task) {
        IVariableProvider variableProvider = new DelegateProcessVariableProvider(user, task.getProcessId());
        return build(user, pageContext, interaction, task.getDefinitionId(), variableProvider);
    }

}
