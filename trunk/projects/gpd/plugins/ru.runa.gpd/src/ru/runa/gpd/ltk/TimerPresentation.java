package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;

import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.util.Delay;

public class TimerPresentation extends VariableRenameProvider<Timer> {
    public TimerPresentation(Timer timer) {
        setElement(timer);
    }

    @Override
    public List<Change> getChanges(String variableName, String replacement) throws Exception {
        List<Change> changes = new ArrayList<Change>();
        if (variableName.equals(element.getDelay().getVariableName())) {
            changes.add(new TimedChange(element, variableName, replacement));
        }
        return changes;
    }

    private class TimedChange extends TextCompareChange {
        public TimedChange(NamedGraphElement element, String currentVariableName, String previewVariableName) {
            super(element, currentVariableName, previewVariableName);
        }

        @Override
        public Change perform(IProgressMonitor pm) throws CoreException {
            element.getDelay().setVariableName(replacementVariableName);
            return new NullChange("ITimed");
        }

        @Override
        protected String toPreviewContent(String varName) {
            StringBuffer buffer = new StringBuffer();
            Delay durationTmp = new Delay(element.getDelay().getDuration());
            durationTmp.setVariableName(varName);
            buffer.append(durationTmp.getDuration());
            return buffer.toString();
        }
    }
}
