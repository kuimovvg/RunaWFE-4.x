package ru.runa.wf.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

import ru.runa.wfe.commons.ftl.FtlTagVariableHandler;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.var.FileVariable;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.format.BooleanFormat;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@SuppressWarnings("unchecked")
public class FormUtils {
    private static final Log log = LogFactory.getLog(FormUtils.class);
    public static final String USER_DEFINED_VARIABLES = "UserDefinedVariables";
    public static final String USER_ERRORS = "UserErrors";

    /**
     * save in request user input with errors
     * 
     * @param userInputErrors
     *            validation errors
     */
    public static void saveUserFormInput(HttpServletRequest request, ActionForm form, Map<String, String> userInputErrors) {
        request.setAttribute(USER_DEFINED_VARIABLES, extractAllAvailableVariables(form));
        // save in request user errors
        request.setAttribute(USER_ERRORS, userInputErrors);
    }

    /**
     * @return saved in request values from previous form submit (used to
     *         re-open form in case of validation errors)
     */
    public static Map<String, String[]> getUserFormInputVariables(ServletRequest request) {
        return (Map<String, String[]>) request.getAttribute(USER_DEFINED_VARIABLES);
    }

    public static Map<String, String> getUserFormValidationErrors(ServletRequest request) {
        return (Map<String, String>) request.getAttribute(USER_ERRORS);
    }

    public static Map<String, String[]> extractAllAvailableVariables(ActionForm actionForm) {
        Hashtable<String, Object> hashtable = actionForm.getMultipartRequestHandler().getAllElements();
        Map<String, String[]> variablesMap = new HashMap<String, String[]>();
        for (String varName : hashtable.keySet()) {
            Object value = hashtable.get(varName);
            if (value instanceof FormFile) {
                // we could not fulfill in future this type of the input on the
                // web page (access restriction), so discard it
                continue;
            } else {
                variablesMap.put(varName, (String[]) value);
            }
        }
        return variablesMap;
    }

    public static Map<String, Object> extractVariables(HttpServletRequest request, ActionForm actionForm, Interaction interaction) {
        try {
            Hashtable<String, Object> hashtable = actionForm.getMultipartRequestHandler().getAllElements();
            List<String> formatErrorsForFields = new ArrayList<String>();
            HashMap<String, Object> variables = Maps.newHashMap();
            for (VariableDefinition variableDefinition : interaction.getVariables().values()) {
                Object value = hashtable.get(variableDefinition.getName());
                VariableFormat<?> format = FormatCommons.create(variableDefinition);
                // in case from contains not optional check box with boolean
                // format we must add boolean value as variable manually since
                // HTTP FORM doesn't pass unchecked variables.
                if (BooleanFormat.class == format.getClass()) {
                    if (value == null) {
                        value = new String[] { Boolean.FALSE.toString() };
                    }
                }
                if (value == null) {
                    // we put this validation in logic
                    continue;
                }
                Object variableValue = null;
                if (value instanceof FormFile) {
                    FormFile formFile = (FormFile) value;
                    if (formFile.getFileSize() > 0) {
                        String contentType = formFile.getContentType();
                        if (contentType == null) {
                            contentType = "application/octet-stream";
                        }
                        FileVariable fileVariable = new FileVariable(formFile.getFileName(), formFile.getFileData(), contentType);
                        if (ListFormat.class == format.getClass()) {
                            variableValue = Lists.newArrayList(fileVariable);
                        } else {
                            variableValue = fileVariable;
                        }
                    }
                } else {
                    String[] valuesToFormat = (String[]) value;
                    try {
                        variableValue = format.parse(valuesToFormat);
                    } catch (Exception e) {
                        log.error(e);
                        if (valuesToFormat[0].length() > 0) {
                            // in other case we put validation in logic
                            formatErrorsForFields.add(variableDefinition.getName());
                        }
                    }
                }
                if (variableValue != null) {
                    FtlTagVariableHandler handler = (FtlTagVariableHandler) request.getSession().getAttribute(
                            FtlTagVariableHandler.HANDLER_KEY_PREFIX + variableDefinition.getName());
                    if (handler != null) {
                        variableValue = handler.handle(variableValue);
                    }
                    variables.put(variableDefinition.getName(), variableValue);
                }
            }
            if (formatErrorsForFields.size() > 0) {
                throw new VariablesFormatException(formatErrorsForFields);
            }
            log.debug("Submitted: " + variables);
            return variables;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

}
