package ru.runa.gpd.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.form.FormType;
import ru.runa.gpd.form.FormTypeProvider;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.validation.ValidatorConfig;
import ru.runa.gpd.validation.ValidatorDefinition;
import ru.runa.gpd.validation.ValidatorDefinitionRegistry;
import ru.runa.gpd.validation.ValidatorParser;

public class ValidationUtil {
    
    // TODO refactor this
    // define extension point, move to 'forms' package

    public static ValidatorDefinition getValidatorDefinition(String name) {
        return ValidatorDefinitionRegistry.getValidatorDefinitions().get(name);
    }

    public static List<ValidatorDefinition> getFieldValidatorDefinitions(String formatName) {
        List<ValidatorDefinition> result = new ArrayList<ValidatorDefinition>();
        for (ValidatorDefinition def : ValidatorDefinitionRegistry.getValidatorDefinitions().values()) {
            if (!def.isGlobal() && def.isApplicable(formatName)) {
                result.add(def);
            }
        }
        return result;
    }

    public static String getFormValidationFileName(String formName) {
        String validationFileName = formName + "." + FormNode.VALIDATION_SUFFIX;
        validationFileName = IOUtils.fixFileName(validationFileName);
        IStatus status = ResourcesPlugin.getWorkspace().validateName(validationFileName, IResource.FILE);
        if (!status.isOK()) {
            MessageDialog.openError(Activator.getDefault().getWorkbench().getDisplay().getActiveShell(), Localization
                    .getString("OpenFormEditorDelegate.error"), Localization.getString("OpenFormEditorDelegate.incorrectName") + ": "
                    + validationFileName);
        }
        return validationFileName;
    }

    public static Map<String, Map<String, ValidatorConfig>> getInitialFormValidators(IFile adjacentFile, FormNode formNode) throws Exception {
        Map<String, Map<String, ValidatorConfig>> formFieldConfigs = new HashMap<String, Map<String, ValidatorConfig>>();
        IFile formFile = IOUtils.getAdjacentFile(adjacentFile, formNode.getFormFileName());
        if (formFile.exists()) {
            Map<String, Integer> variableNames = FormTypeProvider.getFormType(formNode.getFormType()).getFormVariableNames(formFile, formNode);
            //ValidatorDefinition requiredDefinition = getValidatorDefinition(ValidatorDefinition.REQUIRED_VALIDATOR_NAME);
            for (String varName : variableNames.keySet()) {
            	if (variableNames.get(varName) == FormType.WRITE_ACCESS && formNode.getProcessDefinition().getVariableNames(true).contains(varName)) {
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
            configs = new HashMap<String, Map<String,ValidatorConfig>>();
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

    public static IFile createEmptyValidation(IFile adjacentFile, String validationFileName) throws Exception {
        Map<String, Map<String, ValidatorConfig>> formFieldConfigs = new HashMap<String, Map<String,ValidatorConfig>>();
        return rewriteValidation(adjacentFile, validationFileName, formFieldConfigs);
    }

    public static IFile rewriteValidation(IFile file, String validationFileName, Map<String, Map<String, ValidatorConfig>> validators) {
        validationFileName = IOUtils.fixFileName(validationFileName);
        IFile validationFile = IOUtils.getAdjacentFile(file, validationFileName);
        validationFile = ValidatorParser.writeValidatorXml(validationFile, validators);
        return validationFile;
    }

}
