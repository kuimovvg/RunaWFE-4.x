package ru.runa.bpm.ui.dialog;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import ru.runa.bpm.ui.resource.Messages;

public class ConnectionSettingsDialog extends WizardDialog {
	private Button testButton;
	private Button syncButton;

    public ConnectionSettingsDialog(IWizard newWizard) {
        super(Display.getCurrent().getActiveShell(), newWizard);
    }
    
    public Button getTestButton() {
		return testButton;
	}

	public Button getSyncButton() {
		return syncButton;
	}

	@Override
    protected void createButtonsForButtonBar(final Composite parent) {
        testButton = createButton(parent, 197, Messages.getString("button.test.connection"), false);
        syncButton = createButton(parent, 198, Messages.getString("button.Synchronize"), false);
        super.createButtonsForButtonBar(parent);
    }

}
