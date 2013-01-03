package ru.runa.wf.web.ftl;

import java.util.List;

import javax.servlet.jsp.PageContext;

import ru.runa.common.WebResources;
import ru.runa.common.web.Messages;
import ru.runa.wf.web.FormProcessingException;
import ru.runa.wf.web.tag.HTMLFormConverter;
import ru.runa.wfe.commons.ftl.FormHashModel;
import ru.runa.wfe.commons.ftl.FreemarkerProcessor;
import ru.runa.wfe.form.Interaction;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

abstract class BaseTaskFormBuilder {

    public String build(Interaction interaction, FormHashModel model, Long definitionId) throws FormProcessingException {
        try {
            if (interaction.hasForm()) {
                String out = FreemarkerProcessor.process(interaction.getFormData(), model);
                List<String> requiredVariableNames = Lists.newArrayList();
                if (WebResources.isHighlightRequiredFields()) {
                    requiredVariableNames = interaction.getRequiredVariableNames();
                }
                PageContext pageContext = null;
                if (model.getWebHelper() != null) {
                    pageContext = model.getWebHelper().getPageContext();
                }
                byte[] b = HTMLFormConverter.changeUrls(pageContext, definitionId, "form.ftl", out.getBytes(Charsets.UTF_8));
                return new String(HTMLFormConverter.setInputValues(b, model.getVariableProvider(), requiredVariableNames), Charsets.UTF_8);
            } else {
                String message = "Task form is not defined";
                if (model.getWebHelper() != null) {
                    message = Messages.getMessage("task.form.not.defined.error", model.getWebHelper().getPageContext());
                }
                return message;
            }
        } catch (Exception e) {
            throw new FormProcessingException(e);
        }
    }

}
