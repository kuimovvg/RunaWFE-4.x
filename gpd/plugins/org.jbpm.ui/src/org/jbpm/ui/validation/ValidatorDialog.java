package ru.runa.bpm.ui.validation;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import ru.runa.bpm.ui.resource.Messages;
import ru.runa.bpm.ui.wizard.CompactWizardDialog;

public class ValidatorDialog extends CompactWizardDialog {

    private Button resetToDefaultsButton;

    public ValidatorDialog(IWizard newWizard) {
        super(newWizard);
    }

    public Button getResetToDefaultsButton() {
        return resetToDefaultsButton;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        resetToDefaultsButton = createButton(parent, 197, Messages.getString("button.default"), false);
        super.createButtonsForButtonBar(parent);
    }

}
