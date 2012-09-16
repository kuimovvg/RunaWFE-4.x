package ru.runa.bpm.ui.common.action;

import org.eclipse.jface.action.IAction;
import ru.runa.bpm.ui.common.model.Delegable;
import ru.runa.bpm.ui.custom.CustomizationRegistry;
import ru.runa.bpm.ui.custom.DelegableProvider;

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
