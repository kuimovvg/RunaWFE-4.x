package ru.runa.wf.web.customtag;

import javax.servlet.jsp.PageContext;

import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;

public class TaskFormBuilder implements ru.runa.wf.web.TaskFormBuilder {

    @Override
    public String build(User user, PageContext pageContext, Interaction interaction, WfTask task) {
        return new FormParser(user, pageContext, interaction, null, task).getParsedFormBytes();
    }

}
