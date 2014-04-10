package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ltk.core.refactoring.Change;

import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.Duration;

import com.google.common.base.Objects;

public class TimerPresentation extends VariableRenameProvider<Timer> {
    public TimerPresentation(Timer timer) {
        setElement(timer);
    }

    @Override
    public List<Change> getChanges(Variable oldVariable, Variable newVariable) throws Exception {
        List<Change> changes = new ArrayList<Change>();
        if (Objects.equal(oldVariable.getName(), element.getDelay().getVariableName())) {
            changes.add(new TimedChange(element, oldVariable.getName(), newVariable.getName()));
        }
        return changes;
    }

    private class TimedChange extends TextCompareChange {
        
        public TimedChange(NamedGraphElement element, String currentVariableName, String previewVariableName) {
            super(element, currentVariableName, previewVariableName);
        }

        @Override
        protected void performInUIThread() {
            element.getDelay().setVariableName(replacementVariableName);
        }

        @Override
        protected String toPreviewContent(String varName) {
            StringBuffer buffer = new StringBuffer();
            Duration durationTmp = new Duration(element.getDelay().getDuration());
            durationTmp.setVariableName(varName);
            buffer.append(durationTmp.getDuration());
            return buffer.toString();
        }
    }
}
