package ru.runa.wf.web.ftl.method;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ActorFormat;
import ru.runa.wfe.var.format.BigDecimalFormat;
import ru.runa.wfe.var.format.BooleanFormat;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.DateTimeFormat;
import ru.runa.wfe.var.format.DoubleFormat;
import ru.runa.wfe.var.format.ExecutorFormat;
import ru.runa.wfe.var.format.FileFormat;
import ru.runa.wfe.var.format.GroupFormat;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.LongFormat;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.TextFormat;
import ru.runa.wfe.var.format.TimeFormat;
import freemarker.template.TemplateModelException;

public class InputVariableTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(InputVariableTag.class);

    @Override
    protected Object executeTag() throws TemplateModelException {
        String variableName = getParameterAs(String.class, 0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        String format = variable.getDefinition().getFormatClassName();
        Object value = variable.getValue();
        String html = ""; // TODO refactor
        if (StringFormat.class.getName().equals(format)) {
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputString\" ";
            if (value != null) {
                html += "value=\"" + value + "\" ";
            }
            html += "/>";
        }
        if (TextFormat.class.getName().equals(format)) {
            html += "<textarea name=\"" + variableName + "\" class=\"inputText\">";
            if (value != null) {
                html += value;
            }
            html += "</textarea>";
        }
        if (LongFormat.class.getName().equals(format) || DoubleFormat.class.getName().equals(format)
                || BigDecimalFormat.class.getName().equals(format)) {
            html += "<input type=\"text\" class=\"inputNumber\" name=\"" + variableName + "\" ";
            if (value instanceof Number) {
                html += "value=\"" + value + "\" ";
            }
            html += "/>";
        }
        if (FileFormat.class.getName().equals(format)) {
            html += "<input type=\"file\" name=\"" + variableName + "\" class=\"inputFile\" />";
        }
        if (BooleanFormat.class.getName().equals(format)) {
            html += "<input type=\"checkbox\" name=\"" + variableName + "\" class=\"inputBoolean\" ";
            if (value instanceof Boolean && ((Boolean) value)) {
                html += "checked=\"checked\" ";
            }
            html += "/>";
        }
        if (DateFormat.class.getName().equals(format)) {
            html += "<input type=\"text\" class=\"inputDate\" name=\"" + variableName + "\" style=\"width: 100px;\" ";
            if (value instanceof Date) {
                html += "value=\"" + CalendarUtil.formatDate((Date) value) + "\" ";
            }
            html += "/>";
        }
        if (TimeFormat.class.getName().equals(format)) {
            html += "<input type=\"text\" class=\"inputTime\" name=\"" + variableName + "\" style=\"width: 50px;\" ";
            if (value instanceof Date) {
                html += "value=\"" + CalendarUtil.formatTime((Date) value) + "\" ";
            }
            html += "/>";
        }
        if (DateTimeFormat.class.getName().equals(format)) {
            html += "<input type=\"text\" class=\"inputDateTime\" name=\"" + variableName + "\" style=\"width: 150px;\" ";
            if (value instanceof Date) {
                html += "value=\"" + CalendarUtil.formatDateTime((Date) value) + "\" ";
            }
            html += "/>";
        }
        // if (ActorFormat.class.getName().equals(format)) {
        // html += "<input type=\"text\" class=\"inputDateTime\" name=\"" +
        // variableName + "\" style=\"width: 150px;\" ";
        // if (value instanceof Date) {
        // html += "value=\"" + CalendarUtil.formatDateTime((Date) value) +
        // "\" ";
        // }
        // html = ViewUtil.createActorSelect(subject, variableName, value);
        // }
        if (ActorFormat.class.getName().equals(format) || ExecutorFormat.class.getName().equals(format) || GroupFormat.class.getName().equals(format)) {
            html = ViewUtil.createExecutorSelect(user, variable);
        }
        if (ListFormat.class.getName().equals(format)) {
            EditStringListTag tag = new EditStringListTag();
            tag.init(user, webHelper, variableProvider);
            html = tag.renderRequest();
        }
        if (html.length() == 0) {
            log.warn("No HTML built (" + variableName + ") for format " + format);
        }
        return html;
    }

}
