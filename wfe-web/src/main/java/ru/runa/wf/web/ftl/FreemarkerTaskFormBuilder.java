package ru.runa.wf.web.ftl;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import ru.runa.common.web.StrutsWebHelper;
import ru.runa.service.client.DelegateProcessVariableProvider;
import ru.runa.wf.web.TaskFormBuilder;
import ru.runa.wfe.commons.ftl.FormHashModel;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.task.dto.WfTask;

public class FreemarkerTaskFormBuilder extends BaseTaskFormBuilder implements TaskFormBuilder {

    @Override
    public String build(Subject subject, PageContext pageContext, Interaction interaction, WfTask task) throws Exception {
        FormHashModel model = new FormHashModel(subject, new DelegateProcessVariableProvider(subject, task.getProcessId()), new StrutsWebHelper(
                pageContext));
        return build(interaction, model, task.getDefinitionId());
    }

}
