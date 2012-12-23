package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;

import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.util.Delay;

public class TimerPresentation implements VariableRenameProvider {
    private Timer timer;

    public TimerPresentation(Timer timer) {
        setElement(timer);
    }

    @Override
    public void setElement(GraphElement element) {
        this.timer = (Timer) element;
    }

    @Override
    public List<Change> getChanges(String variableName, String replacement) throws Exception {
        List<Change> changes = new ArrayList<Change>();
        if (variableName.equals(timer.getDelay().getVariableName())) {
            changes.add(new TimedChange(timer, variableName, replacement));
        }
        return changes;
    }

    private class TimedChange extends TextCompareChange {
        public TimedChange(NamedGraphElement element, String currentVariableName, String previewVariableName) {
            super(element, currentVariableName, previewVariableName);
        }

        @Override
        public Change perform(IProgressMonitor pm) throws CoreException {
            timer.getDelay().setVariableName(replacementVariableName);
            return new NullChange("ITimed");
        }

        @Override
        protected String toPreviewContent(String varName) {
            StringBuffer buffer = new StringBuffer();
            Delay durationTmp = new Delay(timer.getDelay().getDuration());
            durationTmp.setVariableName(varName);
            buffer.append(durationTmp.getDuration());
            return buffer.toString();
        }
    }
}
