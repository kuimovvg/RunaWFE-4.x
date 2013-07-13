package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;

import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.Duration;

import com.google.common.base.Objects;

public class TimedPresentation extends VariableRenameProvider<ITimed> {
    public TimedPresentation(ITimed timed) {
        setElement(timed);
    }

    @Override
    public List<Change> getChanges(Variable oldVariable, Variable newVariable) throws Exception {
        List<Change> changes = new ArrayList<Change>();
        Timer timer = element.getTimer();
        if (timer != null && Objects.equal(oldVariable.getName(), timer.getDelay().getVariableName())) {
            changes.add(new TimedChange(element, oldVariable.getName(), newVariable.getName()));
        }
        return changes;
    }

    private class TimedChange extends TextCompareChange {
        public TimedChange(Object element, String currentVariableName, String previewVariableName) {
            super(element, currentVariableName, previewVariableName);
        }

        @Override
        public Change perform(IProgressMonitor pm) throws CoreException {
            element.getTimer().getDelay().setVariableName(replacementVariableName);
            return new NullChange("ITimed");
        }

        @Override
        protected String toPreviewContent(String varName) {
            StringBuffer buffer = new StringBuffer();
            Duration durationTmp = new Duration(element.getTimer().getDelay().getDuration());
            durationTmp.setVariableName(varName);
            buffer.append(durationTmp.getDuration());
            return buffer.toString();
        }
    }
}
