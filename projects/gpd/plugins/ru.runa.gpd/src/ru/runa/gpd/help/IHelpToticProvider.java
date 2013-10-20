package ru.runa.gpd.help;

import org.eclipse.jface.viewers.ISelection;

public interface IHelpToticProvider {
    
    /**
     * Provides url of actual help topic
     * Used when IWorkbenchPart provides selection
     * 
     * @return url of actual help topic
     */
    String getHelpTopicUrl(ISelection selection);
    
    /**
     * Provides url of actual help topic
     * Used when IWorkbenchPart does not provide selection
     * 
     * @return url of actual help topic
     */
    String getHelpTopicUrl();
}
