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
            changes.add(new MultiTaskChange(element, oldVariable.getName(), newVariable.getName()));
        }
        if (discriminatorMapping.isMultiinstanceLinkByGroup() && !discriminatorMapping.isText() && Objects.equal(oldVariable.getName(), discriminatorMapping.getName())) {
            changes.add(new MultiTaskChange(element, oldVariable.getName(), newVariable.getName()));
        }
        if (discriminatorMapping.isMultiinstanceLinkByRelation() && discriminatorMapping.getName().contains("(" + oldVariable.getName() + ")")) {
            changes.add(new MultiTaskChange(element, oldVariable.getName(), newVariable.getName()));
        }
        return changes;
    }

    private class MultiTaskChange extends TextCompareChange {

        public MultiTaskChange(Object element, String currentVariableName, String previewVariableName) {
            super(element, currentVariableName, previewVariableName);
        }

        @Override
        protected void performInUIThread() {
            VariableMapping discriminatorMapping = element.getDiscriminatorMapping();
            if (discriminatorMapping.isMultiinstanceLinkByVariable()) {
                element.setExecutorsDiscriminatorValue(replacementVariableName);
            }
            if (discriminatorMapping.isMultiinstanceLinkByGroup() && !discriminatorMapping.isText()) {
                element.setExecutorsDiscriminatorValue(replacementVariableName);
            }
            if (discriminatorMapping.isMultiinstanceLinkByRelation()) {
                String s = element.getExecutorsDiscriminatorValue();
                s = s.replace("(" + currentVariableName + ")", "(" + replacementVariableName + ")");
                element.setExecutorsDiscriminatorValue(s);
            }
        }

        @Override
        protected String toPreviewContent(String varName) {
            return varName;
        }
    }

}
