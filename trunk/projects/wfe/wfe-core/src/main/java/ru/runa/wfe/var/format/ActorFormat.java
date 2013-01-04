package ru.runa.wfe.var.format;

import java.util.HashMap;

import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.auth.SubjectPrincipalsHelper;
import ru.runa.wfe.security.dao.PermissionDAO;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.dao.ExecutorDAO;

import com.google.common.collect.Maps;

public class ActorFormat implements VariableFormat<Actor>, VariableDisplaySupport<Actor> {
    @Autowired
    private ExecutorDAO executorDAO;
    @Autowired
    private PermissionDAO permissionDAO;

    @Override
    public Actor parse(String[] source) throws Exception {
        return TypeConversionUtil.toExecutor(executorDAO, source[0]);
    }

    @Override
    public String format(Actor object) {
        return object.getFullName();
    }

    @Override
    public String getHtml(Subject subject, WebHelper webHelper, Long processId, String name, Actor value) {
        if (permissionDAO.isAllowed(SubjectPrincipalsHelper.getActor(subject), Permission.READ, value)) {
            HashMap<String, Object> params = Maps.newHashMap();
            params.put("id", value.getId());
            String href = webHelper.getActionUrl("/manage_executor", params);
            return "<a href=\"" + href + "\">" + value.getName() + "</>";
        } else {
            return value.getName();
        }
    }

}
