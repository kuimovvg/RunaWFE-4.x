package org.jbpm.ui.common.action;

import org.eclipse.jface.action.IAction;
import org.jbpm.ui.common.model.Delegable;
import org.jbpm.ui.custom.CustomizationRegistry;
import org.jbpm.ui.custom.DelegableProvider;

public class OpenDelegableConfigurationDelegate extends BaseActionDelegate {

    public void run(IAction action) {
    	Delegable delegable = (Delegable) selectedPart.getModel();
        DelegableProvider provider = CustomizationRegistry.getProvider(delegable.getDelegationClassName());
        String newConfig = provider.showConfigurationDialog(delegable);
        if (newConfig != null) {
            delegable.setDelegationConfiguration(newConfig);
        }
    }

}
