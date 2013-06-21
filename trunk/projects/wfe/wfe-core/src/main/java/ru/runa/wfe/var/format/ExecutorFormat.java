package ru.runa.wfe.var.format;

import java.util.HashMap;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

import com.google.common.collect.Maps;

public class ExecutorFormat implements VariableFormat<Executor>, VariableDisplaySupport<Executor> {

    @Override
    public Executor parse(String[] source) throws Exception {
        return TypeConversionUtil.convertTo(Executor.class, source[0]);
    }

    @Override
    public String format(Executor object) {
        return object.getFullName();
    }

    @Override
    public String getHtml(User user, WebHelper webHelper, Long processId, String name, Executor value) {
        String displayName = value instanceof Actor ? value.getFullName() : value.getName();
        if (ApplicationContextFactory.getPermissionDAO().isAllowed(user, Permission.READ, value)) {
            HashMap<String, Object> params = Maps.newHashMap();
            params.put("id", value.getId());
            String href = webHelper.getActionUrl("/manage_executor", params);
            return "<a href=\"" + href + "\">" + displayName + "</>";
        } else {
            return displayName;
        }
    }

}
