package ru.runa.gpd.ltk;

import java.util.List;

import org.eclipse.ltk.core.refactoring.Change;

import ru.runa.gpd.lang.model.MultiTaskState;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.VariableMapping;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class MultiTaskPresentation extends VariableRenameProvider<MultiTaskState> {

    public MultiTaskPresentation(MultiTaskState timed) {
        setElement(timed);
    }

    @Override
    public List<Change> getChanges(Variable oldVariable, Variable newVariable) throws Exception {
        List<Change> changes = Lists.newArrayList();
        VariableMapping discriminatorMapping = element.getDiscriminatorMapping();
        if (discriminatorMapping.isMultiinstanceLinkByVariable() && Objects.equal(oldVariable.getName(), discriminatorMapping.getName())) {
            changes.add(new MultiTaskChange(element, oldVariable, newVariable));
        }
        if (discriminatorMapping.isMultiinstanceLinkByGroup() && !discriminatorMapping.isText() && Objects.equal(oldVariable.getName(), discriminatorMapping.getName())) {
            changes.add(new MultiTaskChange(element, oldVariable, newVariable));
        }
        if (discriminatorMapping.isMultiinstanceLinkByRelation() && discriminatorMapping.getName().contains("(" + oldVariable.getName() + ")")) {
            changes.add(new MultiTaskChange(element, oldVariable, newVariable));
        }
        return changes;
    }

    private class MultiTaskChange extends TextCompareChange {

        public MultiTaskChange(Object element, Variable currentVariable, Variable previewVariable) {
            super(element, currentVariable, previewVariable);
        }

        @Override
        protected void performInUIThread() {
            VariableMapping discriminatorMapping = element.getDiscriminatorMapping();
            if (discriminatorMapping.isMultiinstanceLinkByVariable()) {
                element.setExecutorsDiscriminatorValue(replacementVariable.getName());
            }
            if (discriminatorMapping.isMultiinstanceLinkByGroup() && !discriminatorMapping.isText()) {
                element.setExecutorsDiscriminatorValue(replacementVariable.getName());
            }
            if (discriminatorMapping.isMultiinstanceLinkByRelation()) {
                String s = element.getExecutorsDiscriminatorValue();
                s = s.replace("(" + currentVariable.getName() + ")", "(" + replacementVariable.getName() + ")");
                element.setExecutorsDiscriminatorValue(s);
            }
        }

        @Override
        protected String toPreviewContent(Variable variable) {
            return variable.getName();
        }
    }

}
