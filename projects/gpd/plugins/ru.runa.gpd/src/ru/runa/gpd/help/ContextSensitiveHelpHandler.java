package ru.runa.gpd.help;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.help.IWorkbenchHelpSystem;

public class ContextSensitiveHelpHandler extends AbstractHandler {
    /**
     * The identifier of the command parameter for the URI to open.
     */
    private static final String PARAM_ID_HREF = "href"; //$NON-NLS-1$

    public final Object execute(ExecutionEvent event) {
        final IWorkbenchHelpSystem helpSystem = PlatformUI.getWorkbench().getHelpSystem();
        final String href = event.getParameter(PARAM_ID_HREF);

        if (href == null) {
            String selectionHelpUrl = getUrl(event);

            if (selectionHelpUrl == null)
                helpSystem.displayHelp();
            else {
                helpSystem.displayHelpResource(selectionHelpUrl);
            }
        } else {
            helpSystem.displayHelpResource(href);
        }

        return null;
    }

    private String getUrl(ExecutionEvent event) {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        IWorkbenchPart part = HandlerUtil.getActivePart(event);

        String selectionHelpUrl = null;

        IHelpToticProvider helpTopicProvider = (IHelpToticProvider) part.getAdapter(IHelpToticProvider.class);
        if (helpTopicProvider != null) {
            if (selection != null)
                selectionHelpUrl = helpTopicProvider.getHelpTopicUrl(selection);
            else
                selectionHelpUrl = helpTopicProvider.getHelpTopicUrl();
        }

        return selectionHelpUrl;
    }
}
