package ru.runa.wf.web.customtag;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import ru.runa.wfe.form.Interaction;

import com.google.common.base.Charsets;

public class StartFormBuilder implements ru.runa.wf.web.StartFormBuilder {

    @Override
    public String build(Subject subject, Long definitionId, PageContext pageContext, Interaction interaction) throws Exception {
        FormParser parser = new FormParser(subject, pageContext, interaction, definitionId, null);
        byte[] formBytes = parser.getParsedFormBytes();
        return new String(formBytes, Charsets.UTF_8);
    }

}
