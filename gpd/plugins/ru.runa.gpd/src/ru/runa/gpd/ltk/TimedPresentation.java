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
import ru.runa.gpd.util.TimerDuration;

@SuppressWarnings("unchecked")
public class TimedPresentation implements VariableRenameProvider {

    private ITimed timed;

    public TimedPresentation(ITimed timed) {
        setElement((GraphElement) timed);
    }

    public void setElement(GraphElement element) {
        this.timed = (ITimed) element;
    }

    public List<Change> getChanges(String variableName, String replacement) throws Exception {
        List<Change> changes = new ArrayList<Change>();
        TimerDuration duration = timed.getDuration();
        if (duration != null && variableName.equals(duration.getVariableName())) {
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
            timed.getDuration().setVariableName(replacementVariableName);
            return new NullChange("ITimed");
        }

        @Override
        protected String toPreviewContent(String varName) {
            StringBuffer buffer = new StringBuffer();
            TimerDuration durationTmp = new TimerDuration(timed.getDuration().getDuration());
            durationTmp.setVariableName(varName);
            buffer.append(durationTmp.getDuration());
            return buffer.toString();
        }
    }
}
