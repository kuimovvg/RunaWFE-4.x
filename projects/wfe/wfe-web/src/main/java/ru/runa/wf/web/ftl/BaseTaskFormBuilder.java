package ru.runa.wf.web.ftl;

import java.util.List;

import ru.runa.common.WebResources;
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
            String out = FreemarkerProcessor.process(interaction.getFormDataNotNull(), model);
            List<String> requiredVariableNames = Lists.newArrayList();
            if (WebResources.isHighlightRequiredFields()) {
                requiredVariableNames = interaction.getRequiredVariableNames();
            }
            byte[] b = HTMLFormConverter.changeUrls(model.getPageContext(), definitionId, "form.ftl", out.getBytes(Charsets.UTF_8));
            return new String(HTMLFormConverter.setInputValues(model.getPageContext(), b, model.getVariableProvider(), requiredVariableNames),
                    Charsets.UTF_8);
        } catch (Exception e) {
            throw new FormProcessingException(e);
        }
    }

}
