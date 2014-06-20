package ru.runa.wf.web.ftl.method;

import java.util.List;
import java.util.Map;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import freemarker.template.TemplateModelException;

public class DisplayLinkedMapsTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        List<WfVariable> variables = Lists.newArrayList();
        String firstParameter = getParameterAsString(0);
        boolean componentView = "true".equals(firstParameter);
        int i = ("false".equals(firstParameter) || "true".equals(firstParameter)) ? 1 : 0;
        while (true) {
            String variableName = getParameterAsString(i);
            if (variableName == null) {
                break;
            }
            WfVariable variable = variableProvider.getVariableNotNull(variableName);
            if (variable.getValue() == null) {
                variable.setValue(Maps.newHashMap());
            }
            Preconditions.checkArgument(variable.getValue() instanceof Map, variable);
            variables.add(variable);
            i++;
        }
        if (variables.size() > 0) {
            StringBuffer html = new StringBuffer();
            html.append("<table class=\"displayLinkedMaps\">");
            html.append(ViewUtil.generateTableHeader(variables, variableProvider, null));
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) variables.get(0).getValue()).entrySet()) {
                html.append("<tr>");
                for (int column = 0; column < variables.size(); column++) {
                    WfVariable containerVariable = variables.get(column);
                    WfVariable componentVariable = ViewUtil.createMapComponentVariable(containerVariable, entry.getKey());
                    String value;
                    if (componentView) {
                        value = ViewUtil.getComponentOutput(user, webHelper, variableProvider.getProcessId(), componentVariable);
                    } else {
                        value = ViewUtil.getOutput(user, webHelper, variableProvider.getProcessId(), componentVariable);
                    }
                    html.append("<td>").append(value).append("</td>");
                }
                html.append("</tr>");
            }
            html.append("</table>");
            return html.toString();
        }
        return "-";
    }

}
