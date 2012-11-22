package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.handler.DelegableProvider;
import ru.runa.gpd.handler.HandlerRegistry;
import ru.runa.gpd.lang.model.Delegable;

public class OpenDelegableConfigurationDelegate extends BaseModelActionDelegate {
    @Override
    public void run(IAction action) {
        Delegable delegable = (Delegable) getSelection();
        DelegableProvider provider = HandlerRegistry.getProvider(delegable.getDelegationClassName());
        String newConfig = provider.showConfigurationDialog(delegable);
        if (newConfig != null) {
            delegable.setDelegationConfiguration(newConfig);
        }
    }
}
