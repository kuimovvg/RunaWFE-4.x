package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.handler.CustomizationRegistry;
import ru.runa.gpd.handler.DelegableProvider;
import ru.runa.gpd.lang.model.Delegable;

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
