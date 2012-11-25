package ru.runa.wf.web.ftl.method;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import freemarker.template.TemplateModelException;

public class DisplayFormattedTextTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        String variableName = getParameterAs(String.class, 0);
        String string = variableProvider.getValue(String.class, variableName);
        if (string == null) {
            return "";
        }
        string = string.replaceAll("\n", "<br>");
        string = string.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
        string = string.replaceAll(" ", "&nbsp;");
        // TODO
        // "<div style='display: block; padding-left: 5px; background-color: #FFC; border-color: #FC6;'>"
        // +
        return string;
    }

}
