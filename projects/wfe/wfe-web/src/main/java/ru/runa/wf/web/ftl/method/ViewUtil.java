package ru.runa.wf.web.ftl.method;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
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
import ru.runa.wfe.var.format.LongFormat;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.TextFormat;
import ru.runa.wfe.var.format.TimeFormat;
import ru.runa.wfe.var.format.VariableDisplaySupport;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.base.Objects;

public class ViewUtil {
    private static final Log log = LogFactory.getLog(ViewUtil.class);

    public static String createExecutorSelect(User user, WfVariable variable) {
        return createExecutorSelect(user, variable.getDefinition().getName(), variable.getFormatClassNameNotNull(), variable.getValue(), true);
    }

    public static String createExecutorSelect(User user, String variableName, String formatClassName, Object value, boolean enabled) {
        BatchPresentation batchPresentation;
        int sortColumn = 0;
        boolean javaSort = false;
        if (ActorFormat.class.getName().equals(formatClassName)) {
            batchPresentation = BatchPresentationFactory.ACTORS.createNonPaged();
            sortColumn = 1;
        } else if (ExecutorFormat.class.getName().equals(formatClassName)) {
            batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
            javaSort = true;
        } else if (GroupFormat.class.getName().equals(formatClassName)) {
            batchPresentation = BatchPresentationFactory.GROUPS.createNonPaged();
        } else {
            throw new InternalApplicationException("Unexpected format " + formatClassName);
        }
        batchPresentation.setFieldsToSort(new int[] { sortColumn }, new boolean[] { true });
        List<Executor> executors = (List<Executor>) Delegates.getExecutorService().getExecutors(user, batchPresentation);
        String html = "<select name=\"" + variableName + "\"";
        if (!enabled) {
            html += " disabled=\"true\"";
        }
        html += ">";
        if (javaSort) {
            Collections.sort(executors);
        }
        for (Executor executor : executors) {
            html += "<option value=\"ID" + executor.getId() + "\"";
            if (Objects.equal(executor, value)) {
                html += " selected";
            }
            html += ">" + executor.getLabel() + "</option>";
        }
        html += "</select>";
        return html;
    }

    public static String getComponentInput(User user, String variableName, String formatClassName, Object value, boolean enabled) {
        String html = "";
        if (StringFormat.class.getName().equals(formatClassName)) {
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputString\" ";
            if (value != null) {
                html += "value=\"" + value + "\" ";
            }
            if (!enabled) {
                html += "disabled=\"true\" ";
            }
            html += "/>";
        }
        if (TextFormat.class.getName().equals(formatClassName)) {
            html += "<textarea name=\"" + variableName + "\" class=\"inputText\">";
            if (value != null) {
                html += value;
            }
            if (!enabled) {
                html += "disabled=\"true\" ";
            }
            html += "</textarea>";
        }
        if (LongFormat.class.getName().equals(formatClassName) || DoubleFormat.class.getName().equals(formatClassName)
                || BigDecimalFormat.class.getName().equals(formatClassName)) {
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputNumber\" ";
            if (value instanceof Number) {
                html += "value=\"" + value + "\" ";
            }
            if (!enabled) {
                html += "disabled=\"true\" ";
            }
            html += "/>";
        }
        if (FileFormat.class.getName().equals(formatClassName)) {
            html += "<input type=\"file\" name=\"" + variableName + "\" class=\"inputFile\" ";
            if (!enabled) {
                html += "disabled=\"true\" ";
            }
            html += "/>";
        }
        if (BooleanFormat.class.getName().equals(formatClassName)) {
            html += "<input type=\"checkbox\" name=\"" + variableName + "\" class=\"inputBoolean\" ";
            if (value instanceof Boolean && ((Boolean) value)) {
                html += "checked=\"checked\" ";
            }
            if (!enabled) {
                html += "disabled=\"true\" ";
            }
            html += "/>";
        }
        if (DateFormat.class.getName().equals(formatClassName)) {
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputDate\" style=\"width: 100px;\" ";
            if (value instanceof Date) {
                html += "value=\"" + CalendarUtil.formatDate((Date) value) + "\" ";
            }
            if (!enabled) {
                html += "disabled=\"true\" ";
            }
            html += "/>";
        }
        if (TimeFormat.class.getName().equals(formatClassName)) {
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputTime\" style=\"width: 50px;\" ";
            if (value instanceof Date) {
                html += "value=\"" + CalendarUtil.formatTime((Date) value) + "\" ";
            }
            if (!enabled) {
                html += "disabled=\"true\" ";
            }
            html += "/>";
        }
        if (DateTimeFormat.class.getName().equals(formatClassName)) {
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputDateTime\" style=\"width: 150px;\" ";
            if (value instanceof Date) {
                html += "value=\"" + CalendarUtil.formatDateTime((Date) value) + "\" ";
            }
            if (!enabled) {
                html += "disabled=\"true\" ";
            }
            html += "/>";
        }
        if (ActorFormat.class.getName().equals(formatClassName) || ExecutorFormat.class.getName().equals(formatClassName)
                || GroupFormat.class.getName().equals(formatClassName)) {
            html = ViewUtil.createExecutorSelect(user, variableName, formatClassName, value, enabled);
        }
        return html;
    }

    public static String getComponentJSFunction(String formatClassName) {
        if (DateFormat.class.getName().equals(formatClassName)) {
            return "$('.inputDate').datepicker({ dateFormat: 'dd.mm.yy', buttonImage: '/wfe/images/calendar.gif' });";
        }
        if (TimeFormat.class.getName().equals(formatClassName)) {
            return "$('.inputTime').timepicker({ ampm: false, seconds: false });";
        }
        if (DateTimeFormat.class.getName().equals(formatClassName)) {
            return "$('.inputDateTime').datetimepicker({ dateFormat: 'dd.mm.yy' });";
        }
        return "";
    }

    public static String getVariableValueHtml(User user, WebHelper webHelper, Long processId, WfVariable variable) {
        try {
            VariableFormat<Object> format = variable.getFormatNotNull();
            if (format instanceof VariableDisplaySupport) {
                if (webHelper == null || processId == null || variable.getValue() == null) {
                    return "";
                }
                VariableDisplaySupport<Object> displaySupport = (VariableDisplaySupport<Object>) format;
                return displaySupport.getHtml(user, webHelper, processId, variable.getDefinition().getName(), variable.getValue());
            } else {
                return format.format(variable.getValue());
            }
        } catch (Exception e) {
            log.debug("Unable to format value " + variable + " in " + processId + ": " + e.getMessage());
            if (variable.getValue() != null && variable.getValue().getClass().isArray()) {
                return Arrays.toString((Object[]) variable.getValue());
            } else {
                if (variable.getDefinition().isSyntetic()) {
                    return String.valueOf(variable.getValue());
                } else {
                    return " <span style=\"color: #cccccc;\">(" + variable.getValue() + ")</span>";
                }
            }
        }
    }
}
