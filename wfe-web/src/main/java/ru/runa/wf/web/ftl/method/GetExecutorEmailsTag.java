package ru.runa.wf.web.ftl.method;

import java.util.List;

import ru.runa.wfe.commons.email.EmailUtils;
import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.user.Executor;

public class GetExecutorEmailsTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws Exception {
        Executor executor = getParameterAs(Executor.class, 0);
        List<String> emails = EmailUtils.getEmails(executor);
        log.debug(emails);
        return EmailUtils.concatenateEmails(emails);
    }

}
