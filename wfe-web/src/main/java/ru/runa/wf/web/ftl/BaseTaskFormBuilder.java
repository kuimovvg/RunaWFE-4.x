package ru.runa.wf.web.ftl;

import javax.servlet.jsp.PageContext;

import ru.runa.common.web.Messages;
import ru.runa.wf.web.FormPresentationUtils;
import ru.runa.wfe.commons.ftl.FormHashModel;
import ru.runa.wfe.commons.ftl.FreemarkerProcessor;
import ru.runa.wfe.form.Interaction;

public abstract class BaseTaskFormBuilder {

    public String build(Interaction interaction, FormHashModel model, Long definitionId) {
        if (interaction.hasForm()) {
            String out = FreemarkerProcessor.process(interaction.getFormData(), model);
            PageContext pageContext = null;
            if (model.getWebHelper() != null) {
                pageContext = model.getWebHelper().getPageContext();
            }
            return FormPresentationUtils.adjustForm(pageContext, definitionId, out, model.getVariableProvider(),
                    interaction.getRequiredVariableNames());
        } else {
            String message = "Task form is not defined";
            if (model.getWebHelper() != null) {
                message = Messages.getMessage("task.form.not.defined.error", model.getWebHelper().getPageContext());
            }
            return message;
        }
    }

}
