package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;

import ru.runa.gpd.lang.model.MessagingNode;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.gpd.util.VariableUtils;

public class MessagingNodeRenameProvider extends VariableRenameProvider<MessagingNode> {

    @Override
    public List<Change> getChanges(Variable oldVariable, Variable newVariable) throws Exception {
        List<VariableMapping> mappingsToChange = new ArrayList<VariableMapping>();
        for (VariableMapping mapping : element.getVariableMappings()) {
            if (mapping.isPropertySelector()) {
                if (mapping.getSubprocessVariableName().equals(VariableUtils.wrapVariableName(oldVariable.getName()))) {
                    mappingsToChange.add(mapping);
                }
            } else {
                if (mapping.getProcessVariableName().equals(oldVariable.getName())) {
                    mappingsToChange.add(mapping);
                }
            }
        }
        List<Change> changes = new ArrayList<Change>();
        if (mappingsToChange.size() > 0) {
            changes.add(new VariableMappingChange(element, oldVariable.getName(), newVariable.getName(), mappingsToChange));
        }
        return changes;
    }

    private class VariableMappingChange extends TextCompareChange {
        private final List<VariableMapping> mappingsToChange;

        public VariableMappingChange(NamedGraphElement element, String currentVariableName, String replacementVariableName, List<VariableMapping> mappingsToChange) {
            super(element, currentVariableName, replacementVariableName);
            this.mappingsToChange = mappingsToChange;
        }

        @Override
        public Change perform(IProgressMonitor pm) throws CoreException {
            for (VariableMapping mapping : mappingsToChange) {
                if (mapping.isPropertySelector()) {
                    mapping.setSubprocessVariableName(VariableUtils.wrapVariableName(replacementVariableName));
                } else {
                    mapping.setProcessVariableName(replacementVariableName);
                }
            }
            return new NullChange("Subprocess");
        }

        @Override
        protected String toPreviewContent(String variableName) {
            StringBuffer buffer = new StringBuffer();
            for (VariableMapping mapping : mappingsToChange) {
                buffer.append("<variable access=\"").append(mapping.getUsage()).append("\" mapped-name=\"");
                if (mapping.isPropertySelector()) {
                    buffer.append(mapping.getProcessVariableName()).append("\" name=\"").append(VariableUtils.wrapVariableName(variableName));
                } else {
                    buffer.append(variableName).append("\" name=\"").append(mapping.getSubprocessVariableName());
                }
                buffer.append("\" />").append("\n");
            }
            return buffer.toString();
        }
    }
}
