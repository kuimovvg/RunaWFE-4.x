package ru.runa.wf.web.ftl;

import javax.servlet.jsp.PageContext;

import ru.runa.common.WebResources;
import ru.runa.common.web.Messages;
import ru.runa.wf.web.FormPresentationUtils;
import ru.runa.wfe.commons.ftl.FormHashModel;
import ru.runa.wfe.commons.ftl.FreemarkerProcessor;
import ru.runa.wfe.form.Interaction;

import com.google.common.base.Charsets;

public abstract class BaseTaskFormBuilder {

    public String build(Interaction interaction, FormHashModel model, Long definitionId) {
        if (interaction.hasForm()) {
            String formTemplate = new String(interaction.getFormData(), Charsets.UTF_8);
            // TODO temporary
            String header = WebResources.getResources().getStringProperty("task.form.header");
            if (header != null) {
                model.put("interaction", interaction);
                header = FreemarkerProcessor.process(header, model);
                formTemplate = header + "\n" + formTemplate;
            }
            String out = FreemarkerProcessor.process(formTemplate, model);
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
