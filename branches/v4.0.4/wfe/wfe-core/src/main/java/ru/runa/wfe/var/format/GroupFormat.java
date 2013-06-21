package ru.runa.wfe.var.format;

import java.util.HashMap;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

import com.google.common.collect.Maps;

public class GroupFormat implements VariableFormat<Group>, VariableDisplaySupport<Group> {

    @Override
    public Group parse(String[] source) throws Exception {
        return TypeConversionUtil.convertTo(Group.class, source[0]);
    }

    @Override
    public String format(Group object) {
        return object.getName();
    }

    @Override
    public String getHtml(User user, WebHelper webHelper, Long processId, String name, Group value) {
        if (ApplicationContextFactory.getPermissionDAO().isAllowed(user, Permission.READ, value)) {
            HashMap<String, Object> params = Maps.newHashMap();
            params.put("id", value.getId());
            String href = webHelper.getActionUrl("/manage_executor", params);
            return "<a href=\"" + href + "\">" + value.getName() + "</>";
        } else {
            return value.getName();
        }
    }
}
