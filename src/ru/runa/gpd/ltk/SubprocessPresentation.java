package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;

import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.VariableMapping;

public class SubprocessPresentation extends VariableRenameProvider<Subprocess> {
    public SubprocessPresentation(Subprocess subprocess) {
        setElement(subprocess);
    }

    @Override
    public List<Change> getChanges(Variable oldVariable, Variable newVariable) throws Exception {
        List<VariableMapping> mappingsToChange = new ArrayList<VariableMapping>();
        for (VariableMapping mapping : element.getVariableMappings()) {
            if (mapping.getProcessVariableName().equals(oldVariable.getName())) {
                mappingsToChange.add(mapping);
                // TODO MultiSubprocess selector variables does not renamed automatically
                //            } else if ("tabVariableProcessVariable".equals(mapping.getProcessVariable()) && mapping.getSubprocessVariable().equals(oldVariable)) {
                //                
            }
        }
        List<Change> changes = new ArrayList<Change>();
        if (mappingsToChange.size() > 0) {
            changes.add(new SubprocessChange(element, oldVariable.getName(), newVariable.getName(), mappingsToChange));
        }
        return changes;
    }

    private class SubprocessChange extends TextCompareChange {
        private final List<VariableMapping> mappingsToChange;

        public SubprocessChange(NamedGraphElement element, String currentVariableName, String replacementVariableName, List<VariableMapping> mappingsToChange) {
            super(element, currentVariableName, replacementVariableName);
            this.mappingsToChange = mappingsToChange;
        }

        @Override
        public Change perform(IProgressMonitor pm) throws CoreException {
            for (VariableMapping mapping : mappingsToChange) {
                mapping.setProcessVariableName(replacementVariableName);
            }
            return new NullChange("Subprocess");
        }

        @Override
        protected String toPreviewContent(String variableName) {
            StringBuffer buffer = new StringBuffer();
            for (VariableMapping mapping : mappingsToChange) {
                buffer.append("<variable access=\"").append(mapping.getUsage()).append("\" mapped-name=\"").append(variableName);
                buffer.append("\" name=\"").append(mapping.getSubprocessVariableName()).append("\" />").append("\n");
            }
            return buffer.toString();
        }
    }
}
