package ru.runa.bpm.ui.custom;

import org.eclipse.jface.window.Window;
import ru.runa.bpm.ui.common.model.Delegable;
import ru.runa.bpm.ui.dialog.DelegableConfigurationDialog;
import org.osgi.framework.Bundle;

public class DelegableProvider {

    protected Bundle bundle;

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    protected DelegableConfigurationDialog createConfigurationDialog(Delegable delegable) {
        return new DelegableConfigurationDialog(delegable.getDelegationConfiguration());
    }

    public String showConfigurationDialog(Delegable delegable) {
        DelegableConfigurationDialog dialog = createConfigurationDialog(delegable);
        if (dialog.open() == Window.OK) {
            return dialog.getResult();
        }
        return null;
    }

    public boolean validateValue(Delegable delegable) {
        return true;
    }
}
