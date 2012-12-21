package ru.runa.gpd.lang.action;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.gef.command.FormNodeSetValidationFileCommand;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.ui.wizard.ValidatorWizard;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.ValidationUtil;
import ru.runa.gpd.validation.ValidatorConfig;
import ru.runa.gpd.validation.ValidatorDialog;

public class OpenFormValidationDelegate extends BaseModelActionDelegate {
    @Override
    public void run(IAction action) {
        try {
            FormNode formNode = getSelection();
            if (!formNode.hasFormValidation()) {
                if (!confirm("", Localization.getString("OpenFormValidationDelegate.CreateEmptyValidation"))) {
                    return;
                }
                String validationFileName = formNode.getId() + "." + FormNode.VALIDATION_SUFFIX;
                IFile file = ValidationUtil.rewriteValidation(getDefinitionFile(), validationFileName, new HashMap<String, Map<String, ValidatorConfig>>());
                setNewValidationFormFile(formNode, file.getName());
            }
            IFile validationFile = IOUtils.getAdjacentFile(getDefinitionFile(), formNode.getValidationFileName());
            if (!validationFile.exists()) {
                ValidationUtil.createEmptyValidation(getDefinitionFile(), formNode.getValidationFileName());
            }
            openValidationFile(formNode, validationFile);
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    public void openValidationFile(FormNode formNode, IFile validationFile) {
        ValidatorWizard wizard = new ValidatorWizard(validationFile, formNode);
        ValidatorDialog dialog = new ValidatorDialog(wizard);
        if (dialog.open() == IDialogConstants.OK_ID) {
            formNode.setDirty();
        }
    }

    private void setNewValidationFormFile(FormNode formNode, String fileName) {
        FormNodeSetValidationFileCommand command = new FormNodeSetValidationFileCommand();
        command.setFormNode(formNode);
        command.setValidationFileName(fileName);
        executeCommand(command);
    }
}
