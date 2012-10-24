package org.jbpm.ui.common.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.PartInitException;
import org.jbpm.ui.DesignerLogger;
import org.jbpm.ui.DesignerPlugin;
import org.jbpm.ui.common.model.FormNode;
import org.jbpm.ui.forms.FTLStubFormType;
import org.jbpm.ui.pref.PrefConstants;
import org.jbpm.ui.resource.Messages;
import org.jbpm.ui.util.Streamer;

public class OpenExternalFormEditorDelegate extends OpenFormEditorDelegate {

    @Override
    protected void openInEditor(IFile file, FormNode formNode) throws PartInitException {
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
