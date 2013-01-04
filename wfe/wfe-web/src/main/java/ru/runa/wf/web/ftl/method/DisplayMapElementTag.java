package ru.runa.wf.web.ftl.method;

import java.util.Map;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.var.format.FormatCommons;
import freemarker.template.TemplateModelException;

public class DisplayMapElementTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        String mapVarName = getParameterAs(String.class, 0);
        Map<?, ?> map = variableProvider.getValueNotNull(Map.class, mapVarName);
        Object key = getParameterAs(Object.class, 1);
        Object object = map.get(key);
        return FormatCommons.getVarOut(object, subject, webHelper, variableProvider.getProcessId(), mapVarName, 0, key);
    }

}
