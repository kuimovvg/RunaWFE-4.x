package ru.runa.bpm.ui.forms;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.DesignerPlugin;
import ru.runa.bpm.ui.common.model.FormNode;
import ru.runa.bpm.ui.dialog.ChooseFormTypeDialog;
import ru.runa.bpm.ui.dialog.ExternalEditorDialog;
import ru.runa.bpm.ui.util.IOUtils;
import ru.runa.bpm.ui.util.InfoPathSupport;
import ru.runa.bpm.ui.util.ProjectFinder;
import ru.runa.bpm.ui.util.Streamer;
import ru.runa.bpm.ui.util.ValidationUtil;

public class XSNFormType extends FormType {

    public static final String NAME = "xsn";

    @Override
    public IEditorPart openForm(IFile formFile, FormNode formNode) throws CoreException {
        String infopathEditor = PlatformUI.getPreferenceStore().getString(ChooseFormTypeDialog.INFOPATH_EDITOR_PREFERENCE_ID);
        if (infopathEditor.length() == 0) {
            ExternalEditorDialog dialog = new ExternalEditorDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell(), infopathEditor);
            if (dialog.open() != Window.OK) {
                return null;
            }
            infopathEditor = dialog.getPath();
            PlatformUI.getPreferenceStore().setValue(ChooseFormTypeDialog.INFOPATH_EDITOR_PREFERENCE_ID, infopathEditor);
        }
        try {
            // InfoPathSupport requires validation file
            if (!formNode.hasFormValidation()) {
                String validationFileName = ValidationUtil.getFormValidationFileName(formNode.getName());
                IFile validationFile = ValidationUtil.createEmptyValidation(formFile, validationFileName);
                formNode.setValidationFileName(validationFile.getName());
            }

            String filePath = formFile.getLocation().toOSString();
            InfoPathSupport infoPathSupport = new InfoPathSupport(formNode, formFile, filePath);
            if (infoPathSupport.init()) {
                Process process = Runtime.getRuntime().exec(infopathEditor + " /design \"" + filePath + "\"");
                infoPathSupport.setProcess(process);
                infoPathSupport.start();
                new Streamer(process.getErrorStream()).start();
                new Streamer(process.getInputStream()).start();
            } else {
                MessageDialog.openInformation(DesignerPlugin.getDefault().getWorkbench().getDisplay().getActiveShell(), "Form is opened already",
                        "This form already opened with InfoPath");
            }
        } catch (Throwable e) {
            DesignerLogger.logError("Failed to start InfoPath editor from: \n" + infopathEditor, e);
        }
        return null;
    }

    @Override
    public String getFormFileName(IFile definitionFile, FormNode formNode) {
        try {
            ProjectFinder.refreshProcessFolder(definitionFile);
        } catch (CoreException e) {
            //
        }
        int counter = 1;
        IFile formFileTest = null;
        do {
            String fileName = "form" + counter + "." + getType();
            formFileTest = IOUtils.getAdjacentFile(definitionFile, fileName);
            counter++;
        } while (formFileTest.exists());
        return formFileTest.getName();
    }

    @Override
    public Map<String, Integer> getFormVariableNames(IFile formFile, FormNode formNode) {
        // InfoPathSupport makes this job after each transformation
        throw new UnsupportedOperationException("Don't use this operation for XSN form type.");
    }

    public static void performFormSynchronization(IFile formFile, FormNode formNode) {
        InfoPathSupport infoPathSupport = new InfoPathSupport(formNode, formFile, formFile.getLocation().toOSString());
        try {
            if (!infoPathSupport.rewriteXsnFileWithAnotherTemplateId()) {
                DesignerLogger.logInfo("InfoPath form have not been rewrited");
            }
        } catch (Exception e) {
            DesignerLogger.logError("Error occured when rewriting InfoPath form", e);
        }
    }

    @Override
    public void validate(IFile formFile, FormNode formNode) {
        // 
    }
}
