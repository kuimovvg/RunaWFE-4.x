package ru.runa.gpd.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.form.FormTypeProvider;
import ru.runa.gpd.form.FormVariableAccess;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.validation.ValidatorConfig;
import ru.runa.gpd.validation.ValidatorDefinition;
import ru.runa.gpd.validation.ValidatorDefinitionRegistry;
import ru.runa.gpd.validation.ValidatorParser;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.TimeFormat;

public class ValidationUtil {
    // TODO refactor this
    // define extension point, move to 'forms' package
    public static ValidatorDefinition getValidatorDefinition(String name) {
        return ValidatorDefinitionRegistry.getValidatorDefinitions().get(name);
    }

    public static List<ValidatorDefinition> getFieldValidatorDefinitions(Variable variable) {
        List<ValidatorDefinition> result = new ArrayList<ValidatorDefinition>();
        for (ValidatorDefinition definition : ValidatorDefinitionRegistry.getValidatorDefinitions().values()) {
            if (!definition.isGlobal() && definition.isApplicable(variable.getJavaClassName())) {
                if (DateFormat.class.getName().equals(variable.getFormat()) && "time".equals(definition.getName())) {
                    continue;
                }
                if (TimeFormat.class.getName().equals(variable.getFormat()) && definition.getName().startsWith("date")) {
                    continue;
                }
                result.add(definition);
            }
        }
        return result;
    }

    public static Map<String, Map<String, ValidatorConfig>> getInitialFormValidators(IFile adjacentFile, FormNode formNode) throws Exception {
        Map<String, Map<String, ValidatorConfig>> formFieldConfigs = new HashMap<String, Map<String, ValidatorConfig>>();
        IFile formFile = IOUtils.getAdjacentFile(adjacentFile, formNode.getFormFileName());
        if (formFile.exists()) {
            Map<String, FormVariableAccess> variableNames = FormTypeProvider.getFormType(formNode.getFormType()).getFormVariableNames(formFile, formNode);
            //ValidatorDefinition requiredDefinition = getValidatorDefinition(ValidatorDefinition.REQUIRED_VALIDATOR_NAME);
            for (String varName : variableNames.keySet()) {
                if (variableNames.get(varName) == FormVariableAccess.WRITE && formNode.getProcessDefinition().getVariableNames(true).contains(varName)) {
                    Map<String, ValidatorConfig> configs = new HashMap<String, ValidatorConfig>();
                    // add required validator 
                    //configs.put(ValidatorDefinition.REQUIRED_VALIDATOR_NAME, requiredDefinition.create(Messages.getString("Validation.DefaultRequired")));
                    formFieldConfigs.put(varName, configs);
                }
            }
        }
        return formFieldConfigs;
    }

    public static IFile createNewValidationUsingForm(IFile adjacentFile, String validationFileName, FormNode formNode) throws Exception {
        Map<String, Map<String, ValidatorConfig>> formFieldConfigs = getInitialFormValidators(adjacentFile, formNode);
        return rewriteValidation(adjacentFile, validationFileName, formFieldConfigs);
    }

    public static void updateValidation(IFile adjacentFile, FormNode formNode) throws Exception {
        IFile validationFile = IOUtils.getAdjacentFile(adjacentFile, formNode.getValidationFileName());
        boolean changed = false;
        Map<String, Map<String, ValidatorConfig>> configs;
        try {
            configs = ValidatorParser.parseValidatorConfigs(validationFile);
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("", e);
            configs = new HashMap<String, Map<String, ValidatorConfig>>();
        }
        Map<String, Map<String, ValidatorConfig>> newConfigs = getInitialFormValidators(adjacentFile, formNode);
        for (String variableName : newConfigs.keySet()) {
            if (!configs.containsKey(variableName)) {
                configs.put(variableName, newConfigs.get(variableName));
                changed = true;
            }
        }
        if (changed) {
            rewriteValidation(adjacentFile, formNode.getValidationFileName(), configs);
            formNode.setDirty();
        }
    }

    public static IFile createEmptyValidation(IFile adjacentFile, String validationFileName) {
        Map<String, Map<String, ValidatorConfig>> formFieldConfigs = new HashMap<String, Map<String, ValidatorConfig>>();
        return rewriteValidation(adjacentFile, validationFileName, formFieldConfigs);
    }

    public static IFile rewriteValidation(IFile file, String validationFileName, Map<String, Map<String, ValidatorConfig>> validators) {
        IFile validationFile = IOUtils.getAdjacentFile(file, validationFileName);
        ValidatorParser.writeValidatorXml(validationFile, validators);
        return validationFile;
    }
}
