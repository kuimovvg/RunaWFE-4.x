package ru.runa.wf.web.ftl.method;

import java.io.InputStream;
import java.util.ArrayList;
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
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.commons.web.WebUtils;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.ComplexVariable;
import ru.runa.wfe.var.FileVariable;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableUserType;
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
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.LongFormat;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.TextFormat;
import ru.runa.wfe.var.format.TimeFormat;
import ru.runa.wfe.var.format.UserTypeFormat;
import ru.runa.wfe.var.format.VariableDisplaySupport;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

public class ViewUtil {
    private static final Log log = LogFactory.getLog(ViewUtil.class);

    public static String createExecutorSelect(User user, WfVariable variable) {
        return createExecutorSelect(user, variable.getDefinition().getName(), variable.getDefinition().getFormatNotNull(), variable.getValue(), true);
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

    public static WfVariable createVariable(String variableName, String scriptingName, VariableFormat componentFormat, Object value) {
        VariableDefinition definition = new VariableDefinition(true, variableName, scriptingName, componentFormat.getClass().getName());
        return new WfVariable(definition, value);
    }

    public static WfVariable createComponentVariable(WfVariable containerVariable, String nameSuffix, VariableFormat componentFormat, Object value) {
        String name = containerVariable.getDefinition().getName() + (nameSuffix != null ? nameSuffix : "");
        String scriptingName = containerVariable.getDefinition().getScriptingName() + (nameSuffix != null ? nameSuffix : "");
        return createVariable(name, scriptingName, componentFormat, value);
    }

    public static WfVariable createListComponentVariable(WfVariable complexVariable, int index, VariableFormat componentFormat, Object value) {
        String nameSuffix = index == -1 ? "[]" : "[" + index + "]";
        return createComponentVariable(complexVariable, nameSuffix, componentFormat, value);
    }

    public static WfVariable createListSizeVariable(WfVariable complexVariable, Object value) {
        String nameSuffix = ".size";
        return createComponentVariable(complexVariable, nameSuffix, new StringFormat(), value);
    }

    public static WfVariable createUserTypeComponentVariable(WfVariable complexVariable, VariableDefinition attributeDefinition, Object value) {
        String nameSuffix = VariableUserType.DELIM + attributeDefinition.getName();
        return createComponentVariable(complexVariable, nameSuffix, attributeDefinition.getFormatNotNull(), value);
    }

    public static String getHiddenInput(WfVariable variable) {
        String stringValue = variable.getStringValue();
        if (stringValue == null) {
            stringValue = "";
        }
        return "<input type=\"hidden\" name=\"" + variable.getDefinition().getName() + "\" value=\"" + stringValue + "\" />";
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

    public static String getComponentInput(User user, WebHelper webHelper, WfVariable variable) {
        String variableName = variable.getDefinition().getName();
        VariableFormat variableFormat = variable.getDefinition().getFormatNotNull();
        Object value = variable.getValue();
        if (StringFormat.class == variableFormat.getClass()) {
            String html = "";
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputString\" ";
            if (value != null) {
                html += "value=\"" + value + "\" ";
            }
            html += "/>";
            return html;
        }
        if (TextFormat.class == variableFormat.getClass()) {
            String html = "";
            html += "<textarea name=\"" + variableName + "\" class=\"inputText\">";
            if (value != null) {
                html += value;
            }
            html += "</textarea>";
            return html;
        }
        if (variableFormat instanceof LongFormat || DoubleFormat.class == variableFormat.getClass()
                || BigDecimalFormat.class == variableFormat.getClass()) {
            String html = "";
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputNumber\" ";
            if (value instanceof Number) {
                html += "value=\"" + value + "\" ";
            }
            html += "/>";
            return html;
        }
        if (FileFormat.class == variableFormat.getClass()) {
            return getFileInput(webHelper, variableName, (FileVariable) value);
        }
        if (BooleanFormat.class == variableFormat.getClass()) {
            String html = "";
            html += "<input type=\"checkbox\" name=\"" + variableName + "\" class=\"inputBoolean\" ";
            if (value instanceof Boolean && ((Boolean) value)) {
                html += "checked=\"checked\" ";
            }
            html += "/>";
            return html;
        }
        if (DateFormat.class == variableFormat.getClass()) {
            String html = "";
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputDate\" style=\"width: 100px;\" ";
            if (value instanceof Date) {
                html += "value=\"" + CalendarUtil.formatDate((Date) value) + "\" ";
            }
            html += "/>";
            return html;
        }
        if (TimeFormat.class == variableFormat.getClass()) {
            String html = "";
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputTime\" style=\"width: 50px;\" ";
            if (value instanceof Date) {
                html += "value=\"" + CalendarUtil.formatTime((Date) value) + "\" ";
            }
            html += "/>";
            return html;
        }
        if (DateTimeFormat.class == variableFormat.getClass()) {
            String html = "";
            html += "<input type=\"text\" name=\"" + variableName + "\" class=\"inputDateTime\" style=\"width: 150px;\" ";
            if (value instanceof Date) {
                html += "value=\"" + CalendarUtil.formatDateTime((Date) value) + "\" ";
            }
            html += "/>";
            return html;
        }
        if (variableFormat instanceof ExecutorFormat) {
            return ViewUtil.createExecutorSelect(user, variableName, variableFormat, value, true);
        }
        if (variableFormat instanceof UserTypeFormat) {
            ComplexVariable complexVariable = (ComplexVariable) value;
            if (complexVariable == null) {
                complexVariable = new ComplexVariable();
            }
            VariableUserType userType = ((UserTypeFormat) variableFormat).getVariableDefinition().getUserType();
            StringBuffer b = new StringBuffer();
            b.append("<table class=\"list\">");
            for (VariableDefinition attributeDefinition : userType.getAttributes()) {
                b.append("<tr>");
                b.append("<td class=\"list\">").append(attributeDefinition.getName()).append("</td>");
                b.append("<td class=\"list\">");
                Object attributeValue = complexVariable.get(attributeDefinition.getName());
                WfVariable componentVariable = createUserTypeComponentVariable(variable, attributeDefinition, attributeValue);
                b.append(getComponentInput(user, webHelper, componentVariable));
                b.append("</td>");
                b.append("</tr>");
            }
            b.append("</table>");
            return b.toString();
        }
        if (variableFormat instanceof ListFormat) {
            String scriptingVariableName = variable.getDefinition().getScriptingNameWithoutDots();
            VariableFormat componentFormat = FormatCommons.createComponent(variable, 0);
            Map<String, String> substitutions = new HashMap<String, String>();
            substitutions.put("VARIABLE", variableName);
            substitutions.put("UNIQUENAME", scriptingVariableName);
            WfVariable templateComponentVariable = ViewUtil.createListComponentVariable(variable, -1, componentFormat, null);
            String inputTag = ViewUtil.getComponentInput(user, webHelper, templateComponentVariable);
            inputTag = inputTag.replaceAll("\"", "'").replaceAll("\t", "").replaceAll("\n", "");
            substitutions.put("COMPONENT_INPUT", inputTag);
            substitutions.put("COMPONENT_JS_HANDLER", ViewUtil.getComponentJSFunction(componentFormat));
            StringBuffer html = new StringBuffer();
            InputStream javascriptStream = ClassLoaderUtil.getAsStreamNotNull("scripts/ViewUtil.EditListTag.js", ViewUtil.class);
            html.append(WebUtils.getFreemarkerTagScript(webHelper, javascriptStream, substitutions));
            List<Object> list = TypeConversionUtil.convertTo(List.class, variable.getValue());
            if (list == null) {
                list = new ArrayList<Object>();
            }
            html.append("<span class=\"editList\" id=\"").append(scriptingVariableName).append("\">");
            WfVariable sizeVariable = ViewUtil.createListSizeVariable(variable, list.size());
            html.append(ViewUtil.getHiddenInput(sizeVariable));
            for (int row = 0; row < list.size(); row++) {
                Object o = list.get(row);
                html.append("<div row=\"").append(row).append("\">");
                WfVariable componentVariable = ViewUtil.createListComponentVariable(variable, row, componentFormat, o);
                html.append(ViewUtil.getComponentInput(user, webHelper, componentVariable));
                html.append("<input type='button' value=' - ' onclick=\"remove").append(scriptingVariableName)
                        .append("(this);\" style=\"width: 30px;\" />");
                html.append("</div>");
            }
            html.append("<div><input type=\"button\" id=\"btnAdd").append(scriptingVariableName)
                    .append("\" value=\" + \" style=\"width: 30px;\" /></div>");
            html.append("</span>");
            return html.toString();
        }
        throw new InternalApplicationException("No input method implemented for " + variableFormat);
    }

    public static String getComponentOutput(User user, WebHelper webHelper, Long processId, WfVariable variable) {
        String variableName = variable.getDefinition().getName();
        VariableFormat variableFormat = variable.getDefinition().getFormatNotNull();
        Object value = variable.getValue();
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
        if (variableFormat instanceof LongFormat || DoubleFormat.class == variableFormat.getClass()
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
            ComplexVariable complexVariable = (ComplexVariable) value;
            VariableUserType userType = ((UserTypeFormat) variableFormat).getVariableDefinition().getUserType();
            StringBuffer b = new StringBuffer();
            b.append("<table class=\"list\">");
            for (VariableDefinition attributeDefinition : userType.getAttributes()) {
                b.append("<tr>");
                b.append("<td class=\"list\">").append(attributeDefinition.getName()).append("</td>");
                b.append("<td class=\"list\">");
                Object attributeValue = complexVariable.get(attributeDefinition.getName());
                WfVariable componentVariable = createUserTypeComponentVariable(variable, attributeDefinition, attributeValue);
                b.append(getComponentOutput(user, webHelper, processId, componentVariable));
                b.append("</td>");
                b.append("</tr>");
            }
            b.append("</table>");
            return b.toString();
        }
        if (variableFormat instanceof ListFormat) {
            VariableFormat componentFormat = FormatCommons.createComponent((VariableFormatContainer) variableFormat, 0);
            StringBuffer html = new StringBuffer();
            List<Object> list = TypeConversionUtil.convertTo(List.class, value);
            html.append("<span class=\"viewList\" id=\"").append(variable.getDefinition().getScriptingName()).append("\">");
            if (list != null) {
                for (int row = 0; row < list.size(); row++) {
                    Object listValue = list.get(row);
                    html.append("<div row=\"").append(row).append("\">");
                    WfVariable componentVariable = createListComponentVariable(variable, row, componentFormat, listValue);
                    html.append(ViewUtil.getComponentOutput(user, webHelper, processId, componentVariable));
                    html.append("</div>");
                }
            }
            html.append("</span>");
            return html.toString();
        }
        throw new InternalApplicationException("No output method implemented for " + variableFormat);
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
            VariableFormat format = variable.getDefinition().getFormatNotNull();
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
                if (variable.getDefinition().isSynthetic()) {
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
        String html = "<div class=\"inputFileContainer\">";
        html += "<div class=\"dropzone\" " + (file != null ? hideStyle : "") + ">";
        html += "<label class=\"inputFileAttach\">";
        html += "<div class=\"inputFileAttachButtonDiv\"><img src=\"" + attachImageUrl + "\" />" + uploadFileTitle + "</div>";
        html += "<input class=\"inputFile inputFileAjax\" name=\"" + variableName + "\" type=\"file\">";
        html += "</label></div>";
        html += "<div class=\"progressbar\" " + (file == null ? hideStyle : "") + ">";
        html += "<div class=\"line\" style=\"width: " + (file != null ? "10" : "") + "0%;\"></div>";
        html += "<div class=\"status\">";
        if (file != null) {
            html += "<img src=\"" + deleteImageUrl + "\" class=\"inputFileDelete\" inputId=\"" + variableName + "\">";
        } else {
            html += "<img src=\"" + loadingImageUrl + "\" inputId=\"" + variableName + "\">";
        }
        html += "<span class=\"statusText\">";
        if (file != null && webHelper != null) {
            String viewUrl = webHelper.getUrl("/upload?action=view&inputId=" + variableName);
            html += "<a href='" + viewUrl + "'>" + file.getName() + " - " + file.getSize() + "</a>";
        } else {
            html += loadingMessage;
        }
        html += "</span></div></div></div>";
        return html;
    }

    public static String generateTableHeader(List<WfVariable> variables, IVariableProvider variableProvider, String operationsColumn) {
        StringBuffer header = new StringBuffer();
        header.append("<tr class=\"header\">");
        for (WfVariable variable : variables) {
            Object value = variableProvider.getValue(variable.getDefinition().getName() + "_header");
            if (value == null) {
                value = variable.getDefinition().getName();
            }
            header.append("<th>").append(value).append("</th>");
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
