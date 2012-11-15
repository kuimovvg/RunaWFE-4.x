package ru.runa.gpd;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ru.runa.gpd.handler.CustomizationRegistry;
import ru.runa.gpd.util.ProjectFinder;

public class Activator extends AbstractUIPlugin implements PluginConstants {
    private static Activator plugin;

    public Activator() {
        plugin = this;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        try {
            IJavaProject project = ProjectFinder.getAnyJavaProject();
            if (project != null) {
                CustomizationRegistry.init(project);
            }
        } catch (Exception e) {
            PluginLogger.logError("Exception while loading customization ...", e);
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
    }

    public static Activator getDefault() {
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
