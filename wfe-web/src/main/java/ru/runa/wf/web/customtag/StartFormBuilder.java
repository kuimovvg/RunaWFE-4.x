package ru.runa.wf.web.customtag;

import javax.servlet.jsp.PageContext;

import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.user.User;

public class StartFormBuilder implements ru.runa.wf.web.StartFormBuilder {

    @Override
    public String build(User user, Long definitionId, PageContext pageContext, Interaction interaction) {
        return new FormParser(user, pageContext, interaction, definitionId, null).getParsedFormBytes();
    }

}
