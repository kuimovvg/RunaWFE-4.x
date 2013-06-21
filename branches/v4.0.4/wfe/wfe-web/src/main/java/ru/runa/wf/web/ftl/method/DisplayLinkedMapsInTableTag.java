package ru.runa.wf.web.ftl.method;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.var.format.FormatCommons;
import freemarker.template.TemplateModelException;

public class DisplayLinkedMapsInTableTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        try {
            List<Map<?, ?>> maps = new ArrayList<Map<?, ?>>();
            int i = 0;
            while (true) {
                String mapVarName = getParameterAs(String.class, i);
                if (mapVarName == null) {
                    break;
                }
                Map<?, ?> map = variableProvider.getValueNotNull(Map.class, mapVarName);
                maps.add(map);
                i++;
            }
            if (maps.size() > 0) {
                StringBuffer buffer = new StringBuffer();
                buffer.append("<table class=\"displayLinkedMaps\">");
                buffer.append("<tr class=\"header\">");
                for (i = 0; i < maps.size(); i++) {
                    String headerVariableName = getParameterAs(String.class, i) + "_header";
                    Object o = variableProvider.getValue(headerVariableName);
                    String value = FormatCommons.getVarOut(o, webHelper, variableProvider.getProcessId(), headerVariableName, 0, null);
                    buffer.append("<td>").append(value).append("</td>");
                }
                buffer.append("</tr>");
                for (Map.Entry<?, ?> entry : maps.get(0).entrySet()) {
                    buffer.append("<tr>");
                    for (i = 0; i < maps.size(); i++) {
                        Map<?, ?> map = maps.get(i);
                        Object o = map.get(entry.getKey());
                        String value = FormatCommons.getVarOut(o, webHelper, variableProvider.getProcessId(), getParameterAs(String.class, i), 0,
                                entry.getKey());
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
