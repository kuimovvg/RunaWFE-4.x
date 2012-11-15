package ru.runa.gpd.ui.wizard;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

/**
 * Without progress bar area
 */
public class CompactWizardDialog extends WizardDialog {

    public CompactWizardDialog(IWizard newWizard) {
        super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), newWizard);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Control control = super.createDialogArea(parent);
        ((ProgressMonitorPart) getProgressMonitor()).dispose();
        return control;
    }

}
