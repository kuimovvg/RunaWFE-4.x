package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.core.refactoring.TextEditBasedChangeGroup;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.util.WorkspaceOperations;

@SuppressWarnings("rawtypes")
public class BotTaskConfigRenameProvider implements VariableRenameProvider {
    private BotTask botTask;
    private IFile botTaskInfoFile;

    public BotTaskConfigRenameProvider(BotTask botTask, IFile botTaskInfoFile) {
        this.botTask = botTask;
        this.botTaskInfoFile = botTaskInfoFile;
    }

    @Override
    public List<Change> getChanges(String variableName, String replacement) throws Exception {
        List<Change> changes = new ArrayList<Change>();
        if (botTask != null) {
            String conf = botTask.getConfig();
            if (conf != null && conf.contains(variableName)) {
                changes.add(new ConfigChange(variableName, replacement));
            }
        }
        return changes;
    }

    private class ConfigChange extends TextEditBasedChange {
        protected final String currentVariableName;
        protected final String replacementVariableName;

        public ConfigChange(String currentVariableName, String replacementVariableName) {
            super("");
            this.currentVariableName = currentVariableName;
            this.replacementVariableName = replacementVariableName;
        }

        @Override
        public Change perform(IProgressMonitor pm) throws CoreException {
            if (botTask != null) {
                String conf = botTask.getConfig();
                if (conf != null && conf.contains(currentVariableName)) {
                    botTask.setConfig(conf.replaceAll(currentVariableName, replacementVariableName));
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    if (page != null) {
                        IEditorPart editor = page.findEditor(new FileEditorInput(botTaskInfoFile));
                        if (editor != null) {
                            page.closeEditor(editor, false);
                        }
                    }
                    WorkspaceOperations.saveBotTask(botTaskInfoFile, botTask);
                }
            }
            return new NullChange("TaskState");
        }

        @Override
        public String getCurrentContent(IProgressMonitor pm) throws CoreException {
            return toPreviewContent(currentVariableName);
        }

        @Override
        public String getCurrentContent(IRegion region, boolean arg1, int arg2, IProgressMonitor pm) throws CoreException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getPreviewContent(IProgressMonitor pm) throws CoreException {
            return toPreviewContent(replacementVariableName);
        }

        @Override
        public String getPreviewContent(TextEditBasedChangeGroup[] groups, IRegion region, boolean arg2, int arg3, IProgressMonitor pm) throws CoreException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getModifiedElement() {
            return null;
        }

        @Override
        public void initializeValidationData(IProgressMonitor pm) {
        }

        @Override
        public RefactoringStatus isValid(IProgressMonitor pm) {
            return RefactoringStatus.createInfoStatus("Ok");
        }

        protected String toPreviewContent(String variableName) {
            return variableName;
        }
    }

    @Override
    public void setElement(GraphElement element) {
    }
}
