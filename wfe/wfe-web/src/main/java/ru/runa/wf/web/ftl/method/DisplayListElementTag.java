package ru.runa.wf.web.ftl.method;

import java.util.List;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import freemarker.template.TemplateModelException;

public class DisplayListElementTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    @Override
    protected Object executeTag() throws TemplateModelException {
        String listVarName = getParameterAs(String.class, 0);
        List<Object> list = variableProvider.getValueNotNull(List.class, listVarName);
        int index = getParameterAs(Integer.class, 1);
        Object object = null;
        if (index < list.size()) {
            object = list.get(index);
        }
        return ViewUtil.getVarOut(object, subject, webHelper, variableProvider.getProcessId(), listVarName, index, null);
    }

}
