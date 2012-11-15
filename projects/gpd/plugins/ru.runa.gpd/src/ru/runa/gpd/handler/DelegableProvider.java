package ru.runa.gpd.handler;

import org.eclipse.jface.window.Window;
import org.osgi.framework.Bundle;

import ru.runa.gpd.lang.model.Delegable;

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
