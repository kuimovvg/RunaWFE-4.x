package ru.runa.wf.web.quick;

import javax.servlet.jsp.PageContext;

import ru.runa.wf.web.StartFormBuilder;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.service.client.DelegateDefinitionVariableProvider;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.MapDelegableVariableProvider;

public class QuickFormStartBuilder extends BaseQuickFormBuilder implements StartFormBuilder {

    @Override
    public String build(User user, Long definitionId, PageContext pageContext, Interaction interaction) {
        IVariableProvider variableProvider = new MapDelegableVariableProvider(interaction.getDefaultVariableValues(),
                new DelegateDefinitionVariableProvider(user, definitionId));

        return build(user, pageContext, interaction, definitionId, variableProvider);
    }
}
