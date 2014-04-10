package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ltk.core.refactoring.Change;

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
            if (mapping.isMultiinstanceLinkByRelation() && mapping.getName().contains("(" + oldVariable.getName() + ")")) {
                mappingsToChange.add(mapping);
            }
            if (mapping.isText()) {
                continue;
            }
            if (mapping.getName().equals(oldVariable.getName())) {
                mappingsToChange.add(mapping);
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
        protected void performInUIThread() {
            for (VariableMapping mapping : mappingsToChange) {
                if (currentVariableName.equals(mapping.getName())) {
                    mapping.setName(replacementVariableName);
                }
                if (mapping.isMultiinstanceLinkByRelation()) {
                    mapping.setName(mapping.getName().replace(
                            Pattern.quote("(" + currentVariableName + ")"), Matcher.quoteReplacement("(" + replacementVariableName + ")")));
                }
            }
        }

        @Override
        protected String toPreviewContent(String variableName) {
            StringBuffer buffer = new StringBuffer();
            for (VariableMapping mapping : mappingsToChange) {
                if (currentVariableName.equals(mapping.getName())) {
                    buffer.append("<variable access=\"").append(mapping.getUsage()).append("\" mapped-name=\"").append(variableName);
                    buffer.append("\" name=\"").append(mapping.getMappedName()).append("\" />").append("\n");
                }
                if (mapping.isMultiinstanceLinkByRelation()) {
                    String s = mapping.getName().replace(
                            Pattern.quote("(" + currentVariableName + ")"), Matcher.quoteReplacement("(" + variableName + ")"));
                    buffer.append("<variable access=\"").append(mapping.getUsage()).append("\" mapped-name=\"").append(mapping.getMappedName());
                    buffer.append("\" name=\"").append(s).append("\" />").append("\n");
                }
            }
            return buffer.toString();
        }
    }
}
