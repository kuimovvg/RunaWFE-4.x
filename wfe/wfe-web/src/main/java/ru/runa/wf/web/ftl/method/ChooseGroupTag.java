package ru.runa.wf.web.ftl.method;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDAO;
import ru.runa.wfe.var.dto.WfVariable;
import freemarker.template.TemplateModelException;

/**
 * @deprecated code moved to {@link InputVariableTag}.
 * 
 * @author dofs
 * @since 4.0
 */
@Deprecated
public class ChooseGroupTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;
    @Autowired
    private ExecutorDAO executorDAO;

    @Override
    protected Object executeTag() throws TemplateModelException {
        String varName = getParameterAs(String.class, 0);
        String view = getParameterAs(String.class, 1);
        List<Group> groups = executorDAO.getAllGroups();
        if ("raw".equals(view)) {
            return groups;
        } else if ("selectName".equals(view)) {
            StringBuffer html = new StringBuffer();
            html.append("<select name=\"").append(varName).append("\">");
            for (Group group : groups) {
                html.append("<option value=\"").append(group.getName()).append("\">").append(group.getName()).append("</option>");
            }
            html.append("</select>");
            return html.toString();
        } else if ("selectName".equals(view)) {
            WfVariable variable = variableProvider.getVariableNotNull(varName);
            return ViewUtil.createExecutorSelect(user, variable);
        } else {
            throw new TemplateModelException("Unexpected value of VIEW parameter: " + view);
        }
    }
}
