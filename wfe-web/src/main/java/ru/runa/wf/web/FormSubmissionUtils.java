package ru.runa.wf.web;

import java.util.ArrayList;
import java.util.Arrays;
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

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.TypeConversionUtil;
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
public class FormSubmissionUtils {
    private static final Log log = LogFactory.getLog(FormSubmissionUtils.class);
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
    public static Map<String, String[]> getUserFormInput(ServletRequest request) {
        return (Map<String, String[]>) request.getAttribute(USER_DEFINED_VARIABLES);
    }

    /**
     * @return saved in request values from previous form submit (used to
     *         re-open form in case of validation errors)
     */
    public static Map<String, Object> getUserFormInputVariables(HttpServletRequest request, Interaction interaction) {
        Map<String, String[]> userInput = getUserFormInput(request);
        if (userInput != null) {
            List<String> formatErrorsForFields = new ArrayList<String>();
            return convert(request, userInput, interaction, formatErrorsForFields);
        }
        return null;
    }

    public static Map<String, String> getUserFormValidationErrors(ServletRequest request) {
        Map<String, String> map = (Map<String, String>) request.getAttribute(USER_ERRORS);
        if (map == null) {
            map = Maps.newHashMap();
        }
        return map;
    }

    private static Map<String, String[]> extractAllAvailableVariables(ActionForm actionForm) {
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
        List<String> formatErrorsForFields = new ArrayList<String>();
        Hashtable<String, Object> userInput = actionForm.getMultipartRequestHandler().getAllElements();
        Map<String, Object> variables = convert(request, userInput, interaction, formatErrorsForFields);
        if (formatErrorsForFields.size() > 0) {
            throw new VariablesFormatException(formatErrorsForFields);
        }
        log.debug("Submitted: " + variables);
        return variables;
    }

    private static Map<String, Object> convert(HttpServletRequest request, Map<String, ? extends Object> userInput, Interaction interaction,
            List<String> formatErrorsForFields) {
        try {
            HashMap<String, Object> variables = Maps.newHashMap();
            for (VariableDefinition variableDefinition : interaction.getVariables().values()) {
                VariableFormat<?> format = FormatCommons.create(variableDefinition);
                Object variableValue = null;
                if (format instanceof ListFormat) {
                    String sizeInputName = variableDefinition.getName() + ".size";
                    String[] strings = (String[]) userInput.get(sizeInputName);
                    if (strings == null || strings.length != 1) {
                        log.warn("Incorrect '" + sizeInputName + "' value submitted: " + Arrays.toString(strings));
                        continue;
                    }
                    int listSize = TypeConversionUtil.convertTo(int.class, strings[0]);
                    ListFormat listFormat = (ListFormat) format;
                    VariableFormat<?> componentFormat = FormatCommons.create(listFormat.getComponentClassName(0));
                    List<Object> list = Lists.newArrayListWithExpectedSize(listSize);
                    for (int i = 0; i < listSize; i++) {
                        String inputName = variableDefinition.getName() + "[" + i + "]";
                        Object componentValue = userInput.get(inputName);
                        list.add(convertComponent(inputName, componentFormat, componentValue, formatErrorsForFields));
                    }
                    // variableValue = listFormat.parse(listItems);
                    variableValue = list;
                } else {
                    Object value = userInput.get(variableDefinition.getName());
                    variableValue = convertComponent(variableDefinition.getName(), format, value, formatErrorsForFields);
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
            return variables;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private static Object convertComponent(String inputName, VariableFormat<?> format, Object value, List<String> formatErrorsForFields) {
        try {
            if (format instanceof BooleanFormat) {
                if (value == null) {
                    // HTTP FORM doesn't pass unchecked checkbox value
                    value = new String[] { Boolean.FALSE.toString() };
                }
            }
            if (value instanceof FormFile) {
                FormFile formFile = (FormFile) value;
                if (formFile.getFileSize() > 0) {
                    String contentType = formFile.getContentType();
                    if (contentType == null) {
                        contentType = "application/octet-stream";
                    }
                    return new FileVariable(formFile.getFileName(), formFile.getFileData(), contentType);
                }
            } else if (value instanceof String[]) {
                String valueToFormat = ((String[]) value)[0];
                try {
                    return format.parse(valueToFormat);
                } catch (Exception e) {
                    log.warn(e);
                    if (valueToFormat.length() > 0) {
                        // in other case we put validation in logic
                        formatErrorsForFields.add(inputName);
                    }
                }
            } else if (value == null) {
            } else {
                throw new InternalApplicationException("Unexpected class: " + value.getClass());
            }
            return null;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
