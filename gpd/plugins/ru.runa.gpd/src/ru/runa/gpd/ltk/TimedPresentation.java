package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;

import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.util.Delay;

public class TimedPresentation implements VariableRenameProvider {
    private ITimed timed;

    public TimedPresentation(ITimed timed) {
        setElement((GraphElement) timed);
    }

    @Override
    public void setElement(GraphElement element) {
        this.timed = (ITimed) element;
    }

    @Override
    public List<Change> getChanges(String variableName, String replacement) throws Exception {
        List<Change> changes = new ArrayList<Change>();
        Timer timer = timed.getTimer();
        if (timer != null && variableName.equals(timer.getDelay().getVariableName())) {
            changes.add(new TimedChange((NamedGraphElement) timed, variableName, replacement));
        }
        return changes;
    }

    private class TimedChange extends TextCompareChange {
        public TimedChange(NamedGraphElement element, String currentVariableName, String previewVariableName) {
            super(element, currentVariableName, previewVariableName);
        }

        @Override
        public Change perform(IProgressMonitor pm) throws CoreException {
            timed.getTimer().getDelay().setVariableName(replacementVariableName);
            return new NullChange("ITimed");
        }

        @Override
        protected String toPreviewContent(String varName) {
            StringBuffer buffer = new StringBuffer();
            Delay durationTmp = new Delay(timed.getTimer().getDelay().getDuration());
            durationTmp.setVariableName(varName);
            buffer.append(durationTmp.getDuration());
            return buffer.toString();
        }
    }
}
