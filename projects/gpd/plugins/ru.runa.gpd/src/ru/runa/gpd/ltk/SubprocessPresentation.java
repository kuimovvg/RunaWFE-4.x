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

import com.google.common.base.Objects;

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
            changes.add(new SubprocessChange(element, oldVariable, newVariable, mappingsToChange));
        }
        return changes;
    }

    private class SubprocessChange extends TextCompareChange {
        private final List<VariableMapping> mappingsToChange;

        public SubprocessChange(NamedGraphElement element, Variable currentVariable, Variable replacementVariable, List<VariableMapping> mappingsToChange) {
            super(element, currentVariable, replacementVariable);
            this.mappingsToChange = mappingsToChange;
        }

        @Override
        protected void performInUIThread() {
            for (VariableMapping mapping : mappingsToChange) {
                if (Objects.equal(currentVariable.getName(), mapping.getName())) {
                    mapping.setName(replacementVariable.getName());
                }
                if (mapping.isMultiinstanceLinkByRelation()) {
                    mapping.setName(mapping.getName().replace(Pattern.quote("(" + currentVariable.getName() + ")"),
                            Matcher.quoteReplacement("(" + replacementVariable.getName() + ")")));
                }
            }
        }

        @Override
        protected String toPreviewContent(Variable variable) {
            StringBuffer buffer = new StringBuffer();
            for (VariableMapping mapping : mappingsToChange) {
                if (Objects.equal(currentVariable.getName(), mapping.getName())) {
                    buffer.append("<variable access=\"").append(mapping.getUsage()).append("\" mapped-name=\"").append(variable.getName());
                    buffer.append("\" name=\"").append(mapping.getMappedName()).append("\" />").append("\n");
                }
                if (mapping.isMultiinstanceLinkByRelation()) {
                    String s = mapping.getName().replace(Pattern.quote("(" + currentVariable.getName() + ")"), Matcher.quoteReplacement("(" + variable.getName() + ")"));
                    buffer.append("<variable access=\"").append(mapping.getUsage()).append("\" mapped-name=\"").append(mapping.getMappedName());
                    buffer.append("\" name=\"").append(s).append("\" />").append("\n");
                }
            }
            return buffer.toString();
        }
    }
}
