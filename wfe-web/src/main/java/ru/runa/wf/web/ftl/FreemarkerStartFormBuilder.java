package ru.runa.wf.web.ftl;

import java.util.List;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import ru.runa.common.web.StrutsWebHelper;
import ru.runa.service.delegate.Delegates;
import ru.runa.wf.web.StartFormBuilder;
import ru.runa.wfe.commons.ftl.FormHashModel;
import ru.runa.wfe.definition.DefinitionVariableProvider;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.VariableDefinition;

public class FreemarkerStartFormBuilder extends BaseTaskFormBuilder implements StartFormBuilder {

    @Override
    public String build(Subject subject, Long definitionId, PageContext pageContext, Interaction interaction) throws Exception {
        List<VariableDefinition> variableDefinitions = Delegates.getDefinitionService().getVariables(subject, definitionId);
        FormHashModel model = new FormHashModel(subject, new MapDelegableVariableProvider(interaction.getDefaultVariableValues(),
                new DefinitionVariableProvider(variableDefinitions)), new StrutsWebHelper(pageContext));
        return build(interaction, model, definitionId);
    }

}
