package ru.runa.bpm.ui.common.action;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.common.command.FormNodeSetValidationFileCommand;
import ru.runa.bpm.ui.common.model.FormNode;
import ru.runa.bpm.ui.common.part.graph.FormNodeEditPart;
import ru.runa.bpm.ui.resource.Messages;
import ru.runa.bpm.ui.util.IOUtils;
import ru.runa.bpm.ui.util.ValidationUtil;
import ru.runa.bpm.ui.validation.ValidatorConfig;
import ru.runa.bpm.ui.validation.ValidatorDialog;
import ru.runa.bpm.ui.wizard.ValidatorWizard;

public class OpenFormValidationDelegate extends BaseActionDelegate {

    public void run(IAction action) {
        try {
            FormNode formNode = ((FormNodeEditPart) selectedPart).getModel();

            if (!formNode.hasFormValidation()) {
                if (!MessageDialog.openQuestion(targetPart.getSite().getShell(), "", Messages
                        .getString("OpenFormValidationDelegate.CreateEmptyValidation"))) {
                    return;
                }
                String validationFileName = ValidationUtil.getFormValidationFileName(formNode.getName());
                IFile file = ValidationUtil.rewriteValidation(getDefinitionFile(), validationFileName,
                        new HashMap<String, Map<String, ValidatorConfig>>());
                setNewValidationFormFile(formNode, file.getName());
            }
            IFile validationFile = IOUtils.getAdjacentFile(getDefinitionFile(), formNode.getValidationFileName());
            if (!validationFile.exists()) {
                ValidationUtil.createEmptyValidation(getDefinitionFile(), formNode.getValidationFileName());
            }
            openValidationFile(formNode, validationFile);
        } catch (Exception e) {
            DesignerLogger.logError(e);
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
