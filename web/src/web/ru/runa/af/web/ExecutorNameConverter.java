package ru.runa.af.web;

import javax.servlet.jsp.PageContext;

import ru.runa.af.Executor;
import ru.runa.af.Group;
import ru.runa.af.SystemExecutors;
import ru.runa.common.web.Messages;

public class ExecutorNameConverter {

    public static String getName(Executor executor, PageContext pageContext) {
        if (executor == null) {
            throw new NullPointerException("executor");
        }
        String result = executor.getName();
        if (result.startsWith(Group.TEMPORARY_GROUP_PREFIX)) {
            result = Messages.getMessage(Messages.DYNAMIC_GROUP_NAME, pageContext);
        }
        if (result.equals(SystemExecutors.PROCESS_STARTER_NAME)) {
            result = Messages.getMessage(Messages.PROCESS_STARTER_NAME, pageContext);
        }
        return result;
    }
}
