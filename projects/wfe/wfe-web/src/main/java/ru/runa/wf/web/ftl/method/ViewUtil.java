package ru.runa.wf.web.ftl.method;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.wf.web.FormSubmissionUtils;
import ru.runa.wf.web.servlet.UploadedFile;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.FileVariable;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ActorFormat;
import ru.runa.wfe.var.format.BigDecimalFormat;
import ru.runa.wfe.var.format.BooleanFormat;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.DateTimeFormat;
import ru.runa.wfe.var.format.DoubleFormat;
import ru.runa.wfe.var.format.ExecutorFormat;
import ru.runa.wfe.var.format.FileFormat;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.GroupFormat;
import ru.runa.wfe.var.format.LongFormat;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.TextFormat;
import ru.runa.wfe.var.format.TimeFormat;
import ru.runa.wfe.var.format.UserTypeFormat;
import ru.runa.wfe.var.format.VariableDisplaySupport;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

public class ViewUtil {
    private static final Log log = LogFactory.getLog(ViewUtil.class);

    public static String createExecutorSelect(User user, WfVariable variable) {
        return createExecutorSelect(user, variable.getDefinition().getName(), variable.getFormatNotNull(), variable.getValue(), true);
    }

    private static String createExecutorSelect(User user, String variableName, VariableFormat variableFormat, Object value, boolean enabled) {
        BatchPresentation batchPresentation;
        int sortColumn = 0;
        boolean javaSort = false;
        if (ActorFormat.class == variableFormat.getClass()) {
            batchPresentation = BatchPresentationFactory.ACTORS.createNonPaged();
            sortColumn = 1;
        } else if (ExecutorFormat.class == variableFormat.getClass()) {
            batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
            javaSort = true;
        } else if (GroupFormat.class == variableFormat.getClass()) {
            batchPresentation = BatchPresentationFactory.GROUPS.createNonPaged();
        } else {
            throw new InternalApplicationException("Unexpected format " + variableFormat);
        }
        batchPresentation.setFieldsToSort(new int[] { sortColumn }, new boolean[] { true });
        List<Executor> executors = (List<Executor>) Delegates.getExecutorService().getExecutors(user, batchPresentation);
        return createExecutorSelect(variableName, executors, value, javaSort, enabled);
    }

    public static String createExecutorSelect(String variableName, List<? extends Executor> executors, Object value, boolean javaSort, boolean enabled) {
        String html = "<select name=\"" + variableName + "\"";
        if (!enabled) {
            html += " disabled=\"true\"";
        }
        html += ">";
        if (javaSort) {
            Collections.sort(executors);
        }
        html += "<option value=\"\"> ------------------------- </option>";
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

    public static String getHiddenInput(String variableName, Class<? extends VariableFormat> formatClass, Object value) {
        if (value != null) {
            String stringValue = getStringValue(variableName, formatClass, value);
            if (stringValue != null) {
                return "<input type=\"hidden\" name=\"" + variableName + "\" value=\"" + stringValue + "\" />";
            }
        }
        return "";
    }

    private static String getStringValue(String variableName, Class<? extends VariableFormat> formatClass, Object value) {
        if (value != null) {
            String stringValue = "";
            if (DateFormat.class == formatClass) {
                if (value instanceof Date) {
                    stringValue = CalendarUtil.formatDate((Date) value);
                }
            } else if (TimeFormat.class == formatClass) {
                if (value instanceof Date) {
                    stringValue = CalendarUtil.formatTime((Date) value);
                }
            } else if (DateTimeFormat.class == formatClass) {
                if (value instanceof Date) {
                    stringValue = CalendarUtil.formatDateTime((Date) value);
                }
            } else if (ActorFormat.class == formatClass || ExecutorFormat.class == formatClass || GroupFormat.class == formatClass) {
                if (value instanceof Executor) {
                    stringValue = "ID" + ((Executor) value).getId();
                }
            } else {
                stringValue = value.toString();
            }
            return stringValue;
        }
        return null;
    }

    public static String getComponentInput(User user, WebHelper webHelper, String variableName, VariableFormat variableFormat, Object value) {
        String html = "";
        if (StringFormat.class == variableFormat.getClass()) {
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputString\" ";
            if (value != null) {
                html += "value=\"" + value + "\" ";
            }
            html += "/>";
        }
        if (TextFormat.class == variableFormat.getClass()) {
            html += "<textarea name=\"" + variableName + "\" class=\"inputText\">";
            if (value != null) {
                html += value;
            }
            html += "</textarea>";
        }
        if (LongFormat.class == variableFormat.getClass() || DoubleFormat.class == variableFormat.getClass()
                || BigDecimalFormat.class == variableFormat.getClass()) {
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputNumber\" ";
            if (value instanceof Number) {
                html += "value=\"" + value + "\" ";
            }
            html += "/>";
        }
        if (FileFormat.class == variableFormat.getClass()) {
            html += getFileInput(webHelper, variableName, (FileVariable) value);
        }
        if (BooleanFormat.class == variableFormat.getClass()) {
            html += "<input type=\"checkbox\" name=\"" + variableName + "\" class=\"inputBoolean\" ";
            if (value instanceof Boolean && ((Boolean) value)) {
                html += "checked=\"checked\" ";
            }
            html += "/>";
        }
        if (DateFormat.class == variableFormat.getClass()) {
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputDate\" style=\"width: 100px;\" ";
            if (value instanceof Date) {
                html += "value=\"" + CalendarUtil.formatDate((Date) value) + "\" ";
            }
            html += "/>";
        }
        if (TimeFormat.class == variableFormat.getClass()) {
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputTime\" style=\"width: 50px;\" ";
            if (value instanceof Date) {
                html += "value=\"" + CalendarUtil.formatTime((Date) value) + "\" ";
            }
            html += "/>";
        }
        if (DateTimeFormat.class == variableFormat.getClass()) {
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputDateTime\" style=\"width: 150px;\" ";
            if (value instanceof Date) {
                html += "value=\"" + CalendarUtil.formatDateTime((Date) value) + "\" ";
            }
            html += "/>";
        }
        if (variableFormat instanceof ExecutorFormat) {
            html = ViewUtil.createExecutorSelect(user, variableName, variableFormat, value, true);
        }
        return html;
    }

    public static String getComponentOutput(User user, WebHelper webHelper, Long processId, String variableName, VariableFormat variableFormat,
            Object value) {
        if (StringFormat.class == variableFormat.getClass()) {
            String html = "<input type=\"text\" name=\"" + variableName + "\" class=\"inputString\" disabled=\"true\" ";
            if (value != null) {
                html += "value=\"" + value + "\" ";
            }
            html += "/>";
            return html;
        }
        if (TextFormat.class == variableFormat.getClass()) {
            String html = "<textarea name=\"" + variableName + "\" class=\"inputText\" disabled=\"true\">";
            if (value != null) {
                html += value;
            }
            html += "</textarea>";
            return html;
        }
        if (LongFormat.class == variableFormat.getClass() || DoubleFormat.class == variableFormat.getClass()
                || BigDecimalFormat.class == variableFormat.getClass()) {
            String html = "<input type=\"text\" name=\"" + variableName + "\" class=\"inputNumber\" disabled=\"true\" ";
            if (value instanceof Number) {
                html += "value=\"" + value + "\" ";
            }
            html += "/>";
            return html;
        }
        if (FileFormat.class == variableFormat.getClass()) {
            // because component is not usable
            return FormatCommons.getFileOutput(webHelper, processId, variableName, (FileVariable) value);
        }
        if (BooleanFormat.class == variableFormat.getClass()) {
            String html = "<input type=\"checkbox\" name=\"" + variableName + "\" class=\"inputBoolean\" disabled=\"true\" ";
            if (value instanceof Boolean && ((Boolean) value)) {
                html += "checked=\"checked\" ";
            }
            html += "/>";
            return html;
        }
        if (DateFormat.class == variableFormat.getClass()) {
            String html = "<input type=\"text\" name=\"" + variableName + "\" class=\"inputDate\" style=\"width: 100px;\" disabled=\"true\" ";
            if (value instanceof Date) {
                html += "value=\"" + CalendarUtil.formatDate((Date) value) + "\" ";
            }
            html += "/>";
            return html;
        }
        if (TimeFormat.class == variableFormat.getClass()) {
            String html = "<input type=\"text\" name=\"" + variableName + "\" class=\"inputTime\" style=\"width: 50px;\" disabled=\"true\" ";
            if (value instanceof Date) {
                html += "value=\"" + CalendarUtil.formatTime((Date) value) + "\" ";
            }
            html += "/>";
            return html;
        }
        if (DateTimeFormat.class == variableFormat.getClass()) {
            String html = "<input type=\"text\" name=\"" + variableName + "\" class=\"inputDateTime\" style=\"width: 150px;\" disabled=\"true\" ";
            if (value instanceof Date) {
                html += "value=\"" + CalendarUtil.formatDateTime((Date) value) + "\" ";
            }
            html += "/>";
            return html;
        }
        if (variableFormat instanceof ExecutorFormat) {
            return ViewUtil.createExecutorSelect(user, variableName, variableFormat, value, false);
        }
        if (variableFormat instanceof UserTypeFormat) {
            // TODO tmp
            return ((UserTypeFormat) variableFormat).formatHtml(user, webHelper, processId, variableName, value, null);
        }
        throw new InternalApplicationException("Not supported format " + variableFormat);
    }

    public static String getComponentJSFunction(VariableFormat variableFormat) {
        if (DateFormat.class == variableFormat.getClass()) {
            return "$('.inputDate').datepicker({ dateFormat: 'dd.mm.yy', buttonImage: '/wfe/images/calendar.gif' });";
        }
        if (TimeFormat.class == variableFormat.getClass()) {
            return "$('.inputTime').timepicker({ ampm: false, seconds: false });";
        }
        if (DateTimeFormat.class == variableFormat.getClass()) {
            return "$('.inputDateTime').datetimepicker({ dateFormat: 'dd.mm.yy' });";
        }
        if (FileFormat.class == variableFormat.getClass() && WebResources.isAjaxFileInputEnabled()) {
            return "$('.dropzone').each(function () { initFileInput($(this)) });";
        }
        return "";
    }

    public static String getOutput(User user, WebHelper webHelper, Long processId, String variableName, VariableFormat componentFormat, Object value) {
        VariableDefinition definition = new VariableDefinition(true, variableName, variableName, componentFormat.getClass().getName());
        WfVariable variable = new WfVariable(definition, value);
        return getOutput(user, webHelper, processId, variable);
    }

    public static String getOutput(User user, WebHelper webHelper, Long processId, WfVariable variable) {
        try {
            if (variable.getValue() == null) {
                return "";
            }
            VariableFormat format = variable.getFormatNotNull();
            if (format instanceof VariableDisplaySupport) {
                VariableDisplaySupport displaySupport = (VariableDisplaySupport) format;
                return displaySupport.formatHtml(user, webHelper, processId, variable.getDefinition().getName(), variable.getValue(), null);
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

    public static String getFileInput(WebHelper webHelper, String variableName, FileVariable value) {
        if (!WebResources.isAjaxFileInputEnabled()) {
            return "<input type=\"file\" name=\"" + variableName + "\" class=\"inputFile\" />";
        }
        UploadedFile file = null;
        if (webHelper != null) {
            file = FormSubmissionUtils.getUploadedFilesMap(webHelper.getRequest()).get(variableName);
            if (value != null && file == null) {
                file = new UploadedFile(value);
                FormSubmissionUtils.getUploadedFilesMap(webHelper.getRequest()).put(variableName, file);
            }
        }
        String attachImageUrl = "";
        String loadingImageUrl = "";
        String deleteImageUrl = "";
        String uploadFileTitle = "Upload file";
        String loadingMessage = "Loading ...";
        if (webHelper != null) {
            attachImageUrl = webHelper.getUrl(Resources.IMAGE_ATTACH);
            loadingImageUrl = webHelper.getUrl(Resources.IMAGE_LOADING);
            deleteImageUrl = webHelper.getUrl(Resources.IMAGE_DELETE);
            uploadFileTitle = Commons.getMessage("message.upload.file", webHelper.getPageContext());
            loadingMessage = Commons.getMessage("message.loading", webHelper.getPageContext());
        }
        String hideStyle = "style=\"display: none;\"";
        String html = "";
        html += "<div class=\"inputFileContainer\">";
        html += "\n\t<div class=\"dropzone\" " + (file != null ? hideStyle : "") + ">";
        html += "\n\t\t<label class=\"inputFileAttach\">";
        html += "\n\t\t\t<div style=\"float: left;\"><img src=\"" + attachImageUrl + "\" />" + uploadFileTitle + "</div>";
        html += "\n\t\t\t<input class=\"inputFile inputFileAjax\" name=\"" + variableName + "\" type=\"file\">";
        html += "\n\t\t</label>";
        html += "\n\t</div>";
        html += "\n\t<div class=\"progressbar\" " + (file == null ? hideStyle : "") + ">";
        html += "\n\t\t<div class=\"line\" style=\"width: " + (file != null ? "10" : "") + "0%;\"></div>";
        html += "\n\t\t<div class=\"status\">";
        if (file != null) {
            html += "\n\t\t\t<img src=\"" + deleteImageUrl + "\" class=\"inputFileDelete\" inputId=\"" + variableName + "\">";
        } else {
            html += "\n\t\t\t<img src=\"" + loadingImageUrl + "\" inputId=\"" + variableName + "\">";
        }
        html += "\n\t\t\t<span class=\"statusText\">";
        if (file != null && webHelper != null) {
            String viewUrl = webHelper.getUrl("/upload?action=view&inputId=" + variableName);
            html += "<a href='" + viewUrl + "'>" + file.getName() + " - " + file.getSize() + "</a>";
        } else {
            html += loadingMessage;
        }
        html += "</span>";
        html += "\n\t\t</div>";
        html += "\n\t</div>";
        html += "\n</div>";
        return html;
    }

    public static String generateTableHeader(List<String> variableNames, IVariableProvider variableProvider, String operationsColumn) {
        StringBuffer header = new StringBuffer();
        header.append("<tr class=\"header\">");
        for (String variableName : variableNames) {
            Object value = variableProvider.getValue(variableName + "_header");
            if (value == null) {
                value = variableName;
            }
            header.append("<th>").append(variableName).append("</th>");
        }
        if (operationsColumn != null) {
            header.append(operationsColumn);
        }
        header.append("</tr>");
        return header.toString();
    }

    public static String getFileLogOutput(WebHelper webHelper, Long logId, String fileName) {
        HashMap<String, Object> params = Maps.newHashMap();
        params.put("logId", logId);
        return FormatCommons.getFileOutput(webHelper, params, fileName);
    }

}
