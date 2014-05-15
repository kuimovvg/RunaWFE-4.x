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

import ru.runa.common.WebResources;
import ru.runa.wf.web.servlet.UploadedFile;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.FtlTagVariableHandler;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.var.ComplexVariable;
import ru.runa.wfe.var.FileVariable;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.format.BooleanFormat;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.UserTypeFormat;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@SuppressWarnings("unchecked")
public class FormSubmissionUtils {
    private static final Log log = LogFactory.getLog(FormSubmissionUtils.class);
    public static final String USER_DEFINED_VARIABLES = "UserDefinedVariables";
    public static final String USER_ERRORS = "UserErrors";
    public static final String UPLOADED_FILES = "UploadedFiles";
    public static final Object IGNORED_VALUE = new Object();
    public static final String SIZE_SUFFIX = ".size";
    public static final String COMPONENT_QUALIFIER_START = "[";
    public static final String COMPONENT_QUALIFIER_END = "]";
    public static final String FORM_NODE_ID_KEY = "UserDefinedVariablesForFormNodeId";

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
        if (userInput != null && Objects.equal(userInput.get(FORM_NODE_ID_KEY)[0], interaction.getNodeId())) {
            List<String> formatErrorsForFields = new ArrayList<String>();
            return extractVariables(request, userInput, interaction, formatErrorsForFields);
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
        Map<String, Object> userInput = Maps.newHashMap(actionForm.getMultipartRequestHandler().getAllElements());
        userInput.putAll(getUploadedFilesMap(request));
        Map<String, Object> variables = extractVariables(request, userInput, interaction, formatErrorsForFields);
        if (formatErrorsForFields.size() > 0) {
            throw new VariablesFormatException(formatErrorsForFields);
        }
        log.debug("Submitted: " + variables);
        return variables;
    }

    private static Map<String, Object> extractVariables(HttpServletRequest request, Map<String, ? extends Object> userInput, Interaction interaction,
            List<String> formatErrorsForFields) {
        try {
            HashMap<String, Object> variables = Maps.newHashMap();
            for (VariableDefinition variableDefinition : interaction.getVariables().values()) {
                Object variableValue = extractVariable(request, userInput, variableDefinition, formatErrorsForFields);
                if (!Objects.equal(IGNORED_VALUE, variableValue)) {
                    variables.put(variableDefinition.getName(), variableValue);
                }
            }
            return variables;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private static Object extractVariable(HttpServletRequest request, Map<String, ? extends Object> userInput, VariableDefinition variableDefinition,
            List<String> formatErrorsForFields) throws Exception {
        VariableFormat format = FormatCommons.create(variableDefinition);
        Object variableValue = null;
        boolean forceSetVariableValue = false;
        if (format instanceof ListFormat) {
            String sizeInputName = variableDefinition.getName() + SIZE_SUFFIX;
            ListFormat listFormat = (ListFormat) format;
            VariableFormat componentFormat = FormatCommons.createComponent(listFormat, 0);
            if (userInput.containsKey(sizeInputName)) {
                // js dynamic way
                String[] strings = (String[]) userInput.get(sizeInputName);
                if (strings == null || strings.length != 1) {
                    log.error("Incorrect '" + sizeInputName + "' value submitted: " + Arrays.toString(strings));
                    return null;
                }
                int listSize = TypeConversionUtil.convertTo(int.class, strings[0]);
                List<Object> list = Lists.newArrayListWithExpectedSize(listSize);
                for (int i = 0; i < listSize; i++) {
                    String name = variableDefinition.getName() + COMPONENT_QUALIFIER_START + i + COMPONENT_QUALIFIER_END;
                    String scriptingName = variableDefinition.getScriptingName() + COMPONENT_QUALIFIER_START + i + COMPONENT_QUALIFIER_END;
                    VariableDefinition componentDefinition = new VariableDefinition(true, name, scriptingName, componentFormat);
                    componentDefinition.setUserTypes(variableDefinition.getUserTypes());
                    Object componentValue = extractVariable(request, userInput, componentDefinition, formatErrorsForFields);
                    list.add(componentValue);
                }
                variableValue = list;
            } else {
                // http old-style way
                String[] strings = (String[]) userInput.get(variableDefinition.getName());
                if (strings == null || strings.length == 0) {
                    return null;
                }
                List<Object> list = Lists.newArrayListWithExpectedSize(strings.length);
                for (String componentValue : strings) {
                    list.add(convertComponent(variableDefinition.getName(), componentFormat, componentValue, formatErrorsForFields));
                }
                variableValue = list;
            }
        } else if (format instanceof UserTypeFormat) {
            List<VariableDefinition> expandedDefinitions = variableDefinition.expandComplexVariable();
            ComplexVariable complexVariable = new ComplexVariable();
            for (VariableDefinition expandedDefinition : expandedDefinitions) {
                Object componentValue = extractVariable(request, userInput, expandedDefinition, formatErrorsForFields);
                String attributeName = expandedDefinition.getName().substring(variableDefinition.getName().length() + 1);
                complexVariable.put(attributeName, componentValue);
            }
            variableValue = complexVariable;
        } else {
            Object value = userInput.get(variableDefinition.getName());
            if (value != null) {
                forceSetVariableValue = true;
            }
            variableValue = convertComponent(variableDefinition.getName(), format, value, formatErrorsForFields);
        }
        if (variableValue != null || forceSetVariableValue) {
            FtlTagVariableHandler handler = (FtlTagVariableHandler) request.getSession().getAttribute(
                    FtlTagVariableHandler.HANDLER_KEY_PREFIX + variableDefinition.getName());
            if (handler != null) {
                variableValue = handler.handle(variableValue);
            }
            return variableValue;
        }
        return IGNORED_VALUE;
    }

    private static Object convertComponent(String inputName, VariableFormat format, Object value, List<String> formatErrorsForFields) {
        try {
            if (format instanceof BooleanFormat) {
                if (value == null) {
                    // HTTP FORM doesn't pass unchecked checkbox value
                    value = new String[] { Boolean.FALSE.toString() };
                }
            }
            if (value instanceof String[]) {
                value = ((String[]) value)[0];
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
                if (SystemProperties.isV3CompatibilityMode() || !WebResources.isAjaxFileInputEnabled()) {
                    return IGNORED_VALUE;
                }
            } else if (value instanceof UploadedFile) {
                UploadedFile uploadedFile = (UploadedFile) value;
                return new FileVariable(uploadedFile.getName(), uploadedFile.getContent(), uploadedFile.getMimeType());
            } else if (value instanceof String) {
                String valueToFormat = (String) value;
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

    public static Map<String, UploadedFile> getUploadedFilesMap(HttpServletRequest request) {
        Map<String, UploadedFile> map = (Map<String, UploadedFile>) request.getSession().getAttribute(UPLOADED_FILES);
        if (map == null) {
            map = Maps.newHashMap();
            request.getSession().setAttribute(UPLOADED_FILES, map);
        }
        return map;
    }

}
