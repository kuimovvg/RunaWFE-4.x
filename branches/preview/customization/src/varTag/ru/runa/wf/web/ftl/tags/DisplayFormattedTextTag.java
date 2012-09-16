package ru.runa.wf.web.ftl.tags;

import ru.runa.wf.web.ftl.FreemarkerTag;
import freemarker.template.TemplateModelException;

public class DisplayFormattedTextTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        String stringVarName = getParameterAs(String.class, 0);
        String string = getVariableAs(String.class, stringVarName, true);
        if (string != null) {
            string = string.replaceAll("\n", "<br>");
            string = string.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
            string = string.replaceAll(" ", "&nbsp;");
        }
        return string;
    }

}
