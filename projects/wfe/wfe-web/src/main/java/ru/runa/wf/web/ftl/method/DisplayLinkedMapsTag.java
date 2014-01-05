package ru.runa.wf.web.ftl.method;

import java.util.List;
import java.util.Map;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import freemarker.template.TemplateModelException;

public class DisplayLinkedMapsTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        List<String> variableNames = Lists.newArrayList();
        List<VariableFormat> componentFormats = Lists.newArrayList();
        List<Map<?, ?>> maps = Lists.newArrayList();
        String firstParameter = getParameterAsString(0);
        boolean componentView = "true".equals(firstParameter);
        int i = ("false".equals(firstParameter) || "true".equals(firstParameter)) ? 1 : 0;
        while (true) {
            String variableName = getParameterAsString(i);
            if (variableName == null) {
                break;
            }
            WfVariable variable = variableProvider.getVariableNotNull(variableName);
            VariableFormat componentFormat = FormatCommons.createComponent(variable, 1);
            Map<?, ?> map = (Map<?, ?>) variableProvider.getValue(variableName);
            if (map == null) {
                map = Maps.newHashMap();
            }
            variableNames.add(variableName);
            componentFormats.add(componentFormat);
            maps.add(map);
            i++;
        }
        if (maps.size() > 0) {
            StringBuffer html = new StringBuffer();
            html.append("<table class=\"displayLinkedMaps\">");
            html.append(ViewUtil.generateTableHeader(variableNames, variableProvider, null));
            for (Map.Entry<?, ?> entry : maps.get(0).entrySet()) {
                html.append("<tr>");
                for (int column = 0; column < maps.size(); column++) {
                    Map<?, ?> map = maps.get(column);
                    Object o = map.get(entry.getKey());
                    String variableName = variableNames.get(column);
                    VariableFormat componentFormat = componentFormats.get(column);
                    String value;
                    if (componentView) {
                        value = ViewUtil.getComponentOutput(user, webHelper, variableProvider.getProcessId(), variableName, componentFormat, o);
                    } else {
                        value = ViewUtil.getOutput(user, webHelper, variableProvider.getProcessId(), variableName, componentFormat, o);
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
