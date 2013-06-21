package ru.runa.wf.web.customtag;

import javax.servlet.jsp.PageContext;

import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.user.User;

import com.google.common.base.Charsets;

public class StartFormBuilder implements ru.runa.wf.web.StartFormBuilder {

    @Override
    public String build(User user, Long definitionId, PageContext pageContext, Interaction interaction) {
        FormParser parser = new FormParser(user, pageContext, interaction, definitionId, null);
        byte[] formBytes = parser.getParsedFormBytes();
        return new String(formBytes, Charsets.UTF_8);
    }

}
