package ru.runa.wf.web.ftl.method;

import java.util.List;
import java.util.Map;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.var.FileVariable;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FileFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import freemarker.template.TemplateModelException;

public class DisplayLinkedMapsTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        try {
            List<String> variableNames = Lists.newArrayList();
            List<String> componentFormatClassNames = Lists.newArrayList();
            List<Map<?, ?>> maps = Lists.newArrayList();
            int i = 0;
            while (true) {
                String variableName = getParameterAs(String.class, i);
                if (variableName == null) {
                    break;
                }
                WfVariable variable = variableProvider.getVariableNotNull(variableName);
                String valueFormatClassName = ((VariableFormatContainer) variable.getFormatNotNull()).getComponentClassName(1);
                Map<?, ?> map = (Map<?, ?>) variable.getValue();
                if (map == null) {
                    map = Maps.newHashMap();
                }
                variableNames.add(variableName);
                componentFormatClassNames.add(valueFormatClassName);
                maps.add(map);
                i++;
            }
            if (maps.size() > 0) {
                StringBuffer buffer = new StringBuffer();
                buffer.append("<table class=\"displayLinkedMaps\">");
                StringBuffer header = new StringBuffer();
                header.append("<tr class=\"header\">");
                boolean headerValueNotNull = false;
                for (i = 0; i < maps.size(); i++) {
                    String headerVariableName = getParameterAs(String.class, i) + "_header";
                    Object o = variableProvider.getValue(headerVariableName);
                    WfVariable headerVariable = variableProvider.getVariableNotNull(headerVariableName);
                    String value = ViewUtil.getOutput(user, webHelper, variableProvider.getProcessId(), headerVariable);
                    if (headerVariable.getValue() != null) {
                        headerValueNotNull = true;
                    }
                    header.append("<td>").append(value).append("</td>");
                }
                header.append("</tr>");
                if (headerValueNotNull) {
                    buffer.append(header);
                }
                for (Map.Entry<?, ?> entry : maps.get(0).entrySet()) {
                    buffer.append("<tr>");
                    for (int column = 0; column < maps.size(); column++) {
                        Map<?, ?> map = maps.get(column);
                        Object o = map.get(entry.getKey());
                        String variableName = variableNames.get(column);
                        String componentClassName = componentFormatClassNames.get(column);
                        String value;
                        if (FileFormat.class.getName().equals(componentClassName)) {
                            value = ViewUtil.getFileOutput(webHelper, variableProvider.getProcessId(), variableName, (FileVariable) o, 0,
                                    entry.getKey());
                        } else {
                            value = ViewUtil.getOutput(user, webHelper, variableProvider.getProcessId(), variableName, componentClassName, o);
                        }
                        buffer.append("<td>").append(value).append("</td>");
                    }
                    buffer.append("</tr>");
                }
                buffer.append("</table>");
                return buffer.toString();
            }
            return "-";
        } catch (Exception e) {
            throw new TemplateModelException(e);
        }
    }

}
