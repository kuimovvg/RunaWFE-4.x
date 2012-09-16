package ru.runa.wf.web.ftl;

import java.util.List;

import ru.runa.commons.ftl.FormHashModel;
import ru.runa.commons.ftl.FreemarkerProcessor;
import ru.runa.wf.form.Interaction;
import ru.runa.wf.web.Resources;
import ru.runa.wf.web.html.WorkflowFormProcessingException;
import ru.runa.wf.web.tag.HTMLFormConverter;

import com.google.common.base.Charsets;

abstract class BaseTaskFormBuilder {

    public String build(Interaction interaction, FormHashModel model, Long definitionId) throws WorkflowFormProcessingException {
        try {
            String out = FreemarkerProcessor.process(interaction.getFormData(), model);
            List<String> requiredVariableNames = null;
            if (Resources.highlightRequiredFields()) {
                requiredVariableNames = interaction.getRequiredVariableNames();
            }
            byte[] b = HTMLFormConverter.changeUrls(model.getPageContext(), definitionId, "form.ftl", out.getBytes(Charsets.UTF_8));
            return new String(HTMLFormConverter.setInputValues(model.getPageContext(), b, model.getVariables(), requiredVariableNames),
                    Charsets.UTF_8);
        } catch (Exception e) {
            throw new WorkflowFormProcessingException(e);
        }
    }

}
