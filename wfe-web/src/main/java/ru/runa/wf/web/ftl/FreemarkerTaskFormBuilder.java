package ru.runa.wf.web.ftl;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import ru.runa.common.web.StrutsWebHelper;
import ru.runa.service.client.DelegateProcessVariableProvider;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.wf.web.FormProcessingException;
import ru.runa.wf.web.html.TaskFormBuilder;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ftl.FormHashModel;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.task.dto.WfTask;

public class FreemarkerTaskFormBuilder extends BaseTaskFormBuilder implements TaskFormBuilder {

    @Override
    public String build(Subject subject, Long taskId, PageContext pageContext, Interaction interaction) throws AuthenticationException,
            FormProcessingException {
        try {
            WfTask task = DelegateFactory.getExecutionService().getTask(subject, taskId);
            FormHashModel model = new FormHashModel(subject, pageContext, new DelegateProcessVariableProvider(subject, task.getProcessId()),
                    StrutsWebHelper.INSTANCE);
            return build(interaction, model, task.getProcessDefinitionId());
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

}
