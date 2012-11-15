package ru.runa.gpd.lang.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.gef.command.FormNodeSetFileCommand;
import ru.runa.gpd.editor.gef.part.graph.FormNodeEditPart;
import ru.runa.gpd.form.FormTypeProvider;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.ui.dialog.ChooseFormTypeDialog;
import ru.runa.gpd.util.IOUtils;

public class CreateFormDelegate extends BaseActionDelegate {

    public void run(IAction action) {
        try {
            FormNode formNode = ((FormNodeEditPart) selectedPart).getModel();
            ChooseFormTypeDialog chooseFormTypeDialog = new ChooseFormTypeDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell());
            if (chooseFormTypeDialog.open() != Window.OK) {
                return;
            }
            formNode.setFormType(chooseFormTypeDialog.getType());
            String fileName = FormTypeProvider.getFormType(formNode.getFormType()).getFormFileName(getDefinitionFile(), formNode);
            fileName = IOUtils.fixFileName(fileName);
            IFile file = IOUtils.getAdjacentFile(getDefinitionFile(), fileName);
            if (!file.exists()) {
                file = IOUtils.createFileSafely(file);
            }
            setNewFormFile(formNode, file.getName());
            formNode.setDirty();

            // open form
            OpenFormEditorDelegate openFormEditorDelegate;
            if (ChooseFormTypeDialog.EDITOR_EXTERNAL.equals(chooseFormTypeDialog.getEditorType())) {
                openFormEditorDelegate = new OpenExternalFormEditorDelegate();
            } else {
                openFormEditorDelegate = new OpenVisualFormEditorDelegate();
            }
            openFormEditorDelegate.targetPart = targetPart;
            openFormEditorDelegate.selectedPart = selectedPart;
            openFormEditorDelegate.run(action);
        } catch (CoreException e) {
            PluginLogger.logError(e);
            throw new RuntimeException(e);
        }
    }

    private void setNewFormFile(FormNode formNode, String fileName) {
        FormNodeSetFileCommand command = new FormNodeSetFileCommand();
        command.setFormNode(formNode);
        command.setFileName(fileName);
        executeCommand(command);
    }

}
