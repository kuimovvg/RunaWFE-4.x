package ru.runa.gpd.extension;

import org.eclipse.jface.window.Window;
import org.osgi.framework.Bundle;

import ru.runa.gpd.lang.model.Delegable;

public class DelegableProvider {
    protected Bundle bundle;

    public Bundle getBundle() {
        return bundle;
    }

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

    /**
     * Validates configuration. Implementors can return <code>false</code> to raise default invalid configuration message. Or can invoke delegable.addError. 
     * @return <code>false</code> for raising default invalid configuration message
     */
    public boolean validateValue(Delegable delegable) {
        return true;
    }
}
