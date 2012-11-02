package ru.runa.wf.web.ftl.method;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDAO;
import freemarker.template.TemplateModelException;

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
        }
        StringBuffer html = new StringBuffer();
        html.append("<select name=\"").append(varName).append("\">");
        for (Group group : groups) {
            String value;
            if ("selectName".equals(view)) {
                value = group.getName();
            } else if ("selectId".equals(view)) {
                value = String.valueOf(group.getId());
            } else {
                throw new TemplateModelException("Unexpected value of VIEW parameter: " + view);
            }
            html.append("<option value=\"").append(value).append("\">").append(group.getName()).append("</option>");
        }
        html.append("</select>");
        return html.toString();
    }
}
