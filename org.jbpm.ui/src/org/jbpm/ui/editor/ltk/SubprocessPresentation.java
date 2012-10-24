package org.jbpm.ui.editor.ltk;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.jbpm.ui.common.model.NamedGraphElement;
import org.jbpm.ui.common.model.Subprocess;
import org.jbpm.ui.util.VariableMapping;

public class SubprocessPresentation implements VariableRenameProvider<Subprocess> {
    private Subprocess subprocess;

    public SubprocessPresentation(Subprocess subprocess) {
        setElement(subprocess);
    }

    public void setElement(Subprocess subprocess) {
        this.subprocess = subprocess;
    }

    public List<Change> getChanges(String variableName, String replacement) throws Exception {
        List<VariableMapping> mappingsToChange = new ArrayList<VariableMapping>();
        for (VariableMapping mapping : subprocess.getVariablesList()) {
            if (mapping.getProcessVariable().equals(variableName)) {
                mappingsToChange.add(mapping);
            }
        }

        List<Change> changes = new ArrayList<Change>();
        if (mappingsToChange.size() > 0) {
            changes.add(new SubprocessChange(subprocess, variableName, replacement, mappingsToChange));
        }
        return changes;
    }

    private class SubprocessChange extends TextCompareChange {
        private final List<VariableMapping> mappingsToChange;

        public SubprocessChange(NamedGraphElement element, String currentVariableName, String replacementVariableName,
                List<VariableMapping> mappingsToChange) {
            super(element, currentVariableName, replacementVariableName);
            this.mappingsToChange = mappingsToChange;
        }

        @Override
        public Change perform(IProgressMonitor pm) throws CoreException {
            for (VariableMapping mapping : mappingsToChange) {
                mapping.setProcessVariable(replacementVariableName);
            }
            return new NullChange("Subprocess");
        }

        @Override
        protected String toPreviewContent(String variableName) {
            StringBuffer buffer = new StringBuffer();
            for (VariableMapping mapping : mappingsToChange) {
                buffer.append("<variable access=\"").append(mapping.getUsage()).append("\" mapped-name=\"").append(variableName);
                buffer.append("\" name=\"").append(mapping.getSubprocessVariable()).append("\" />").append("\n");
            }
            return buffer.toString();
        }
    }
}
