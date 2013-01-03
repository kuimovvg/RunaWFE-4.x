package ru.runa.wf.web.customtag;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.task.dto.WfTask;

import com.google.common.base.Charsets;

public class TaskFormBuilder implements ru.runa.wf.web.TaskFormBuilder {

    @Override
    public String build(Subject subject, PageContext pageContext, Interaction interaction, WfTask task) throws Exception {
        FormParser parser = new FormParser(subject, pageContext, interaction, null, task);
        byte[] formBytes = parser.getParsedFormBytes();
        return new String(formBytes, Charsets.UTF_8);
    }

}
