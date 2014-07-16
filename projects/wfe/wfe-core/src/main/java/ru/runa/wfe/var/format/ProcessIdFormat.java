package ru.runa.wfe.var.format;

import java.util.HashMap;

import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.User;

import com.google.common.collect.Maps;

public class ProcessIdFormat extends LongFormat implements VariableDisplaySupport {

    @Override
    public String getName() {
        return "processref";
    }

    @Override
    public String formatHtml(User user, WebHelper webHelper, Long currentProcessId, String name, Object object) {
        Long processId = (Long) object;
        boolean link = false;
        String title;
        try {
            ru.runa.wfe.execution.Process process = ApplicationContextFactory.getProcessDAO().getNotNull(processId);
            link = ApplicationContextFactory.getPermissionDAO().isAllowed(user, Permission.READ, process);
            title = ApplicationContextFactory.getDeploymentDAO().getNotNull(process.getDeployment().getId()).getName();
        } catch (Exception e) {
            title = e.toString();
            LogFactory.getLog(getClass()).warn("Unable to determine permission or process name", e);
        }
        if (link) {
            HashMap<String, Object> params = Maps.newHashMap();
            params.put(WebHelper.PARAM_ID, processId);
            String href = webHelper.getActionUrl(WebHelper.ACTION_VIEW_PROCESS, params);
            return "<a href=\"" + href + "\" title=\"" + title + "\">" + processId + "</a>";
        } else {
            return "<span title=\"" + title + "\">" + processId + "</span>";
        }
    }

}
