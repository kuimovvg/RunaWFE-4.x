package ru.runa.gpd.ltk;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.core.refactoring.TextEditBasedChangeGroup;
import org.eclipse.ltk.ui.refactoring.TextEditChangeNode;
import org.eclipse.swt.widgets.Display;

public abstract class TextCompareChange extends TextEditBasedChange {
    protected final Object object;
    protected final String currentVariableName;
    protected final String replacementVariableName;

    public TextCompareChange(Object element, String currentVariableName, String replacementVariableName) {
        // TODO
        super(element.toString());
        this.object = element;
        this.currentVariableName = currentVariableName;
        this.replacementVariableName = replacementVariableName;
    }

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (adapter == TextEditChangeNode.class) {
            return new GPDChangeNode(this, object);
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
    public String getPreviewContent(TextEditBasedChangeGroup[] groups, IRegion region, boolean arg2, int arg3, IProgressMonitor pm) throws CoreException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getModifiedElement() {
        return object;
    }

    @Override
    public void initializeValidationData(IProgressMonitor pm) {
    }

    @Override
    public RefactoringStatus isValid(IProgressMonitor pm) {
        return RefactoringStatus.createInfoStatus("Ok");
    }

    protected abstract String toPreviewContent(String variableName);
    
    @Override
    public Change perform(IProgressMonitor pm) throws CoreException {
        Display.getDefault().asyncExec(new Runnable() {
            
            @Override
            public void run() {
                performInUIThread();
            }
        });
        return new NullChange();
    }
    
    protected abstract void performInUIThread();

}
