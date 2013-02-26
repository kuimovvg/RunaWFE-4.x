package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;

import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.util.Delay;

public class TimedPresentation extends VariableRenameProvider<ITimed> {
    public TimedPresentation(ITimed timed) {
        setElement(timed);
    }

    @Override
    public List<Change> getChanges(String variableName, String replacement) throws Exception {
        List<Change> changes = new ArrayList<Change>();
        Timer timer = element.getTimer();
        if (timer != null && variableName.equals(timer.getDelay().getVariableName())) {
            changes.add(new TimedChange(element, variableName, replacement));
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
            Delay durationTmp = new Delay(element.getTimer().getDelay().getDuration());
            durationTmp.setVariableName(varName);
            buffer.append(durationTmp.getDuration());
            return buffer.toString();
        }
    }
}
