package ru.runa.wf.web.ftl;

import java.util.Map;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import ru.runa.InternalApplicationException;
import ru.runa.af.AuthenticationException;
import ru.runa.commons.ftl.FormHashModel;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.form.Interaction;
import ru.runa.wf.service.ExecutionService;
import ru.runa.wf.web.html.TaskFormBuilder;
import ru.runa.wf.web.html.WorkflowFormProcessingException;

public class FreemarkerTaskFormBuilder extends BaseTaskFormBuilder implements TaskFormBuilder {

    @Override
    public String build(Subject subject, Long taskId, String taskName, PageContext pageContext, Interaction interaction) throws AuthenticationException,
            WorkflowFormProcessingException {
        try {
            ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
            Map<String, Object> variableValues = executionService.getVariables(subject, taskId);
            FormHashModel model = new FormHashModel(subject, pageContext, variableValues);
            Long definitionId = executionService.getTask(subject, taskId).getProcessDefinitionId();
            return build(interaction, model, definitionId);
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

}
