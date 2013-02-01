package ru.runa.wf.web.ftl;

import javax.servlet.jsp.PageContext;

import ru.runa.common.web.StrutsWebHelper;
import ru.runa.service.client.DelegateProcessVariableProvider;
import ru.runa.wf.web.TaskFormBuilder;
import ru.runa.wfe.commons.ftl.FormHashModel;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;

public class FreemarkerTaskFormBuilder extends BaseTaskFormBuilder implements TaskFormBuilder {

    @Override
    public String build(User user, PageContext pageContext, Interaction interaction, WfTask task) {
        FormHashModel model = new FormHashModel(user, new DelegateProcessVariableProvider(user, task.getProcessId()),
                new StrutsWebHelper(pageContext));
        return build(interaction, model, task.getDefinitionId());
    }

}
