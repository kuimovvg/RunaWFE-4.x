package ru.runa.wf.web.ftl;

import java.util.List;

import javax.servlet.jsp.PageContext;

import ru.runa.common.web.StrutsWebHelper;
import ru.runa.wf.web.StartFormBuilder;
import ru.runa.wfe.commons.ftl.FormHashModel;
import ru.runa.wfe.definition.DefinitionVariableProvider;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.VariableDefinition;

public class FreemarkerStartFormBuilder extends BaseTaskFormBuilder implements StartFormBuilder {

    @Override
    public String build(User user, Long definitionId, PageContext pageContext, Interaction interaction) {
        List<VariableDefinition> variableDefinitions = Delegates.getDefinitionService().getVariables(user, definitionId);
        FormHashModel model = new FormHashModel(user, new MapDelegableVariableProvider(interaction.getDefaultVariableValues(),
                new DefinitionVariableProvider(variableDefinitions)), new StrutsWebHelper(pageContext));
        return build(interaction, model, definitionId);
    }

}
