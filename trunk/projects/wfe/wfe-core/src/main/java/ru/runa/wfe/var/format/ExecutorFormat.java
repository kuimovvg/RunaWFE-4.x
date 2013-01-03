package ru.runa.wfe.var.format;

import java.util.HashMap;

import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.auth.SubjectPrincipalsHelper;
import ru.runa.wfe.security.dao.PermissionDAO;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.dao.ExecutorDAO;

import com.google.common.collect.Maps;

public class ExecutorFormat implements VariableFormat<Executor>, VariableDisplaySupport<Executor> {
    @Autowired
    private ExecutorDAO executorDAO;
    @Autowired
    private PermissionDAO permissionDAO;

    @Override
    public Executor parse(String[] source) throws Exception {
        return executorDAO.getActorByCode(TypeConversionUtil.convertTo(source[0], Long.class));
        // TODO tmp
        // return
        // executorDAO.getExecutor(TypeConversionUtil.convertTo(source[0],
        // Long.class));
    }

    @Override
    public String format(Executor object) {
        return object.getFullName();
    }

    @Override
    public String getHtml(Subject subject, WebHelper webHelper, Long processId, String name, Executor value) {
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
