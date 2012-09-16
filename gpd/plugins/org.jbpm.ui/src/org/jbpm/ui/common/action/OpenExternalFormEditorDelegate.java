package ru.runa.bpm.ui.common.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.PartInitException;
import ru.runa.bpm.ui.DesignerLogger;
import ru.runa.bpm.ui.DesignerPlugin;
import ru.runa.bpm.ui.common.model.FormNode;
import ru.runa.bpm.ui.forms.XSNFormType;
import ru.runa.bpm.ui.pref.PrefConstants;
import ru.runa.bpm.ui.resource.Messages;
import ru.runa.bpm.ui.util.Streamer;

public class OpenExternalFormEditorDelegate extends OpenFormEditorDelegate {

    @Override
    protected void openInEditor(IFile file, FormNode formNode) throws PartInitException {
        if (XSNFormType.NAME.equals(formNode.getFormType())) {
            MessageDialog.openInformation(DesignerPlugin.getDefault().getWorkbench().getDisplay().getActiveShell(), 
                    Messages.getString("message.warning"),
                    Messages.getString("ExternalEditor.TryingOpenInfoPathForm.warning"));
            return;
        }
        String htmlEditorPath = DesignerPlugin.getPrefString(PrefConstants.P_FORM_EXTERNAL_EDITOR_PATH);
        String filePath = file.getLocation().toOSString();
        try {
            String[] commands = { htmlEditorPath, filePath };
            Process process = Runtime.getRuntime().exec(commands);
            new ProcessListener(process, file).start();
            new Streamer(process.getErrorStream()).start();
            new Streamer(process.getInputStream()).start();
        } catch (Throwable e) {
            DesignerLogger.logError("Failed to start program \n" + htmlEditorPath, e);
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        action.setEnabled(DesignerPlugin.getPrefBoolean(PrefConstants.P_FORM_USE_EXTERNAL_EDITOR));
    }
    
    private class ProcessListener extends Thread {
        private final Process process;
        private final IFile file;

        public ProcessListener(Process process, IFile file) {
            this.process = process;
            this.file = file;
        }

        @Override
        public void run() {
            try {
                process.waitFor();
                file.getParent().refreshLocal(IResource.DEPTH_INFINITE, null);
            } catch (Exception e) {
                DesignerLogger.logError(e);
            }
        }
    }


}
