package ru.runa.af.web;

import javax.servlet.jsp.PageContext;

import ru.runa.common.web.Messages;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.EscalationGroup;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.SystemExecutors;
import ru.runa.wfe.user.TemporaryGroup;

public class ExecutorNameConverter {

    public static String getName(Executor executor, PageContext pageContext) {
        String result;
        if (executor == null) {
            result = "";
        } else if (Actor.UNAUTHORIZED_ACTOR.getName().equals(executor.getName())) {
            result = Messages.getMessage(ru.runa.common.WebResources.UNAUTHORIZED_EXECUTOR_NAME, pageContext);
        } else if (executor.getName().startsWith(EscalationGroup.GROUP_PREFIX)) {
            result = Messages.getMessage(Messages.ESCALATION_GROUP_NAME, pageContext);
        } else if (executor.getName().startsWith(TemporaryGroup.GROUP_PREFIX)) {
            result = Messages.getMessage(Messages.DYNAMIC_GROUP_NAME, pageContext);
        } else if (executor.getName().equals(SystemExecutors.PROCESS_STARTER_NAME)) {
            result = Messages.getMessage(Messages.PROCESS_STARTER_NAME, pageContext);
        } else {
            result = executor.getName();
        }
        return result;
    }

}
