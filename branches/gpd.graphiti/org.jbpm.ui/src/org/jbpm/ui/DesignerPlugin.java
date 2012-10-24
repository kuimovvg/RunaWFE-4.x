package org.jbpm.ui;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jbpm.ui.custom.CustomizationRegistry;
import org.jbpm.ui.util.ProjectFinder;
import org.osgi.framework.BundleContext;

public class DesignerPlugin extends AbstractUIPlugin implements PluginConstants {

    private static DesignerPlugin plugin;

    public DesignerPlugin() {
        plugin = this;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        try {
            File infopathAssemblyFolder = new File(getStateLocation().toFile(), "infopathassembly");
            if (infopathAssemblyFolder.exists()) {
                delete(infopathAssemblyFolder);
            }
        } catch (Exception e) {
            DesignerLogger.logError("Exception deleting infopath assembly folder ...", e);
        }
        
        try {
            IJavaProject project = ProjectFinder.getAnyJavaProject();
            if (project != null) {
                CustomizationRegistry.init(project);
            }
        } catch (Exception e) {
            DesignerLogger.logError("Exception while loading customization ...", e);
        }
    }

    private void delete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File dirFile : files) {
                delete(dirFile);
            }
        }
        file.delete();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
    }

    public static DesignerPlugin getDefault() {
        return plugin;
    }

    public static File getPreferencesFolder() {
        IPath path = plugin.getStateLocation();
        File folder = path.toFile();
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }
    
    public static String getPrefString(String name) {
        return getDefault().getPreferenceStore().getString(name);
    }

    public static boolean getPrefBoolean(String name) {
        return getDefault().getPreferenceStore().getBoolean(name);
    }

}
