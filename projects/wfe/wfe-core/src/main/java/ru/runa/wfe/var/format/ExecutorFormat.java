package ru.runa.wfe.var.format;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.dao.PermissionDAO;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

import com.google.common.collect.Maps;

public class ExecutorFormat implements VariableFormat<Executor>, VariableDisplaySupport<Executor> {
    @Autowired
    private PermissionDAO permissionDAO;

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
        if (permissionDAO.isAllowed(user, Permission.READ, value)) {
            HashMap<String, Object> params = Maps.newHashMap();
            params.put("id", value.getId());
            String href = webHelper.getActionUrl("/manage_executor", params);
            return "<a href=\"" + href + "\">" + value.getName() + "</>";
        } else {
            return value.getName();
        }
    }

}
