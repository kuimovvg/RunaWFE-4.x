package ru.runa.wf.web.ftl;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import ru.runa.InternalApplicationException;
import ru.runa.af.AuthenticationException;
import ru.runa.commons.ftl.FormHashModel;
import ru.runa.wf.form.Interaction;
import ru.runa.wf.web.html.StartFormBuilder;
import ru.runa.wf.web.html.WorkflowFormProcessingException;

public class FreemarkerStartFormBuilder extends BaseTaskFormBuilder implements StartFormBuilder {

    @Override
    public String build(Subject subject, Long definitionId, PageContext pageContext, Interaction interaction) throws AuthenticationException,
            WorkflowFormProcessingException {
        try {
            FormHashModel model = new FormHashModel(subject, pageContext, interaction.getDefaultVariableValues());
            return build(interaction, model, definitionId);
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

}
