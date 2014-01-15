package ru.runa.wf.web.ftl;

import ru.runa.common.web.StrutsWebHelper;
import ru.runa.wf.web.TaskFormBuilder;
import ru.runa.wfe.commons.ftl.FormHashModel;
import ru.runa.wfe.commons.ftl.FreemarkerProcessor;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Charsets;

public class FtlFormBuilder extends TaskFormBuilder {

    @Override
    protected String buildForm(IVariableProvider variableProvider) {
        String template = new String(interaction.getFormData(), Charsets.UTF_8);
        return processFreemarkerTemplate(template, variableProvider);
    }

    protected String processFreemarkerTemplate(String template, IVariableProvider variableProvider) {
        FormHashModel model = new FormHashModel(user, variableProvider, new StrutsWebHelper(pageContext));
        model.put("interaction", interaction);
        // String header =
        // WebResources.getResources().getStringProperty("task.form.header");
        // if (header != null) {
        // header = FreemarkerProcessor.process(header, model);
        // formTemplate = header + "\n" + formTemplate;
        // }
        return FreemarkerProcessor.process(template, model);
    }
}
