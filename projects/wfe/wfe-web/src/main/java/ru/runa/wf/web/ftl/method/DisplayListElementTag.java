package ru.runa.wf.web.ftl.method;

import java.util.List;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.var.FileVariable;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FileFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;
import freemarker.template.TemplateModelException;

public class DisplayListElementTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    @Override
    protected Object executeTag() throws TemplateModelException {
        String variableName = getParameterAs(String.class, 0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        List<Object> list = (List<Object>) variable.getValue();
        int index = getParameterAs(Integer.class, 1);
        Object object = null;
        if (index < list.size()) {
            object = list.get(index);
        }
        String elementFormatClassName = ((VariableFormatContainer) variable.getFormatNotNull()).getComponentClassName(0);
        if (FileFormat.class.getName().equals(elementFormatClassName)) {
            return ViewUtil.getFileOutput(webHelper, variableProvider.getProcessId(), variableName, (FileVariable) object, index, null);
        } else {
            return ViewUtil.getOutput(user, webHelper, variableProvider.getProcessId(), variableName, elementFormatClassName, object);
        }
    }

}
