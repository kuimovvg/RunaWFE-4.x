package ru.runa.wf.web.ftl.method;

import java.util.List;
import java.util.Map;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.var.FileVariable;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FileFormat;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import freemarker.template.TemplateModelException;

public class DisplayLinkedMapsTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        List<String> variableNames = Lists.newArrayList();
        List<String> componentFormatClassNames = Lists.newArrayList();
        List<Map<?, ?>> maps = Lists.newArrayList();
        String firstParameter = getParameterAs(String.class, 0);
        boolean componentView = "true".equals(firstParameter);
        int i = ("false".equals(firstParameter) || "true".equals(firstParameter)) ? 1 : 0;
        while (true) {
            String variableName = getParameterAs(String.class, i);
            if (variableName == null) {
                break;
            }
            WfVariable variable = variableProvider.getVariableNotNull(variableName);
            String valueFormatClassName = ViewUtil.getElementFormatClassName(variable, 1);
            Map<?, ?> map = (Map<?, ?>) variableProvider.getValue(variableName);
            if (map == null) {
                map = Maps.newHashMap();
            }
            variableNames.add(variableName);
            componentFormatClassNames.add(valueFormatClassName);
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
                    String componentClassName = componentFormatClassNames.get(column);
                    String value;
                    if (componentView) {
                        if (FileFormat.class.getName().equals(componentClassName)) {
                            value = ViewUtil.getFileOutput(webHelper, variableProvider.getProcessId(), variableName, (FileVariable) o, null,
                                    entry.getKey());
                        } else {
                            value = ViewUtil.getComponentOutput(user, variableName, componentClassName, o);
                        }
                    } else {
                        value = ViewUtil.getOutput(user, webHelper, variableProvider.getProcessId(), variableName, componentClassName, o);
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
