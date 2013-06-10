package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import ru.runa.gpd.BotCache;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.WorkspaceOperations;

import com.google.common.base.Preconditions;

public class BotTaskConfigRenameProvider extends VariableRenameProvider<BotTask> {
    public BotTaskConfigRenameProvider(BotTask botTask) {
        Preconditions.checkNotNull(botTask);
        setElement(botTask);
    }

    @Override
    public List<Change> getChanges(Variable oldVariable, Variable newVariable) throws Exception {
        List<Change> changes = new ArrayList<Change>();
        if (element.getDelegationConfiguration() != null && element.getDelegationConfiguration().contains(Pattern.quote("\"" + oldVariable + "\""))) {
            changes.add(new ConfigChange(oldVariable.getName(), newVariable.getName()));
        }
        return changes;
    }

    private class ConfigChange extends TextCompareChange {
        public ConfigChange(String currentVariableName, String replacementVariableName) {
            super(element, currentVariableName, replacementVariableName);
            // unchecked by default
            setEnabled(false);
        }

        @Override
        public Change perform(IProgressMonitor pm) throws CoreException {
            String newConfiguration = getConfigurationReplacement();
            element.setDelegationConfiguration(newConfiguration);
            IFile botTaskFile = BotCache.getBotTaskFile(element);
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            if (page != null) {
                IEditorPart editor = page.findEditor(new FileEditorInput(botTaskFile));
                if (editor != null) {
                    page.closeEditor(editor, false);
                }
            }
            WorkspaceOperations.saveBotTask(botTaskFile, element);
            return new NullChange("BotTask");
        }

        private String getConfigurationReplacement() {
            String oldString = Pattern.quote("\"" + currentVariableName + "\"");
            String newString = Matcher.quoteReplacement("\"" + replacementVariableName + "\"");
            String newConfiguration = element.getDelegationConfiguration().replaceAll(oldString, newString);
            return newConfiguration;
        }

        @Override
        public String getCurrentContent(IProgressMonitor pm) throws CoreException {
            return element.getDelegationConfiguration();
        }

        @Override
        public String getPreviewContent(IProgressMonitor pm) throws CoreException {
            return getConfigurationReplacement();
        }

        @Override
        protected String toPreviewContent(String variableName) {
            throw new UnsupportedOperationException();
        }
    }
}
