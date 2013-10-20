package ru.runa.gpd;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;

public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
    public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
    }

    @Override
    protected void fillMenuBar(IMenuManager menuBar) {
        ActionSetRegistry reg = WorkbenchPlugin.getDefault().getActionSetRegistry();
        IActionSetDescriptor[] actionSets = reg.getActionSets();
        for (int i = 0; i < actionSets.length; i++) {
            if (actionSets[i].getId().contains("gpd")) {
                continue;
            }
            IExtension ext = actionSets[i].getConfigurationElement().getDeclaringExtension();
            reg.removeExtension(ext, new Object[] { actionSets[i] });
        }
        IContextService contextService = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);
        contextService.activateContext("ru.runa.gpd.context.app");
    }

    protected void makeActions(IWorkbenchWindow window) {
    	IWorkbenchAction contentsHelpAction = ActionFactory.HELP_CONTENTS.create(window);
    	register(contentsHelpAction);
    }
}
