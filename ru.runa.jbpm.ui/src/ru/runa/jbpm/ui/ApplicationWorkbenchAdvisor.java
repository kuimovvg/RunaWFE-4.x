package ru.runa.jbpm.ui;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.jbpm.ui.DesignerLogger;
import org.jbpm.ui.PluginConstants;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

    private static final String PERSPECTIVE_ID = "org.jbpm.ui.perspective";

    @Override
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        return new ApplicationWorkbenchWindowAdvisor(configurer);
    }

    @Override
    public String getInitialWindowPerspectiveId() {
        return PERSPECTIVE_ID;
    }

    @Override
    public void preStartup() {
        getWorkbenchConfigurer().setSaveAndRestore(true);
        super.preStartup();
    }

    @Override
    public void postStartup() {
        PreferenceManager pm = PlatformUI.getWorkbench().getPreferenceManager();
        IPreferenceNode[] nodes = pm.getRootSubNodes();
        for (IPreferenceNode preferenceNode : nodes) {
            if (preferenceNode.getId().contains("jbpm") || preferenceNode.getId().contains("runa")) {
                continue;
            }
            pm.remove(preferenceNode.getId());
        }
        try {
            // refresh workspace and set default encoding to UTF8
            ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
            setUtfCharsetRecursively(ResourcesPlugin.getWorkspace().getRoot());
        } catch (CoreException e) {
            DesignerLogger.logErrorWithoutDialog("Unable to save workspace", e);
        }
    }

    private static void setUtfCharsetRecursively(IResource resource) throws CoreException {
        if (resource instanceof IFile) {
            IFile file = (IFile) resource;
            if (!PluginConstants.UTF_ENCODING.equalsIgnoreCase(file.getCharset())) {
                file.setCharset(PluginConstants.UTF_ENCODING, null);
            }
        }
        if (resource instanceof IContainer) {
            for (IResource member : ((IContainer) resource).members()) {
                setUtfCharsetRecursively(member);
            }
        }
    }

    @Override
    public boolean preShutdown() {
        try {
            // save the workspace before quit
            ResourcesPlugin.getWorkspace().save(true, null);
        } catch (CoreException e) {
            DesignerLogger.logErrorWithoutDialog("Unable to save workspace", e);
        }
        return super.preShutdown();
    }

}
