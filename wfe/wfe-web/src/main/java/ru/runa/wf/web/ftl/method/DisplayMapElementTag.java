package ru.runa.wf.web.ftl.method;

import java.util.Map;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.var.FileVariable;
import ru.runa.wfe.var.dto.WfVariable;
import freemarker.template.TemplateModelException;

public class DisplayMapElementTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        String variableName = getParameterAs(String.class, 0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        Map<?, ?> map = (Map<?, ?>) variable.getValue();
        Object key = getParameterAs(Object.class, 1);
        Object object = map.get(key);
        String valueFormatClassName = ViewUtil.getElementFormatClassName(variable, 1);
        if (object instanceof FileVariable) {
            return ViewUtil.getFileOutput(webHelper, variableProvider.getProcessId(), variableName, (FileVariable) object, null, key);
        } else {
            return ViewUtil.getOutput(user, webHelper, variableProvider.getProcessId(), variableName, valueFormatClassName, object);
        }
    }

}
