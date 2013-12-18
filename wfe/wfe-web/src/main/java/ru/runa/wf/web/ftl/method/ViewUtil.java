package ru.runa.wf.web.ftl.method;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import ru.runa.wfe.security.Permission;
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
import ru.runa.wfe.var.format.GroupFormat;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.LongFormat;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.TextFormat;
import ru.runa.wfe.var.format.TimeFormat;
import ru.runa.wfe.var.format.VariableDisplaySupport;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

public class ViewUtil {
    private static final Log log = LogFactory.getLog(ViewUtil.class);

    public static String createExecutorSelect(User user, WfVariable variable) {
        return createExecutorSelect(user, variable.getDefinition().getName(), variable.getFormatClassNameNotNull(), variable.getValue(), true);
    }

    private static String createExecutorSelect(User user, String variableName, String formatClassName, Object value, boolean enabled) {
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

    public static String getHiddenInput(String variableName, String formatClassName, Object value) {
        if (value != null) {
            String stringValue = getStringValue(variableName, formatClassName, value);
            if (stringValue != null) {
                return "<input type=\"hidden\" name=\"" + variableName + "\" value=\"" + stringValue + "\" />";
            }
        }
        return "";
    }

    public static String getStringValue(String variableName, String formatClassName, Object value) {
        if (value != null) {
            String stringValue = "";
            if (DateFormat.class.getName().equals(formatClassName)) {
                if (value instanceof Date) {
                    stringValue = CalendarUtil.formatDate((Date) value);
                }
            } else if (TimeFormat.class.getName().equals(formatClassName)) {
                if (value instanceof Date) {
                    stringValue = CalendarUtil.formatTime((Date) value);
                }
            } else if (DateTimeFormat.class.getName().equals(formatClassName)) {
                if (value instanceof Date) {
                    stringValue = CalendarUtil.formatDateTime((Date) value);
                }
            } else if (ActorFormat.class.getName().equals(formatClassName) || ExecutorFormat.class.getName().equals(formatClassName)
                    || GroupFormat.class.getName().equals(formatClassName)) {
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

    public static String getComponentInput(User user, WebHelper webHelper, String variableName, String formatClassName, Object value) {
        String html = "";
        if (StringFormat.class.getName().equals(formatClassName)) {
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputString\" ";
            if (value != null) {
                html += "value=\"" + value + "\" ";
            }
            html += "/>";
        }
        if (TextFormat.class.getName().equals(formatClassName)) {
            html += "<textarea name=\"" + variableName + "\" class=\"inputText\">";
            if (value != null) {
                html += value;
            }
            html += "</textarea>";
        }
        if (LongFormat.class.getName().equals(formatClassName) || DoubleFormat.class.getName().equals(formatClassName)
                || BigDecimalFormat.class.getName().equals(formatClassName)) {
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputNumber\" ";
            if (value instanceof Number) {
                html += "value=\"" + value + "\" ";
            }
            html += "/>";
        }
        if (FileFormat.class.getName().equals(formatClassName)) {
            html += getFileInput(webHelper, variableName);
        }
        if (BooleanFormat.class.getName().equals(formatClassName)) {
            html += "<input type=\"checkbox\" name=\"" + variableName + "\" class=\"inputBoolean\" ";
            if (value instanceof Boolean && ((Boolean) value)) {
                html += "checked=\"checked\" ";
            }
            html += "/>";
        }
        if (DateFormat.class.getName().equals(formatClassName)) {
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputDate\" style=\"width: 100px;\" ";
            if (value instanceof Date) {
                html += "value=\"" + CalendarUtil.formatDate((Date) value) + "\" ";
            }
            html += "/>";
        }
        if (TimeFormat.class.getName().equals(formatClassName)) {
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputTime\" style=\"width: 50px;\" ";
            if (value instanceof Date) {
                html += "value=\"" + CalendarUtil.formatTime((Date) value) + "\" ";
            }
            html += "/>";
        }
        if (DateTimeFormat.class.getName().equals(formatClassName)) {
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputDateTime\" style=\"width: 150px;\" ";
            if (value instanceof Date) {
                html += "value=\"" + CalendarUtil.formatDateTime((Date) value) + "\" ";
            }
            html += "/>";
        }
        if (ActorFormat.class.getName().equals(formatClassName) || ExecutorFormat.class.getName().equals(formatClassName)
                || GroupFormat.class.getName().equals(formatClassName)) {
            html = ViewUtil.createExecutorSelect(user, variableName, formatClassName, value, true);
        }
        return html;
    }

    public static String getComponentOutput(User user, WebHelper webHelper, Long processId, String variableName, String formatClassName, Object value) {
        String html = "";
        if (StringFormat.class.getName().equals(formatClassName)) {
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputString\" disabled=\"true\" ";
            if (value != null) {
                html += "value=\"" + value + "\" ";
            }
            html += "/>";
        }
        if (TextFormat.class.getName().equals(formatClassName)) {
            html += "<textarea name=\"" + variableName + "\" class=\"inputText\" disabled=\"true\">";
            if (value != null) {
                html += value;
            }
            html += "</textarea>";
        }
        if (LongFormat.class.getName().equals(formatClassName) || DoubleFormat.class.getName().equals(formatClassName)
                || BigDecimalFormat.class.getName().equals(formatClassName)) {
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputNumber\" disabled=\"true\" ";
            if (value instanceof Number) {
                html += "value=\"" + value + "\" ";
            }
            html += "/>";
        }
        if (FileFormat.class.getName().equals(formatClassName)) {
            // because component is not usable
            html += getFileOutput(webHelper, processId, variableName, (FileVariable) value);
        }
        if (BooleanFormat.class.getName().equals(formatClassName)) {
            html += "<input type=\"checkbox\" name=\"" + variableName + "\" class=\"inputBoolean\" disabled=\"true\" ";
            if (value instanceof Boolean && ((Boolean) value)) {
                html += "checked=\"checked\" ";
            }
            html += "/>";
        }
        if (DateFormat.class.getName().equals(formatClassName)) {
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputDate\" style=\"width: 100px;\" disabled=\"true\" ";
            if (value instanceof Date) {
                html += "value=\"" + CalendarUtil.formatDate((Date) value) + "\" ";
            }
            html += "/>";
        }
        if (TimeFormat.class.getName().equals(formatClassName)) {
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputTime\" style=\"width: 50px;\" disabled=\"true\" ";
            if (value instanceof Date) {
                html += "value=\"" + CalendarUtil.formatTime((Date) value) + "\" ";
            }
            html += "/>";
        }
        if (DateTimeFormat.class.getName().equals(formatClassName)) {
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputDateTime\" style=\"width: 150px;\" disabled=\"true\" ";
            if (value instanceof Date) {
                html += "value=\"" + CalendarUtil.formatDateTime((Date) value) + "\" ";
            }
            html += "/>";
        }
        if (ActorFormat.class.getName().equals(formatClassName) || ExecutorFormat.class.getName().equals(formatClassName)
                || GroupFormat.class.getName().equals(formatClassName)) {
            html = ViewUtil.createExecutorSelect(user, variableName, formatClassName, value, false);
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
        if (FileFormat.class.getName().equals(formatClassName) && WebResources.isAjaxFileInputEnabled()) {
            return "$('.dropzone').each(function () { initFileInput($(this)) });";
        }
        return "";
    }

    public static String getOutput(User user, WebHelper webHelper, Long processId, String variableName, String formatClassName, Object value) {
        VariableDefinition definition = new VariableDefinition(true, variableName, formatClassName, variableName);
        WfVariable variable = new WfVariable(definition, value);
        return getOutput(user, webHelper, processId, variable);
    }

    public static String getOutput(User user, WebHelper webHelper, Long processId, WfVariable variable) {
        try {
            if (variable.getValue() == null) {
                return "";
            }
            VariableFormat format = variable.getFormatNotNull();
            if (format instanceof FileFormat) {
                return getFileOutput(webHelper, processId, variable.getDefinition().getName(), (FileVariable) variable.getValue());
            }
            if (format instanceof ActorFormat || format instanceof ExecutorFormat || format instanceof GroupFormat) {
                Executor executor = (Executor) variable.getValue();
                if (Delegates.getAuthorizationService().isAllowed(user, Permission.READ, executor)) {
                    HashMap<String, Object> params = Maps.newHashMap();
                    params.put("id", executor.getId());
                    String href = webHelper.getActionUrl("/manage_executor", params);
                    return "<a href=\"" + href + "\">" + executor.getLabel() + "</a>";
                } else {
                    return executor.getLabel();
                }
            }
            if (format instanceof ListFormat) {
                List<Object> list = (List<Object>) variable.getValue();
                String elementFormatClassName = getElementFormatClassName(variable, 0);
                StringBuffer html = new StringBuffer();
                html.append("[");
                for (int i = 0; i < list.size(); i++) {
                    if (i != 0) {
                        html.append(", ");
                    }
                    Object o = list.get(i);
                    String value;
                    if (FileFormat.class.getName().equals(elementFormatClassName)) {
                        value = ViewUtil.getFileOutput(webHelper, processId, variable.getDefinition().getName(), (FileVariable) o, i, null);
                    } else {
                        value = ViewUtil.getOutput(user, webHelper, processId, variable.getDefinition().getName(), elementFormatClassName, o);
                    }
                    html.append(value);
                }
                html.append("]");
                return html.toString();
            }
            if (format instanceof TextFormat) {
                String s = variable.getValue() != null ? variable.getValue().toString() : "";
                s = s.replaceAll("\n", "<br>");
                return s;
            }
            if (format instanceof VariableDisplaySupport) {
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

    public static String getFileInput(WebHelper webHelper, String variableName) {
        if (!WebResources.isAjaxFileInputEnabled()) {
            return "<input type=\"file\" name=\"" + variableName + "\" class=\"inputFile\" />";
        }
        String attachImageUrl = "";
        String loadingImageUrl = "";
        String deleteImageUrl = "";
        UploadedFile file = null;
        String uploadFileTitle = "Upload file";
        String loadingMessage = "Loading ...";
        if (webHelper !=null) {
            attachImageUrl = webHelper.getUrl(Resources.IMAGE_ATTACH);
            loadingImageUrl = webHelper.getUrl(Resources.IMAGE_LOADING);
            deleteImageUrl = webHelper.getUrl(Resources.IMAGE_DELETE);
            file = FormSubmissionUtils.getUploadedFilesMap(webHelper.getRequest()).get(variableName);
            uploadFileTitle = Commons.getMessage("message.upload.file", webHelper.getPageContext());
            loadingMessage = Commons.getMessage("message.loading", webHelper.getPageContext());
        }
        String hideStyle = "style=\"display: none;\"";
        String html = "";
        html += "<div class=\"inputFileContainer\">";
        html += "\n\t<div class=\"dropzone\" " + (file != null ? hideStyle : "") + ">";
        html += "\n\t\t<div class=\"inputFileAttach\"><img src=\"" + attachImageUrl + "\" />" + uploadFileTitle + "</div>";
        html += "\n\t\t<input class=\"inputFile\" name=\"" + variableName + "\" type=\"file\" style=\"display: none;\">";
        html += "\n\t\tDrag & Drop";
        html += "\n\t</div>";
        html += "\n\t<div class=\"progressbar\" " + (file == null ? hideStyle : "") + ">";
        html += "\n\t\t<div class=\"line\" style=\"width: " + (file != null ? "10" : "") + "0%;\"></div>";
        html += "\n\t\t<div class=\"status\">";
        html += "\n\t\t\t<img src=\"" + attachImageUrl + "\" />";
        html += "\n\t\t\t<span class=\"statusText\">";
        if (file != null && webHelper != null) {
            String label = file.getName() + "<span style='color: #888'> - " + file.getSize() + "</span>";
            String viewUrl = webHelper.getUrl("/upload?action=view&inputId=" + variableName);
            html += "<a href='" + viewUrl + "'>" + label + "</a>";
        } else {
            html += loadingMessage;
        }
        html += "</span>";
        if (file != null) {
            html += "\n\t\t\t<img src=\"" + deleteImageUrl + "\" class=\"statusImg inputFileDelete\" inputId=\"" + variableName + "\">";
        } else {
            html += "\n\t\t\t<img src=\"" + loadingImageUrl + "\" class=\"statusImg\" inputId=\"" + variableName + "\">";
        }
        html += "\n\t\t</div>";
        html += "\n\t</div>";
        html += "\n</div>";
        return html;
    }

    public static String getFileOutput(WebHelper webHelper, Long processId, String variableName, FileVariable value) {
        return getFileOutput(webHelper, processId, variableName, value, null, null);
    }

    public static String getFileOutput(WebHelper webHelper, Long processId, String variableName, FileVariable value, Integer listIndex, Object mapKey) {
        if (value == null) {
            return "&nbsp;";
        }
        HashMap<String, Object> params = Maps.newHashMap();
        params.put("id", processId);
        params.put("variableName", variableName);
        if (listIndex != null) {
            params.put("listIndex", String.valueOf(listIndex));
        }
        if (mapKey != null) {
            params.put("mapKey", String.valueOf(mapKey));
        }
        return getFileOutput(webHelper, params, value.getName());
    }

    public static String getFileLogOutput(WebHelper webHelper, Long logId, String fileName) {
        HashMap<String, Object> params = Maps.newHashMap();
        params.put("logId", logId);
        return getFileOutput(webHelper, params, fileName);
    }

    private static String getFileOutput(WebHelper webHelper, Map<String, Object> params, String fileName) {
        String href = webHelper.getActionUrl("/variableDownloader", params);
        return "<a href=\"" + href + "\">" + fileName + "</>";
    }

    public static String getElementFormatClassName(WfVariable variable, int index) {
        if (variable != null) {
            VariableFormat format = variable.getFormatNotNull();
            if (format instanceof VariableFormatContainer) {
                return ((VariableFormatContainer) format).getComponentClassName(index);
            }
        }
        return StringFormat.class.getName();
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
    
}
