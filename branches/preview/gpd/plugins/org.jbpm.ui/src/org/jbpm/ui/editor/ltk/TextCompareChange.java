package ru.runa.bpm.ui.editor.ltk;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.core.refactoring.TextEditBasedChangeGroup;
import org.eclipse.ltk.ui.refactoring.TextEditChangeNode;
import ru.runa.bpm.ui.common.model.GraphElement;
import ru.runa.bpm.ui.common.model.NamedGraphElement;

public abstract class TextCompareChange extends TextEditBasedChange {
    protected final GraphElement element;
    protected final String currentVariableName;
    protected final String replacementVariableName;

    public TextCompareChange(NamedGraphElement element, String currentVariableName, String replacementVariableName) {
        super(element.getName());
        this.element = element;
        this.currentVariableName = currentVariableName;
        this.replacementVariableName = replacementVariableName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == TextEditChangeNode.class) {
            return new GPDChangeNode(this, element);
        }
        return super.getAdapter(adapter);
    }

    @Override
    public String getCurrentContent(IProgressMonitor pm) throws CoreException {
        return toPreviewContent(currentVariableName);
    }

    @Override
    public String getCurrentContent(IRegion region, boolean arg1, int arg2, IProgressMonitor pm) throws CoreException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPreviewContent(IProgressMonitor pm) throws CoreException {
        return toPreviewContent(replacementVariableName);
    }

    @Override
    public String getPreviewContent(TextEditBasedChangeGroup[] groups, IRegion region, boolean arg2, int arg3, IProgressMonitor pm)
            throws CoreException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getModifiedElement() {
        return element;
    }

    @Override
    public void initializeValidationData(IProgressMonitor pm) {
    }

    @Override
    public RefactoringStatus isValid(IProgressMonitor pm) {
        return RefactoringStatus.createInfoStatus("Ok");
    }

    protected abstract String toPreviewContent(String variableName);

}
