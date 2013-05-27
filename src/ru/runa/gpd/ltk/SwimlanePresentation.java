package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.swt.widgets.Display;

import ru.runa.gpd.lang.model.Swimlane;

public class SwimlanePresentation extends VariableRenameProvider<Swimlane> {
    public SwimlanePresentation(Swimlane swimlane) {
        setElement(swimlane);
    }

    @Override
    public List<Change> getChanges(String variableName, String replacement) throws Exception {
        List<Change> changes = new ArrayList<Change>();
        String config = element.getDelegationConfiguration();
        if (config != null && config.contains(getVariableRef(variableName))) {
            changes.add(new SwimlaneChange(element, variableName, replacement));
        }
        return changes;
    }

    private String getVariableRef(String variableName) {
        return "${" + variableName + "}";
    }

    private String getVariableRefQuoted(String variableName) {
        return Pattern.quote(getVariableRef(variableName));
    }

    private class SwimlaneChange extends TextCompareChange {
        public SwimlaneChange(Object element, String currentVariableName, String previewVariableName) {
            super(element, currentVariableName, previewVariableName);
        }

        @Override
        public Change perform(IProgressMonitor pm) throws CoreException {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    element.setDelegationConfiguration(getReplacementConfig());
                }
            });
            return new NullChange("Swimlane");
        }
        
        private String getReplacementConfig() {
            String config = element.getDelegationConfiguration();
            return config.replaceAll(getVariableRefQuoted(currentVariableName), Matcher.quoteReplacement(getVariableRef(replacementVariableName)));
        }

        @Override
        public String getCurrentContent(IProgressMonitor pm) throws CoreException {
            return element.getDelegationConfiguration();
        }
        
        @Override
        public String getPreviewContent(IProgressMonitor pm) throws CoreException {
            return getReplacementConfig();
        }
        
        @Override
        protected String toPreviewContent(String varName) {
            throw new UnsupportedOperationException();
        }
    }
}
