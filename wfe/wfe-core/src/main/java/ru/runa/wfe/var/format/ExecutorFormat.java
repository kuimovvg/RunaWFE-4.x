package ru.runa.wfe.var.format;

import java.util.HashMap;

import javax.security.auth.Subject;
import javax.servlet.jsp.PageContext;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.dao.ExecutorDAO;

import com.google.common.collect.Maps;

public class ExecutorFormat implements VariableFormat, VariableDisplaySupport {
    @Autowired
    private ExecutorDAO executorDAO;

    @Override
    public Object parse(String[] source) throws Exception {
        return executorDAO.getExecutor(TypeConversionUtil.convertTo(source[0], Long.class));
    }

    @Override
    public String format(Object object) {
        return ((Executor) object).getName();
    }

    @Override
    public String getHtml(Subject subject, PageContext pageContext, WebHelper webHelper, Long processId, String name, Object value) {
        Executor executor = (Executor) value;
        HashMap<String, Object> params = Maps.newHashMap();
        params.put("id", executor.getId());
        String href = webHelper.getActionUrl("/manage_executor", params, pageContext, PortletUrlType.Render);
        return "<a href=\"" + href + "\">" + executor.getName() + "</>";
    }

}
