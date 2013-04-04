package ru.runa.wfe.var.format;

import java.util.HashMap;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

import com.google.common.collect.Maps;

public class ActorFormat implements VariableFormat<Actor>, VariableDisplaySupport<Actor> {

    @Override
    public Actor parse(String[] source) throws Exception {
        return TypeConversionUtil.convertTo(Actor.class, source[0]);
    }

    @Override
    public String format(Actor object) {
        return object.getFullName();
    }

    @Override
    public String getHtml(User user, WebHelper webHelper, Long processId, String name, Actor value) {
        if (ApplicationContextFactory.getPermissionDAO().isAllowed(user, Permission.READ, value)) {
            HashMap<String, Object> params = Maps.newHashMap();
            params.put("id", value.getId());
            String href = webHelper.getActionUrl("/manage_executor", params);
            return "<a href=\"" + href + "\">" + value.getFullName() + "</>";
        } else {
            return value.getFullName();
        }
    }

}
