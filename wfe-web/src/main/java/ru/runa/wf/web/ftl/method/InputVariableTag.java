package ru.runa.wf.web.ftl.method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ListFormat;
import freemarker.template.TemplateModelException;

public class InputVariableTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(InputVariableTag.class);

    // TODO css classes in FTL tags

    @Override
    protected Object executeTag() throws TemplateModelException {
        String variableName = getParameterAs(String.class, 0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        String formatClassName = variable.getFormatClassNameNotNull();
        Object value = variable.getValue();
        String html;
        if (ListFormat.class.getName().equals(formatClassName)) {
            EditListTag tag = new EditListTag();
            tag.initChained(this);
            html = tag.renderRequest();
        } else {
            html = ViewUtil.getComponentInput(user, variableName, formatClassName, value);
        }
        if (html.length() == 0) {
            log.warn("No HTML built for " + variable);
        }
        return html;
    }

}
